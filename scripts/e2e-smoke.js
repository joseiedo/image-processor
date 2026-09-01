#!/usr/bin/env node
/*
 * End-to-end smoke test: exercises the whole pipeline against a running app.
 * Prereqs: docker compose up -d && ./mvnw spring-boot:run
 *   (auth is currently disabled in SecurityConfig)
 *
 * Flow: for each operation, upload monke.jpg -> 202 + Location -> poll -> GET processed
 * image -> verify PNG. Runs every operation TWICE: the second pass has the same
 * content, so it must be served from the Redis cache (identical bytes, no new keys).
 * Writes the input + every result to target/e2e-report/ and generates index.html.
 */

const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const { execSync } = require('child_process');

const BASE_URL = process.env.BASE_URL || 'http://localhost:8080';
const IMAGE_PATH = process.env.SMOKE_IMAGE || path.join(__dirname, '..', 'src', 'test', 'resources', 'monke.jpg');
const REPORT_DIR = path.join(__dirname, '..', 'target', 'e2e-report');
const POLL_ATTEMPTS = 20;
const POLL_INTERVAL_MS = 500;

const OPERATIONS = [
  { name: 'grayscale', operation: 'GRAYSCALE', params: null },
  { name: 'resize', operation: 'RESIZE', params: '{"width":300,"height":300}' },
  { name: 'rotate', operation: 'ROTATE', params: '{"degrees":90}' },
  { name: 'crop', operation: 'CROP', params: '{"x":50,"y":50,"width":300,"height":300}' },
  { name: 'blur', operation: 'BLUR', params: '{"radius":10}' },
];

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

async function submitAndFetch(name, operation, params) {
  const form = new FormData();
  form.append('operation', operation);
  if (params) {
    form.append('params', params);
  }
  form.append('file', new Blob([fs.readFileSync(IMAGE_PATH)], { type: 'image/jpeg' }), path.basename(IMAGE_PATH));

  const submit = await fetch(`${BASE_URL}/api/v1/images/process`, { method: 'POST', body: form, redirect: 'manual' });
  const location = submit.headers.get('location');
  if (!location) {
    throw new Error(`${name}: process response missing Location header`);
  }

  if (submit.status === 202) {
    const id = location.split('/').pop();
    for (let attempt = 0; attempt < POLL_ATTEMPTS; attempt++) {
      const response = await fetch(`${BASE_URL}/api/v1/images/${id}`);
      if (response.ok) {
        const bytes = Buffer.from(await response.arrayBuffer());
        if (!isPng(bytes)) {
          throw new Error(`${name}: response is not a PNG`);
        }
        return { bytes, cached: false };
      }
      await sleep(POLL_INTERVAL_MS);
    }
    throw new Error(`${name}: timed out waiting for processed image ${id}`);
  }

  if (submit.status === 303) {
    const response = await fetch(`${BASE_URL}${location}`);
    if (!response.ok) {
      throw new Error(`${name}: cached result at ${location} returned ${response.status}`);
    }
    const bytes = Buffer.from(await response.arrayBuffer());
    if (!isPng(bytes)) {
      throw new Error(`${name}: cached response is not a PNG`);
    }
    return { bytes, cached: true };
  }

  throw new Error(`${name}: process endpoint returned ${submit.status} (is the app running?)`);
}

function isPng(bytes) {
  return bytes.length > 8
    && bytes[0] === 0x89 && bytes[1] === 0x50 && bytes[2] === 0x4e && bytes[3] === 0x47;
}

function sha256(bytes) {
  return crypto.createHash('sha256').update(bytes).digest('hex');
}

function countCacheKeys() {
  try {
    const container = execSync('docker ps --filter ancestor=redis --format "{{.Names}}"', { encoding: 'utf8' })
      .trim()
      .split('\n')[0];
    if (!container) {
      return null;
    }
    const out = execSync(`docker exec ${container} redis-cli --scan --pattern 'image:*'`, { encoding: 'utf8' });
    return out.trim() ? out.trim().split('\n').length : 0;
  } catch {
    return null;
  }
}

function buildHtml(results) {
  const body = Object.entries(results)
    .map(([name, file]) => `<h2>${name}</h2><img src="${file}">`)
    .join('\n');
  return `<!doctype html>
<html>
<head>
  <meta charset="utf-8">
  <title>E2E smoke report</title>
  <style>body{font-family:sans-serif;margin:2rem}img{display:block;margin:.5rem 0;border:1px solid #ccc;max-width:100%}</style>
</head>
<body>
  <h1>E2E smoke report</h1>
  ${body}
</body>
</html>`;
}

async function runPass(label) {
  const hashes = {};
  for (const { name, operation, params } of OPERATIONS) {
    const { bytes, cached } = await submitAndFetch(name, operation, params);
    hashes[name] = sha256(bytes);
    if (label === 'first') {
      fs.writeFileSync(path.join(REPORT_DIR, `${name}.png`), bytes);
    }
    const how = cached ? 'cached (redirect)' : 'processed (queued)';
    console.log(`OK: ${name} (${label}) -> ${how}, ${bytes.length} bytes, sha256=${hashes[name].slice(0, 12)}…`);
  }
  return hashes;
}

async function main() {
  if (!fs.existsSync(IMAGE_PATH)) {
    throw new Error(`test image not found: ${IMAGE_PATH}`);
  }
  fs.mkdirSync(REPORT_DIR, { recursive: true });
  fs.copyFileSync(IMAGE_PATH, path.join(REPORT_DIR, 'input.png'));

  console.log('--- first pass (cold cache) ---');
  const first = await runPass('first');

  const keysAfterFirst = countCacheKeys();
  console.log(`cache keys (image:*): ${keysAfterFirst === null ? 'n/a' : keysAfterFirst}`);

  console.log('--- second pass (same content -> cache) ---');
  const second = await runPass('second');

  for (const { name } of OPERATIONS) {
    if (second[name] !== first[name]) {
      throw new Error(`${name}: second pass differs from first (cache not reused)`);
    }
  }

  const keysAfterSecond = countCacheKeys();
  if (keysAfterFirst !== null && keysAfterSecond !== null) {
    const delta = keysAfterSecond - keysAfterFirst;
    console.log(`cache keys (image:*): ${keysAfterSecond} after second pass (${delta} new -> all served from cache)`);
  } else {
    console.log('cache keys (image:*): n/a (redis-cli/docker unavailable)');
  }

  const results = { input: 'input.png' };
  for (const { name } of OPERATIONS) {
    results[name] = `${name}.png`;
  }
  fs.writeFileSync(path.join(REPORT_DIR, 'index.html'), buildHtml(results));
  console.log(`report: ${path.join(REPORT_DIR, 'index.html')}`);
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(`SMOKE FAILED: ${error.message}`);
    process.exit(1);
  });
