package com.specialtyfood.controller;

import com.specialtyfood.dto.NotificationDto;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Controller for handling WebSocket notifications
 */
@Controller
public class NotificationController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Handle subscription to notifications
     * Clients can send messages to /app/subscribe to receive notifications
     */
    @MessageMapping("/subscribe")
    @SendTo("/topic/notifications")
    public NotificationDto subscribe(NotificationDto notification) {
        // Echo back subscription confirmation
        return new NotificationDto(
            "SYSTEM",
            "Successfully subscribed to notifications",
            System.currentTimeMillis()
        );
    }

    /**
     * Send notification to all subscribers
     */
    public void sendGlobalNotification(String message) {
        NotificationDto notification = new NotificationDto(
            "SYSTEM",
            message,
            System.currentTimeMillis()
        );
        messagingTemplate.convertAndSend("/topic/notifications", notification);
    }

    /**
     * Send notification to specific user
     */
    public void sendUserNotification(String username, String message) {
        NotificationDto notification = new NotificationDto(
            "PERSONAL",
            message,
            System.currentTimeMillis()
        );
        messagingTemplate.convertAndSendToUser(username, "/queue/notifications", notification);
    }

    /**
     * Send notification to admin users
     */
    public void sendAdminNotification(String message) {
        NotificationDto notification = new NotificationDto(
            "ADMIN",
            message,
            System.currentTimeMillis()
        );
        messagingTemplate.convertAndSend("/topic/admin-notifications", notification);
    }

    /**
     * Send order status notification
     */
    public void sendOrderStatusNotification(String username, String orderNumber, String status, String message) {
        NotificationDto notification = new NotificationDto(
            "ORDER_STATUS",
            String.format("Order %s: %s - %s", orderNumber, status, message),
            System.currentTimeMillis()
        );
        messagingTemplate.convertAndSendToUser(username, "/queue/order-updates", notification);
    }

    /**
     * Send inventory alert to admins
     */
    public void sendInventoryAlert(String productName, int stockQuantity) {
        String message = String.format("Low stock alert: %s has only %d units remaining", productName, stockQuantity);
        NotificationDto notification = new NotificationDto(
            "INVENTORY_ALERT",
            message,
            System.currentTimeMillis()
        );
        messagingTemplate.convertAndSend("/topic/inventory-alerts", notification);
    }

    /**
     * Send payment confirmation
     */
    public void sendPaymentConfirmation(String username, String orderNumber, String amount) {
        String message = String.format("Payment confirmed for order %s. Amount: %s VND", orderNumber, amount);
        NotificationDto notification = new NotificationDto(
            "PAYMENT_CONFIRMATION",
            message,
            System.currentTimeMillis()
        );
        messagingTemplate.convertAndSendToUser(username, "/queue/payment-confirmations", notification);
    }

    /**
     * Send maintenance notification to all users
     */
    public void sendMaintenanceNotification(String message) {
        NotificationDto notification = new NotificationDto(
            "MAINTENANCE",
            message,
            System.currentTimeMillis()
        );
        messagingTemplate.convertAndSend("/topic/maintenance", notification);
    }
}