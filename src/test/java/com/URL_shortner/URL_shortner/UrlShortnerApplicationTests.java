package com.URL_shortner.URL_shortner;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.chitra.urlshortener.auth.AuthenticatedUser;
import com.chitra.urlshortener.auth.RateLimitFilter;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;

class UrlShortnerApplicationTests {

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

    private static StringRedisTemplate redisWithCount(long count) {
        return new StringRedisTemplate() {
            @Override
            public <T> T execute(RedisScript<T> script, List<String> keys, Object... args) {
                return (T) Long.valueOf(count);
            }
        };
    }
}
