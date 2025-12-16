package com.specialtyfood.config;

import org.springframework.context.annotation.Configuration;

/**
 * Query optimization configuration for improving database query performance.
 * This configuration provides query hints and optimization strategies.
 */
@Configuration
public class QueryOptimizationConfig {

    /**
     * Common query hints for performance optimization.
     * These can be used in repository methods with @QueryHints annotation.
     */
    public static class QueryHints {
        
        // Cache query results for read-only operations
        public static final String CACHEABLE = "org.hibernate.cacheable";
        public static final String CACHE_REGION = "org.hibernate.cacheRegion";
        
        // Fetch size optimization
        public static final String FETCH_SIZE = "org.hibernate.fetchSize";
        
        // Read-only optimization
        public static final String READ_ONLY = "org.hibernate.readOnly";
        
        // Timeout settings
        public static final String TIMEOUT = "javax.persistence.query.timeout";
        
        // Lock timeout
        public static final String LOCK_TIMEOUT = "javax.persistence.lock.timeout";
        
        // Common values
        public static final String TRUE = "true";
        public static final String FALSE = "false";
        public static final String DEFAULT_FETCH_SIZE = "50";
        public static final String DEFAULT_TIMEOUT = "30000"; // 30 seconds
        public static final String DEFAULT_CACHE_REGION = "query.cache";
    }

    /**
     * Performance monitoring and statistics configuration.
     * These settings help monitor query performance in development.
     */
    public static class PerformanceConfig {
        
        // Enable in development for query analysis
        public static final boolean SHOW_SQL = true;
        public static final boolean FORMAT_SQL = true;
        public static final boolean USE_SQL_COMMENTS = true;
        
        // Batch processing settings
        public static final int BATCH_SIZE = 25;
        public static final boolean ORDER_INSERTS = true;
        public static final boolean ORDER_UPDATES = true;
        
        // Connection pool settings
        public static final int MAX_POOL_SIZE = 20;
        public static final int MIN_IDLE = 5;
        public static final long IDLE_TIMEOUT = 300000; // 5 minutes
        public static final long MAX_LIFETIME = 1200000; // 20 minutes
    }

    /**
     * Index recommendations for optimal query performance.
     * These are implemented in DatabaseOptimizationConfig.
     */
    public static class IndexRecommendations {
        
        // Product table indexes
        public static final String PRODUCT_CATEGORY_ACTIVE = "idx_product_category_active";
        public static final String PRODUCT_PRICE_ACTIVE = "idx_product_price_active";
        public static final String PRODUCT_FEATURED_ACTIVE = "idx_product_featured_active";
        public static final String PRODUCT_STOCK_ACTIVE = "idx_product_stock_active";
        
        // Order table indexes
        public static final String ORDER_USER_DATE = "idx_order_user_date";
        public static final String ORDER_STATUS_DATE = "idx_order_status_date";
        
        // Cart table indexes
        public static final String CART_USER_DATE = "idx_cart_user_date";
        
        // User table indexes (already exist)
        public static final String USER_EMAIL = "idx_user_email";
        public static final String USER_USERNAME = "idx_user_username";
    }

    /**
     * Query optimization strategies and best practices.
     */
    public static class OptimizationStrategies {
        
        /**
         * Use pagination for large result sets to avoid memory issues.
         */
        public static final String USE_PAGINATION = "Always use Pageable for large datasets";
        
        /**
         * Use specific field selection instead of SELECT * when possible.
         */
        public static final String SELECT_SPECIFIC_FIELDS = "Use projection queries for specific fields";
        
        /**
         * Use JOIN FETCH for eager loading of associations when needed.
         */
        public static final String USE_JOIN_FETCH = "Use JOIN FETCH to avoid N+1 queries";
        
        /**
         * Use batch processing for bulk operations.
         */
        public static final String USE_BATCH_PROCESSING = "Use batch processing for bulk inserts/updates";
        
        /**
         * Cache frequently accessed read-only data.
         */
        public static final String CACHE_READ_ONLY_DATA = "Cache categories, featured products, etc.";
        
        /**
         * Use database-specific optimizations when possible.
         */
        public static final String DATABASE_SPECIFIC_OPTS = "Use database-specific query hints and features";
    }
}