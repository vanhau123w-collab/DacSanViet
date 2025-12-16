package com.specialtyfood.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Cache configuration for performance optimization.
 * Implements caching strategy for product catalog, user sessions, and search results.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configure cache manager with different cache regions for different data types.
     */
    @Bean
    public CacheManager cacheManager() {
        javax.cache.CacheManager jCacheManager = Caching.getCachingProvider().getCacheManager();
        
        // Product catalog cache - longer TTL since products don't change frequently
        MutableConfiguration<Object, Object> productCacheConfig = new MutableConfiguration<>()
                .setTypes(Object.class, Object.class)
                .setStoreByValue(false)
                .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.MINUTES, 30)));
        
        // User session cache - medium TTL for user-specific data
        MutableConfiguration<Object, Object> userCacheConfig = new MutableConfiguration<>()
                .setTypes(Object.class, Object.class)
                .setStoreByValue(false)
                .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.MINUTES, 15)));
        
        // Search results cache - shorter TTL since search results can change frequently
        MutableConfiguration<Object, Object> searchCacheConfig = new MutableConfiguration<>()
                .setTypes(Object.class, Object.class)
                .setStoreByValue(false)
                .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.MINUTES, 5)));
        
        // Category cache - long TTL since categories rarely change
        MutableConfiguration<Object, Object> categoryCacheConfig = new MutableConfiguration<>()
                .setTypes(Object.class, Object.class)
                .setStoreByValue(false)
                .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.MINUTES, 60)));

        // Create cache instances
        jCacheManager.createCache("products", productCacheConfig);
        jCacheManager.createCache("productDetails", productCacheConfig);
        jCacheManager.createCache("categories", categoryCacheConfig);
        jCacheManager.createCache("userSessions", userCacheConfig);
        jCacheManager.createCache("userCarts", userCacheConfig);
        jCacheManager.createCache("searchResults", searchCacheConfig);
        jCacheManager.createCache("featuredProducts", productCacheConfig);
        
        return new JCacheCacheManager(jCacheManager);
    }
}