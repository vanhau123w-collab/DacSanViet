// WebSocket notification client
class NotificationClient {
    constructor() {
        this.stompClient = null;
        this.connected = false;
    }

    connect() {
        const socket = new SockJS('/ws');
        this.stompClient = Stomp.over(socket);
        
        const self = this;
        this.stompClient.connect({}, function (frame) {
            console.log('Connected: ' + frame);
            self.connected = true;
            self.subscribeToNotifications();
        }, function (error) {
            console.log('Connection error: ' + error);
            self.connected = false;
        });
    }

    subscribeToNotifications() {
        // Subscribe to global notifications
        this.stompClient.subscribe('/topic/notifications', function (notification) {
            const message = JSON.parse(notification.body);
            self.displayNotification(message, 'global');
        });

        // Subscribe to admin notifications (if user is admin)
        this.stompClient.subscribe('/topic/admin-notifications', function (notification) {
            const message = JSON.parse(notification.body);
            self.displayNotification(message, 'admin');
        });

        // Subscribe to inventory alerts (for admins)
        this.stompClient.subscribe('/topic/inventory-alerts', function (notification) {
            const message = JSON.parse(notification.body);
            self.displayNotification(message, 'inventory');
        });

        // Subscribe to maintenance notifications
        this.stompClient.subscribe('/topic/maintenance', function (notification) {
            const message = JSON.parse(notification.body);
            self.displayNotification(message, 'maintenance');
        });

        // Subscribe to personal notifications (requires authentication)
        this.stompClient.subscribe('/user/queue/notifications', function (notification) {
            const message = JSON.parse(notification.body);
            self.displayNotification(message, 'personal');
        });

        // Subscribe to order updates
        this.stompClient.subscribe('/user/queue/order-updates', function (notification) {
            const message = JSON.parse(notification.body);
            self.displayNotification(message, 'order');
        });

        // Subscribe to payment confirmations
        this.stompClient.subscribe('/user/queue/payment-confirmations', function (notification) {
            const message = JSON.parse(notification.body);
            self.displayNotification(message, 'payment');
        });
    }

    displayNotification(message, category) {
        console.log('Received notification:', message);
        
        // Create notification element
        const notificationDiv = document.createElement('div');
        notificationDiv.className = `notification ${category}`;
        notificationDiv.innerHTML = `
            <div class="notification-header">
                <span class="notification-type">${message.type}</span>
                <span class="notification-time">${message.dateTime}</span>
            </div>
            <div class="notification-message">${message.message}</div>
        `;

        // Add to notification container
        const container = document.getElementById('notifications-container');
        if (container) {
            container.insertBefore(notificationDiv, container.firstChild);
            
            // Auto-remove after 10 seconds
            setTimeout(() => {
                if (notificationDiv.parentNode) {
                    notificationDiv.parentNode.removeChild(notificationDiv);
                }
            }, 10000);
        }

        // Show browser notification if supported
        if (Notification.permission === 'granted') {
            new Notification(message.type, {
                body: message.message,
                icon: '/favicon.ico'
            });
        }
    }

    sendTestNotification() {
        if (this.connected && this.stompClient) {
            this.stompClient.send('/app/subscribe', {}, JSON.stringify({
                type: 'TEST',
                message: 'Test notification from client',
                timestamp: Date.now()
            }));
        }
    }

    disconnect() {
        if (this.stompClient !== null) {
            this.stompClient.disconnect();
            this.connected = false;
        }
        console.log('Disconnected');
    }
}

// Initialize notification client when page loads
let notificationClient;

document.addEventListener('DOMContentLoaded', function() {
    // Request notification permission
    if ('Notification' in window && Notification.permission === 'default') {
        Notification.requestPermission();
    }

    // Initialize WebSocket connection
    notificationClient = new NotificationClient();
    notificationClient.connect();

    // Add test button functionality
    const testButton = document.getElementById('test-notification-btn');
    if (testButton) {
        testButton.addEventListener('click', function() {
            notificationClient.sendTestNotification();
        });
    }
});

// Disconnect when page unloads
window.addEventListener('beforeunload', function() {
    if (notificationClient) {
        notificationClient.disconnect();
    }
});