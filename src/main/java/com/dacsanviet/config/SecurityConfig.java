package com.dacsanviet.config;

import com.dacsanviet.security.CustomAuthenticationSuccessHandler;
import com.dacsanviet.security.JwtAuthenticationEntryPoint;
import com.dacsanviet.security.JwtAuthenticationFilter;
import com.dacsanviet.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security Configuration with JWT
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    
    @Autowired
    private JwtAuthenticationEntryPoint unauthorizedHandler;
    
    @Autowired
    private CustomAuthenticationSuccessHandler authenticationSuccessHandler;
    
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
    
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf
                // Enable CSRF for form-based endpoints, but ignore specific API endpoints
                .ignoringRequestMatchers("/api/auth/**", "/api/csrf/**", "/api/payment/**", "/h2-console/**", "/ws/**", 
                                       "/api/admin/news/*/update-debug", "/api/admin/news/*/update-simple", "/api/admin/news/*/update",
                                       "/api/admin/news/*", "/api/checkout/**", "/api/cart/**", "/checkout/process") // Allow checkout process without CSRF
                .csrfTokenRepository(org.springframework.security.web.csrf.CookieCsrfTokenRepository.withHttpOnlyFalse())
            )
            .headers(headers -> headers
                .frameOptions().sameOrigin() // Allow H2 console frames
                .contentTypeOptions().and()
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true)
                )
                .and()
            )
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/about", "/contact", "/news", "/news/**", "/test", "/test-simple").permitAll()
                .requestMatchers("/privacy-policy", "/terms-of-service", "/shipping-policy").permitAll()
                .requestMatchers("/error/**").permitAll()
                .requestMatchers("/init-data", "/clear-test-data", "/check-conflicts", "/view-database", "/database").permitAll()
                .requestMatchers("/login", "/register").permitAll()
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/csrf/**").permitAll() // Allow CSRF token endpoint
                .requestMatchers("/admin/chat").permitAll() // Temporary for testing
                .requestMatchers("/chat-demo").permitAll() // Demo page
                

                
                // Static resources
                .requestMatchers("/css/**", "/js/**", "/images/**", "/uploads/**", "/favicon.ico", "/*.svg").permitAll()
                
                // Product browsing (require login)
                // Product browsing (public)
                .requestMatchers("/", "/home", "/products/**", "/categories/**", "/search/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                
                // Cart endpoints (allow guests)
                .requestMatchers("/cart/**", "/api/cart/**").permitAll()
                
                // Checkout endpoints (allow guests for guest checkout)
                .requestMatchers("/checkout", "/checkout/**").permitAll()
                .requestMatchers("/api/checkout/**").permitAll()
                
                // Payment endpoints (allow all for payment processing)
                .requestMatchers("/payment/**").permitAll()
                .requestMatchers("/api/payment/**").permitAll()
                
                // User-specific endpoints
                .requestMatchers("/profile/**", "/orders/**").hasAnyRole("USER", "ADMIN", "STAFF")
                .requestMatchers("/api/profile/**", "/api/orders/**").hasAnyRole("USER", "ADMIN", "STAFF")
                
                // Admin DELETE operations (ADMIN only) - must be before general admin rules
                .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/categories/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/admin/users/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/admin/news/**").hasAnyRole("ADMIN", "STAFF") // Allow STAFF to delete news
                .requestMatchers(HttpMethod.DELETE, "/admin/products/**").hasRole("ADMIN")
                
                // Admin endpoints (ADMIN and STAFF)
                .requestMatchers("/admin/**").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers("/api/admin/news/*/update-debug", "/api/admin/news/*/update-simple", "/api/admin/news/*/update").permitAll() // Temporary for testing
                .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(HttpMethod.POST, "/api/products/**").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(HttpMethod.PUT, "/api/products/**").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(HttpMethod.POST, "/api/categories/**").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(HttpMethod.PUT, "/api/categories/**").hasAnyRole("ADMIN", "STAFF")
                
                // WebSocket endpoints
                .requestMatchers("/ws/**").permitAll()
                
                // Chat endpoints (allow all for guest chat)
                .requestMatchers("/api/chat/**").permitAll()
                
                // All other requests require authentication
                .anyRequest().authenticated()
            );
        
        // Form login configuration
        http.formLogin(form -> form
            .loginPage("/login")
            .loginProcessingUrl("/login")
            .successHandler(authenticationSuccessHandler) // Use custom success handler to merge cart
            .failureUrl("/login?error=true")
            .usernameParameter("username")
            .passwordParameter("password")
            .permitAll()
        );
        
        // Logout configuration
        http.logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/")
            .invalidateHttpSession(true)
            .clearAuthentication(true)
            .deleteCookies("JSESSIONID", "JWT-TOKEN")
            .permitAll()
        );
        
        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}