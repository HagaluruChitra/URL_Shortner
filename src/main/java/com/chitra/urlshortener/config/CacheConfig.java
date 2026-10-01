package com.chitra.urlshortener.config;

import java.time.Duration;
import java.util.Set;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(Environment environment,
                                    ObjectProvider<RedisConnectionFactory> redisConnectionFactoryProvider) {
        if (Boolean.parseBoolean(environment.getProperty("app.cache.redis.enabled", "false"))
                && redisConnectionFactoryProvider.getIfAvailable() != null) {
            RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofSeconds(environment.getProperty("app.cache.ttl-seconds", Integer.class, 300)))
                    .disableCachingNullValues()
                    .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                    .serializeValuesWith(RedisSerializationContext.SerializationPair
                            .fromSerializer(new GenericJackson2JsonRedisSerializer()));

            return RedisCacheManager.builder(redisConnectionFactoryProvider.getObject())
                    .cacheDefaults(cacheConfig)
                    .transactionAware()
                    .initialCacheNames(Set.of("short-url-by-code", "short-url-lookup"))
                    .build();
        }

        return new ConcurrentMapCacheManager("short-url-by-code", "short-url-lookup");
    }
}
