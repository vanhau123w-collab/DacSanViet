package com.specialtyfood.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Database configuration
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.specialtyfood.repository")
@EnableTransactionManagement
public class DatabaseConfig {
    // Database configuration will be handled by Spring Boot auto-configuration
    // This class serves as a placeholder for any custom database configurations
}