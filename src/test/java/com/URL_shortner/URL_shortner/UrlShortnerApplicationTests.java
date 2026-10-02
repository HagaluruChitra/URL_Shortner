package com.URL_shortner.URL_shortner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.chitra.urlshortener.analytics.ClickAnalyticsEvent;
import com.chitra.urlshortener.analytics.UrlClickAnalyticsProducer;
import com.chitra.urlshortener.auth.AuthenticatedUser;
import com.chitra.urlshortener.auth.RateLimitFilter;
import com.chitra.urlshortener.domain.ShortUrl;
import com.chitra.urlshortener.domain.UserEntity;
import com.chitra.urlshortener.repository.ShortUrlRepository;
import com.chitra.urlshortener.service.ShortUrlService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;

class UrlShortnerApplicationTests {

    @SuppressWarnings("unused")
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void allowsRequestsWithinLimit() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(redisWithCount(2L), new ObjectMapper(), 2, 60);
        FilterChain chain = (request, response) -> {
            ((jakarta.servlet.http.HttpServletResponse) response).setStatus(204);
        };
        AuthenticatedUser user = new AuthenticatedUser(7L, "alice@example.com", "password");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/urls");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertEquals(204, response.getStatus());
    }

    @Test
    void blocksRequestsAboveLimit() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(redisWithCount(3L), new ObjectMapper(), 2, 60);
        FilterChain chain = (request, response) -> {
            assertNotNull(request);
            ((jakarta.servlet.http.HttpServletResponse) response).setStatus(204);
        };
        AuthenticatedUser user = new AuthenticatedUser(7L, "alice@example.com", "password");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/urls");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertEquals(429, response.getStatus());
    }

    @Test
    void cachesShortUrlLookupByCode() {
        ShortUrlRepository repository = mock(ShortUrlRepository.class);
        CacheManager cacheManager = new ConcurrentMapCacheManager("short-url-by-code");
        ShortUrlService service = new ShortUrlService(repository, cacheManager);

        UserEntity owner = new UserEntity("Alice", "alice@example.com", "hashed-password");
        ShortUrl shortUrl = new ShortUrl(owner, "https://example.com", "abc123", null);

        service.cacheShortUrl(shortUrl);

        Cache cache = cacheManager.getCache("short-url-by-code");
        assertNotNull(cache);
        assertEquals("https://example.com", cache.get("abc123", ShortUrl.class).getOriginalUrl());
    }

    @Test
    void publishesBusinessAnalyticsEvent() {
        TestKafkaTemplate kafkaTemplate = new TestKafkaTemplate();
        UrlClickAnalyticsProducer producer = new UrlClickAnalyticsProducer(kafkaTemplate, "url-click-events");

        UserEntity owner = new UserEntity("Alice", "alice@example.com", "hashed-password");
        ShortUrl shortUrl = new ShortUrl(owner, "https://example.com", "abc123", null);

        producer.publishClick(shortUrl, "127.0.0.1", "curl/8.0", "https://example.org");

        assertEquals("url-click-events", kafkaTemplate.topic);
        assertNotNull(kafkaTemplate.event);
        assertEquals("abc123", kafkaTemplate.event.shortCode());
        assertEquals("127.0.0.1", kafkaTemplate.event.ipAddress());
    }

    private static final class TestKafkaTemplate extends KafkaTemplate<String, ClickAnalyticsEvent> {
        private String topic;
        private ClickAnalyticsEvent event;

        private TestKafkaTemplate() {
            super(new DefaultKafkaProducerFactory<>(testProducerProps()));
        }

        private static Map<String, Object> testProducerProps() {
            Map<String, Object> props = new HashMap<>();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
            return props;
        }

        @Override
        public CompletableFuture<SendResult<String, ClickAnalyticsEvent>> send(
                String topic, String key, ClickAnalyticsEvent data) {
            this.topic = topic;
            this.event = data;
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static StringRedisTemplate redisWithCount(long count) {
        return new StringRedisTemplate() {
            @Override
            public <T> T execute(RedisScript<T> script, List<String> keys, Object... args) {
                return (T) Long.valueOf(count);
            }
        };
    }
}
