package com.specialtyfood.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for monitoring database and application performance.
 * Provides insights into connection pool usage, query performance, and cache statistics.
 */
@Service
public class PerformanceMonitoringService {

    @Autowired
    private DataSource dataSource;

    /**
     * Get database connection pool statistics.
     */
    public Map<String, Object> getConnectionPoolStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            stats.put("databaseProductName", metaData.getDatabaseProductName());
            stats.put("databaseProductVersion", metaData.getDatabaseProductVersion());
            stats.put("driverName", metaData.getDriverName());
            stats.put("driverVersion", metaData.getDriverVersion());
            stats.put("maxConnections", metaData.getMaxConnections());
            
            // HikariCP specific stats (if available)
            if (dataSource.getClass().getName().contains("HikariDataSource")) {
                try {
                    // Use reflection to get HikariCP stats
                    Object hikariPoolMXBean = dataSource.getClass().getMethod("getHikariPoolMXBean").invoke(dataSource);
                    if (hikariPoolMXBean != null) {
                        Class<?> mxBeanClass = hikariPoolMXBean.getClass();
                        stats.put("activeConnections", mxBeanClass.getMethod("getActiveConnections").invoke(hikariPoolMXBean));
                        stats.put("idleConnections", mxBeanClass.getMethod("getIdleConnections").invoke(hikariPoolMXBean));
                        stats.put("totalConnections", mxBeanClass.getMethod("getTotalConnections").invoke(hikariPoolMXBean));
                        stats.put("threadsAwaitingConnection", mxBeanClass.getMethod("getThreadsAwaitingConnection").invoke(hikariPoolMXBean));
                    }
                } catch (Exception e) {
                    stats.put("hikariStatsError", "Could not retrieve HikariCP statistics: " + e.getMessage());
                }
            }
            
        } catch (SQLException e) {
            stats.put("error", "Could not retrieve database statistics: " + e.getMessage());
        }
        
        return stats;
    }

    /**
     * Get cache performance statistics.
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Get cache statistics from JCache/Ehcache
            javax.cache.CacheManager cacheManager = javax.cache.Caching.getCachingProvider().getCacheManager();
            
            for (String cacheName : cacheManager.getCacheNames()) {
                javax.cache.Cache<Object, Object> cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    Map<String, Object> cacheStats = new HashMap<>();
                    
                    // Try to get cache statistics if available
                    try {
                        // This is implementation-specific and may not work with all cache providers
                        Object cacheStatistics = cache.getClass().getMethod("getStatistics").invoke(cache);
                        if (cacheStatistics != null) {
                            Class<?> statsClass = cacheStatistics.getClass();
                            cacheStats.put("hits", statsClass.getMethod("getCacheHits").invoke(cacheStatistics));
                            cacheStats.put("misses", statsClass.getMethod("getCacheMisses").invoke(cacheStatistics));
                            cacheStats.put("hitPercentage", statsClass.getMethod("getCacheHitPercentage").invoke(cacheStatistics));
                        }
                    } catch (Exception e) {
                        cacheStats.put("statisticsAvailable", false);
                        cacheStats.put("note", "Cache statistics not available for this implementation");
                    }
                    
                    stats.put(cacheName, cacheStats);
                }
            }
            
        } catch (Exception e) {
            stats.put("error", "Could not retrieve cache statistics: " + e.getMessage());
        }
        
        return stats;
    }

    /**
     * Get general performance recommendations.
     */
    public Map<String, Object> getPerformanceRecommendations() {
        Map<String, Object> recommendations = new HashMap<>();
        
        // Database recommendations
        Map<String, String> dbRecommendations = new HashMap<>();
        dbRecommendations.put("indexing", "Ensure proper indexes on frequently queried columns");
        dbRecommendations.put("connectionPool", "Monitor connection pool usage and adjust size as needed");
        dbRecommendations.put("queryOptimization", "Use pagination for large result sets");
        dbRecommendations.put("batchProcessing", "Use batch processing for bulk operations");
        recommendations.put("database", dbRecommendations);
        
        // Cache recommendations
        Map<String, String> cacheRecommendations = new HashMap<>();
        cacheRecommendations.put("readOnlyData", "Cache frequently accessed read-only data like categories");
        cacheRecommendations.put("searchResults", "Cache search results with appropriate TTL");
        cacheRecommendations.put("userSessions", "Cache user-specific data with shorter TTL");
        cacheRecommendations.put("monitoring", "Monitor cache hit rates and adjust cache sizes");
        recommendations.put("caching", cacheRecommendations);
        
        // Application recommendations
        Map<String, String> appRecommendations = new HashMap<>();
        appRecommendations.put("lazyLoading", "Use lazy loading for entity relationships");
        appRecommendations.put("pagination", "Always use pagination for large datasets");
        appRecommendations.put("projections", "Use projections for queries that don't need full entities");
        appRecommendations.put("asyncProcessing", "Use async processing for non-critical operations");
        recommendations.put("application", appRecommendations);
        
        return recommendations;
    }

    /**
     * Get system health check information.
     */
    public Map<String, Object> getSystemHealth() {
        Map<String, Object> health = new HashMap<>();
        
        // Database connectivity check
        try (Connection connection = dataSource.getConnection()) {
            health.put("database", "UP");
            health.put("databaseResponseTime", System.currentTimeMillis());
        } catch (SQLException e) {
            health.put("database", "DOWN");
            health.put("databaseError", e.getMessage());
        }
        
        // Memory usage
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> memory = new HashMap<>();
        memory.put("totalMemory", runtime.totalMemory());
        memory.put("freeMemory", runtime.freeMemory());
        memory.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
        memory.put("maxMemory", runtime.maxMemory());
        health.put("memory", memory);
        
        // Cache health
        try {
            javax.cache.CacheManager cacheManager = javax.cache.Caching.getCachingProvider().getCacheManager();
            health.put("cache", "UP");
            health.put("cacheNames", cacheManager.getCacheNames());
        } catch (Exception e) {
            health.put("cache", "DOWN");
            health.put("cacheError", e.getMessage());
        }
        
        return health;
    }
}