package com.URL_shortner.URL_shortner;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

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
