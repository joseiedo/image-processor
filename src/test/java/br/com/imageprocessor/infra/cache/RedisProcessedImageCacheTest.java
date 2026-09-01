package br.com.imageprocessor.infra.cache;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedisProcessedImageCacheTest {

    private final RedisTemplate<String, byte[]> redisTemplate = mock(RedisTemplate.class);
    private final ValueOperations<String, byte[]> ops = mock(ValueOperations.class);
    private final RedisProcessedImageCache cache = new RedisProcessedImageCache(redisTemplate);

    @Test
    void returnsEmptyWhenKeyMissing() {
        when(redisTemplate.opsForValue()).thenReturn(ops);
        when(ops.get("key")).thenReturn(null);

        Optional<byte[]> result = cache.get("key");

        assertTrue(result.isEmpty());
    }

    @Test
    void getsStoredBytes() {
        byte[] expected = {1, 2, 3};
        when(redisTemplate.opsForValue()).thenReturn(ops);
        when(ops.get("key")).thenReturn(expected);

        assertArrayEquals(expected, cache.get("key").orElseThrow());
    }

    @Test
    void putsWithTtl() {
        when(redisTemplate.opsForValue()).thenReturn(ops);

        cache.put("key", new byte[]{1, 2, 3});

        verify(ops).set(eq("key"), eq(new byte[]{1, 2, 3}), any(Duration.class));
    }
}
