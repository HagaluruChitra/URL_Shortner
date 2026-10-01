package com.chitra.urlshortener.auth;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private static final DefaultRedisScript<Long> INCREMENT = new DefaultRedisScript<>(
            "local n = redis.call('INCR', KEYS[1]); if n == 1 then redis.call('EXPIRE', KEYS[1], ARGV[1]); end; return n", Long.class);
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final int limit;
    private final int windowSeconds;

    public RateLimitFilter(StringRedisTemplate redis, ObjectMapper objectMapper,
                           @Value("${app.rate-limit.requests}") int limit,
                           @Value("${app.rate-limit.window-seconds}") int windowSeconds) {
        this.redis = redis;
        this.objectMapper = objectMapper;
        this.objectMapper.findAndRegisterModules();
        this.limit = limit;
        this.windowSeconds = windowSeconds;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/urls");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            chain.doFilter(request, response);
            return;
        }
        long bucket = Instant.now().getEpochSecond() / windowSeconds;
        Long count = redis.execute(INCREMENT, List.of("rate:user:" + user.id() + ":" + bucket), Integer.toString(windowSeconds));
        if (count != null && count > limit) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            objectMapper.writeValue(response.getOutputStream(), java.util.Map.of("timestamp", Instant.now(), "status", 429,
                    "error", "Too Many Requests", "code", "RATE_LIMIT_EXCEEDED", "message", "Request limit exceeded.",
                    "path", request.getRequestURI()));
            return;
        }
        chain.doFilter(request, response);
    }
}