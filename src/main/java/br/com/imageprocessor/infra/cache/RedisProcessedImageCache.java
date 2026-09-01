package br.com.imageprocessor.infra.cache;

import br.com.imageprocessor.imageprocessor.application.ProcessedImageCache;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
public class RedisProcessedImageCache implements ProcessedImageCache {

    private static final Duration TTL = Duration.ofHours(1);

    private final RedisTemplate<String, byte[]> redisTemplate;

    public RedisProcessedImageCache(RedisTemplate<String, byte[]> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Optional<byte[]> get(String key) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key));
    }

    @Override
    public void put(String key, byte[] image) {
        redisTemplate.opsForValue().set(key, image, TTL);
    }
}
