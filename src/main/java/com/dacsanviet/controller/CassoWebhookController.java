package com.dacsanviet.controller;

import com.dacsanviet.service.OrderService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Webhook controller for Casso payment verification
 * Casso will call this endpoint when there's a new bank transaction
 */
@RestController
@RequestMapping("/payment/casso")
public class CassoWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(CassoWebhookController.class);

    @Autowired
    private OrderService orderService;

    @Value("${casso.webhook.secret:}")
    private String webhookSecret;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Webhook endpoint for Casso
     * Casso will POST transaction data here
     */
    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> handleWebhook(@RequestBody String payload) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("Received Casso webhook: {}", payload);
            
            // Parse JSON payload
            JsonNode rootNode = objectMapper.readTree(payload);
            
            // Check error code
            int error = rootNode.path("error").asInt(-1);
            if (error != 0) {
                logger.error("Casso webhook error: {}", error);
                response.put("success", false);
                response.put("message", "Invalid webhook data");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Get transactions array
            JsonNode dataNode = rootNode.path("data");
            if (!dataNode.isArray() || dataNode.size() == 0) {
                logger.warn("No transactions in webhook");
                response.put("success", true);
                response.put("message", "No transactions to process");
                return ResponseEntity.ok(response);
            }
            
            // Process each transaction
            int processedCount = 0;
            for (JsonNode transaction : dataNode) {
                if (processTransaction(transaction)) {
                    processedCount++;
                }
            }
            
            response.put("success", true);
            response.put("processed", processedCount);
            response.put("message", "Processed " + processedCount + " transactions");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error processing Casso webhook", e);
            response.put("success", false);
            response.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Process a single transaction
     */
    private boolean processTransaction(JsonNode transaction) {
        try {
            // Extract transaction data
            long cassoId = transaction.path("id").asLong();
            String tid = transaction.path("tid").asText();
            String description = transaction.path("description").asText();
            long amount = transaction.path("amount").asLong();
            String when = transaction.path("when").asText();
            String bankName = transaction.path("bankName").asText();
            
            logger.info("Processing transaction - ID: {}, Amount: {}, Description: {}", 
                       cassoId, amount, description);
            
            // Extract order ID from description
            // Expected format: "DH123" or "Don hang DH123" or "DH 123"
            Long orderId = extractOrderId(description);
            
            if (orderId == null) {
                logger.warn("Could not extract order ID from description: {}", description);
                return false;
            }
            
            logger.info("Extracted order ID: {}", orderId);
            
            // Verify and update order payment status
            boolean updated = orderService.verifyAndUpdatePayment(
                orderId, 
                amount, 
                "BANK_TRANSFER", 
                tid,
                description
            );
            
            if (updated) {
                logger.info("Successfully updated order {} payment status", orderId);
                return true;
            } else {
                logger.warn("Failed to update order {} - order not found or amount mismatch", orderId);
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Error processing transaction", e);
            return false;
        }
    }

    /**
     * Extract order ID from transaction description
     * Supports formats: DH123, DH 123, Don hang DH123, etc.
     */
    private Long extractOrderId(String description) {
        if (description == null || description.isEmpty()) {
            return null;
        }
        
        // Pattern to match DH followed by numbers
        Pattern pattern = Pattern.compile("DH\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(description);
        
        if (matcher.find()) {
            try {
                return Long.parseLong(matcher.group(1));
            } catch (NumberFormatException e) {
                logger.error("Invalid order ID format: {}", matcher.group(1));
                return null;
            }
        }
        
        return null;
    }

    /**
     * Test endpoint to verify webhook is working
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testWebhook() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Casso webhook endpoint is working");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}
