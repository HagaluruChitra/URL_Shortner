package com.chitra.urlshortener.service;

import java.util.Optional;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.stereotype.Service;

import com.chitra.urlshortener.domain.ShortUrl;
import com.chitra.urlshortener.repository.ShortUrlRepository;

@Service
public class ShortUrlService {
    private static final String CACHE_NAME = "short-url-by-code";

    private final ShortUrlRepository shortUrlRepository;
    private final CacheManager cacheManager;

    public ShortUrlService(ShortUrlRepository shortUrlRepository) {
        this(shortUrlRepository, new ConcurrentMapCacheManager(CACHE_NAME));
    }

    public ShortUrlService(ShortUrlRepository shortUrlRepository, CacheManager cacheManager) {
        this.shortUrlRepository = shortUrlRepository;
        this.cacheManager = cacheManager;
    }

    @Cacheable(value = CACHE_NAME, key = "#shortCode", unless = "#result == null || #result.isEmpty()")
    public Optional<ShortUrl> findByShortCode(String shortCode) {
        return shortUrlRepository.findByShortCode(shortCode);
    }

    @CachePut(value = CACHE_NAME, key = "#shortUrl.shortCode")
    public ShortUrl cacheShortUrl(ShortUrl shortUrl) {
        if (shortUrl == null || shortUrl.getShortCode() == null || cacheManager == null) {
            return shortUrl;
        }

        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            cache.put(shortUrl.getShortCode(), shortUrl);
        }
        return shortUrl;
    }

    @CacheEvict(value = CACHE_NAME, key = "#shortCode")
    public void evictCache(String shortCode) {
        // cache is invalidated by the annotation; explicit no-op reserved for future cleanup
    }
}
