#!/usr/bin/env node
/*
 * Rate limiter check: fires a burst of requests at POST /api/v1/images/process
 * and reports allowed (202/303) vs blocked (429).
 *
 * Prereqs: app running with app.ratelimit.enabled=true (default 60 req/min).
 * Run `make clean-redis` first (or the script clears ratelimit:* keys) so the
 * window starts empty.
 *
 * Exit 0 if the limiter kicked in (at least one 429), 1 otherwise.
 */

const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

const BASE_URL = process.env.BASE_URL || 'http://localhost:8080';
const IMAGE_PATH = process.env.SMOKE_IMAGE || path.join(__dirname, '..', 'src', 'test', 'resources', 'monke.jpg');
const BURST = Number(process.env.BURST || 70);

function clearRateLimitKeys() {
  try {
    const container = execSync('docker ps --filter ancestor=redis --format "{{.Names}}"', { encoding: 'utf8' })
      .trim()
      .split('\n')[0];
    if (!container) {
      return;
    }
    const out = execSync(`docker exec ${container} redis-cli --scan --pattern 'ratelimit:*'`, { encoding: 'utf8' });
    const keys = out.trim().split('\n').filter(Boolean);
    if (keys.length) {
      execSync(`docker exec ${container} redis-cli ${keys.map((k) => `DEL "${k}"`).join(' ')}`, { encoding: 'utf8' });
    }
    console.log(`cleared ${keys.length} ratelimit:* keys`);
  } catch {
    console.log('could not clear ratelimit:* keys (docker unavailable) — counters may not start at 0');
  }
}

async function main() {
  if (!fs.existsSync(IMAGE_PATH)) {
    throw new Error(`test image not found: ${IMAGE_PATH}`);
  }
  clearRateLimitKeys();

  const allowed = [];
  const blocked = [];
  for (let i = 0; i < BURST; i++) {
    const form = new FormData();
    form.append('operation', 'GRAYSCALE');
    form.append('file', new Blob([fs.readFileSync(IMAGE_PATH)], { type: 'image/jpeg' }), 'monke.jpg');
    const res = await fetch(`${BASE_URL}/api/v1/images/process`, { method: 'POST', body: form, redirect: 'manual' });
    if (res.status === 429) {
      blocked.push(res.status);
    } else {
      allowed.push(res.status);
    }
    process.stdout.write(`${res.status} `);
  }

  const allowedStatuses = [...new Set(allowed)];
  console.log(`\nallowed: ${allowed.length} (${allowedStatuses.length ? allowedStatuses.join('/') : '-'})`);
  console.log(`blocked: ${blocked.length} (429)`);
  console.log(`\nlimit hit at request ${allowed.length + 1} of ${BURST}`);

  if (blocked.length === 0) {
    throw new Error(`rate limiter did NOT kick in (0 x 429) — is it enabled and the app restarted?`);
  }
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(`RATE LIMIT CHECK FAILED: ${error.message}`);
    process.exit(1);
  });
