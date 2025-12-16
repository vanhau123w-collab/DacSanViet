package com.specialtyfood.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Database optimization configuration for performance improvements.
 * Handles database-specific optimizations and query performance tuning.
 */
@Configuration
@EnableTransactionManagement
public class DatabaseOptimizationConfig {

    /**
     * Database initialization for MySQL-specific optimizations.
     * This bean runs only when using MySQL database.
     */
    @Bean
    @ConditionalOnProperty(name = "spring.datasource.driver-class-name", havingValue = "com.mysql.cj.jdbc.Driver")
    public DatabaseInitializer mysqlDatabaseInitializer(DataSource dataSource) {
        return new DatabaseInitializer(dataSource, DatabaseType.MYSQL);
    }

    /**
     * Database initialization for H2-specific optimizations.
     * This bean runs only when using H2 database (development/testing).
     */
    @Bean
    @ConditionalOnProperty(name = "spring.datasource.driver-class-name", havingValue = "org.h2.Driver")
    public DatabaseInitializer h2DatabaseInitializer(DataSource dataSource) {
        return new DatabaseInitializer(dataSource, DatabaseType.H2);
    }

    /**
     * Database initializer class that applies database-specific optimizations.
     */
    public static class DatabaseInitializer {
        private final DataSource dataSource;
        private final DatabaseType databaseType;

        public DatabaseInitializer(DataSource dataSource, DatabaseType databaseType) {
            this.dataSource = dataSource;
            this.databaseType = databaseType;
            initializeDatabase();
        }

        private void initializeDatabase() {
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {

                switch (databaseType) {
                    case MYSQL:
                        applyMySQLOptimizations(statement);
                        break;
                    case H2:
                        applyH2Optimizations(statement);
                        break;
                }

            } catch (SQLException e) {
                // Log warning but don't fail application startup
                System.err.println("Warning: Could not apply database optimizations: " + e.getMessage());
            }
        }

        private void applyMySQLOptimizations(Statement statement) throws SQLException {
            // Create additional indexes for frequently queried columns if they don't exist
            try {
                // Composite index for product search with category and active status
                statement.execute(
                    "CREATE INDEX IF NOT EXISTS idx_product_category_active " +
                    "ON products (category_id, is_active, name)"
                );

                // Composite index for product search with price range
                statement.execute(
                    "CREATE INDEX IF NOT EXISTS idx_product_price_active " +
                    "ON products (is_active, price, name)"
                );

                // Index for featured products
                statement.execute(
                    "CREATE INDEX IF NOT EXISTS idx_product_featured_active " +
                    "ON products (is_featured, is_active, created_at)"
                );

                // Composite index for order search by user and date
                statement.execute(
                    "CREATE INDEX IF NOT EXISTS idx_order_user_date " +
                    "ON orders (user_id, order_date DESC)"
                );

                // Index for order search by status and date
                statement.execute(
                    "CREATE INDEX IF NOT EXISTS idx_order_status_date " +
                    "ON orders (status, order_date DESC)"
                );

                // Composite index for cart items by user and date
                statement.execute(
                    "CREATE INDEX IF NOT EXISTS idx_cart_user_date " +
                    "ON cart_items (user_id, added_date DESC)"
                );

                System.out.println("MySQL database optimizations applied successfully");

            } catch (SQLException e) {
                // Indexes might already exist, which is fine
                System.out.println("Some MySQL optimizations were skipped (likely already exist): " + e.getMessage());
            }
        }

        private void applyH2Optimizations(Statement statement) throws SQLException {
            // H2-specific optimizations for development/testing
            try {
                // Similar indexes for H2, but with H2-specific syntax
                statement.execute(
                    "CREATE INDEX IF NOT EXISTS idx_product_category_active " +
                    "ON products (category_id, is_active, name)"
                );

                statement.execute(
                    "CREATE INDEX IF NOT EXISTS idx_product_price_active " +
                    "ON products (is_active, price, name)"
                );

                statement.execute(
                    "CREATE INDEX IF NOT EXISTS idx_product_featured_active " +
                    "ON products (is_featured, is_active, created_at)"
                );

                statement.execute(
                    "CREATE INDEX IF NOT EXISTS idx_order_user_date " +
                    "ON orders (user_id, order_date DESC)"
                );

                statement.execute(
                    "CREATE INDEX IF NOT EXISTS idx_order_status_date " +
                    "ON orders (status, order_date DESC)"
                );

                statement.execute(
                    "CREATE INDEX IF NOT EXISTS idx_cart_user_date " +
                    "ON cart_items (user_id, added_date DESC)"
                );

                System.out.println("H2 database optimizations applied successfully");

            } catch (SQLException e) {
                System.out.println("Some H2 optimizations were skipped: " + e.getMessage());
            }
        }
    }

    /**
     * Enum to identify database type for applying specific optimizations.
     */
    public enum DatabaseType {
        MYSQL, H2
    }
}