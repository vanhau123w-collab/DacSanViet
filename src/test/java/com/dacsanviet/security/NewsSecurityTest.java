package com.dacsanviet.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test security configuration for news management endpoints
 * Validates: Requirements 1.5 (Security and validation)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class NewsSecurityTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    /**
     * Test that admin endpoints require authentication
     */
    @Test
    public void testAdminEndpointsRequireAuth() throws Exception {
        mockMvc.perform(get("/api/admin/news"))
                .andExpect(status().isUnauthorized());
    }
    
    /**
     * Test that admin endpoints allow ADMIN role
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    public void testAdminEndpointsAllowAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/news"))
                .andExpect(status().isOk());
    }
    
    /**
     * Test that admin endpoints allow STAFF role
     */
    @Test
    @WithMockUser(roles = "STAFF")
    public void testAdminEndpointsAllowStaff() throws Exception {
        mockMvc.perform(get("/api/admin/news"))
                .andExpect(status().isOk());
    }
    
    /**
     * Test that admin endpoints deny USER role
     */
    @Test
    @WithMockUser(roles = "USER")
    public void testAdminEndpointsDenyUser() throws Exception {
        mockMvc.perform(get("/api/admin/news"))
                .andExpect(status().isForbidden());
    }
    
    /**
     * Test that public news endpoints are accessible
     */
    @Test
    public void testPublicNewsEndpointsAccessible() throws Exception {
        mockMvc.perform(get("/news"))
                .andExpect(status().isOk());
    }
    
    /**
     * Test CSRF token endpoint is accessible
     */
    @Test
    public void testCsrfTokenEndpointAccessible() throws Exception {
        mockMvc.perform(get("/api/csrf/token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }
}