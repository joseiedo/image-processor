package br.com.imageprocessor.infra.ratelimit;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RateLimitFilterTest {

    private final StringRedisTemplate redis = mock(StringRedisTemplate.class);
    private final ValueOperations<String, String> ops = mock(ValueOperations.class);
    private final RateLimitProperties properties = new RateLimitProperties(true, 5, 60);
    private final RateLimitFilter filter = new RateLimitFilter(redis, properties);

    @Test
    void allowsFirstRequestAndSetsTtl() throws Exception {
        when(redis.opsForValue()).thenReturn(ops);
        when(ops.increment("ratelimit:127.0.0.1")).thenReturn(1L);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(new MockHttpServletRequest(), response, new MockFilterChain());

        assertEquals(200, response.getStatus());
        verify(redis).expire(eq("ratelimit:127.0.0.1"), any(Duration.class));
    }

    @Test
    void doesNotRefreshTtlOnSubsequentRequests() throws Exception {
        when(redis.opsForValue()).thenReturn(ops);
        when(ops.increment("ratelimit:127.0.0.1")).thenReturn(2L);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(new MockHttpServletRequest(), response, new MockFilterChain());

        assertEquals(200, response.getStatus());
        verify(redis, never()).expire(any(String.class), any(Duration.class));
    }

    @Test
    void rejectsRequestsAboveLimit() throws Exception {
        when(redis.opsForValue()).thenReturn(ops);
        when(ops.increment("ratelimit:127.0.0.1")).thenReturn(6L);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(new MockHttpServletRequest(), response, chain);

        assertEquals(429, response.getStatus());
        assertNull(chain.getRequest());
        verify(redis, never()).expire(any(String.class), any(Duration.class));
    }

    @Test
    void allowsWhenRedisFails() throws Exception {
        when(redis.opsForValue()).thenReturn(ops);
        when(ops.increment(any(String.class))).thenThrow(new RuntimeException("redis down"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(new MockHttpServletRequest(), response, new MockFilterChain());

        assertEquals(200, response.getStatus());
    }
}
