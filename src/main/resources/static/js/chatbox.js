/**
 * Chatbox functionality for Đặc Sản Việt
 * Real-time chat with WebSocket support and REST API fallback
 */
class ChatBox {
    constructor() {
        this.sessionId = null;
        this.stompClient = null;
        this.isConnected = false;
        this.messageQueue = [];
        this.isMinimized = true;
        this.unreadCount = 0;
        
        this.init();
    }
    
    init() {
        this.createChatboxHTML();
        this.bindEvents();
        this.loadSessionFromStorage();
        this.connectWebSocket();
    }
    
    createChatboxHTML() {
        const chatboxHTML = `
            <div id="chatbox-container" class="chatbox-container">
                <!-- Chat Toggle Button -->
                <div id="chat-toggle" class="chat-toggle">
                    <i class="fas fa-comments"></i>
                    <span id="chat-badge" class="chat-badge" style="display: none;">0</span>
                </div>
                
                <!-- Chat Window -->
                <div id="chat-window" class="chat-window" style="display: none;">
                    <!-- Chat Header -->
                    <div class="chat-header">
                        <div class="chat-header-info">
                            <i class="fas fa-headset me-2"></i>
                            <span>Hỗ trợ trực tuyến</span>
                        </div>
                        <div class="chat-header-actions">
                            <button id="chat-minimize" class="chat-action-btn">
                                <i class="fas fa-minus"></i>
                            </button>
                        </div>
                    </div>
                    
                    <!-- Chat Messages -->
                    <div id="chat-messages" class="chat-messages">
                        <div class="chat-loading">
                            <i class="fas fa-spinner fa-spin"></i>
                            <span>Đang kết nối...</span>
                        </div>
                    </div>
                    
                    <!-- Chat Input -->
                    <div class="chat-input-container">
                        <div id="chat-user-info" class="chat-user-info" style="display: none;">
                            <input type="text" id="chat-name" placeholder="Tên của bạn" class="form-control form-control-sm mb-2">
                            <input type="email" id="chat-email" placeholder="Email (tùy chọn)" class="form-control form-control-sm mb-2">
                        </div>
                        <div class="chat-input-wrapper">
                            <input type="text" id="chat-input" placeholder="Nhập tin nhắn..." class="chat-input" disabled>
                            <button id="chat-send" class="chat-send-btn" disabled>
                                <i class="fas fa-paper-plane"></i>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;
        
        document.body.insertAdjacentHTML('beforeend', chatboxHTML);
        this.addChatboxStyles();
    }
    
    addChatboxStyles() {
        const styles = `
            <style>
                .chatbox-container {
                    position: fixed;
                    bottom: 20px;
                    right: 20px;
                    z-index: 9999;
                    font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
                }
                
                .chat-toggle {
                    width: 60px;
                    height: 60px;
                    background: linear-gradient(135deg, #D2691E 0%, #8B4513 100%);
                    border-radius: 50%;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    color: white;
                    font-size: 24px;
                    cursor: pointer;
                    box-shadow: 0 4px 20px rgba(210, 105, 30, 0.4);
                    transition: all 0.3s ease;
                    position: relative;
                }
                
                .chat-toggle:hover {
                    transform: scale(1.1);
                    box-shadow: 0 6px 25px rgba(210, 105, 30, 0.6);
                }
                
                .chat-badge {
                    position: absolute;
                    top: -5px;
                    right: -5px;
                    background: #e74c3c;
                    color: white;
                    border-radius: 50%;
                    width: 24px;
                    height: 24px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    font-size: 12px;
                    font-weight: bold;
                    animation: pulse 2s infinite;
                }
                
                @keyframes pulse {
                    0% { transform: scale(1); }
                    50% { transform: scale(1.1); }
                    100% { transform: scale(1); }
                }
                
                .chat-window {
                    position: absolute;
                    bottom: 80px;
                    right: 0;
                    width: 350px;
                    height: 500px;
                    background: white;
                    border-radius: 15px;
                    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
                    display: flex;
                    flex-direction: column;
                    overflow: hidden;
                    animation: slideUp 0.3s ease;
                }
                
                @keyframes slideUp {
                    from {
                        opacity: 0;
                        transform: translateY(20px);
                    }
                    to {
                        opacity: 1;
                        transform: translateY(0);
                    }
                }
                
                .chat-header {
                    background: linear-gradient(135deg, #D2691E 0%, #8B4513 100%);
                    color: white;
                    padding: 15px 20px;
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                }
                
                .chat-header-info {
                    display: flex;
                    align-items: center;
                    font-weight: 600;
                }
                
                .chat-action-btn {
                    background: none;
                    border: none;
                    color: white;
                    font-size: 16px;
                    cursor: pointer;
                    padding: 5px;
                    border-radius: 3px;
                    transition: background 0.2s ease;
                }
                
                .chat-action-btn:hover {
                    background: rgba(255, 255, 255, 0.2);
                }
                
                .chat-messages {
                    flex: 1;
                    padding: 20px;
                    overflow-y: auto;
                    background: #f8f9fa;
                }
                
                .chat-loading {
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    color: #666;
                    font-size: 14px;
                    gap: 10px;
                }
                
                .chat-message {
                    margin-bottom: 15px;
                    animation: fadeInUp 0.3s ease;
                }
                
                @keyframes fadeInUp {
                    from {
                        opacity: 0;
                        transform: translateY(10px);
                    }
                    to {
                        opacity: 1;
                        transform: translateY(0);
                    }
                }
                
                .message-user {
                    text-align: right;
                }
                
                .message-admin, .message-system {
                    text-align: left;
                }
                
                .message-bubble {
                    display: inline-block;
                    max-width: 80%;
                    padding: 12px 16px;
                    border-radius: 18px;
                    font-size: 14px;
                    line-height: 1.4;
                    word-wrap: break-word;
                }
                
                .message-user .message-bubble {
                    background: linear-gradient(135deg, #D2691E 0%, #8B4513 100%);
                    color: white;
                }
                
                .message-admin .message-bubble {
                    background: white;
                    color: #333;
                    border: 1px solid #e0e0e0;
                }
                
                .message-system .message-bubble {
                    background: #e3f2fd;
                    color: #1976d2;
                    font-style: italic;
                }
                
                .message-time {
                    font-size: 11px;
                    color: #999;
                    margin-top: 5px;
                }
                
                .chat-user-info {
                    padding: 15px 20px;
                    background: #fff3e0;
                    border-bottom: 1px solid #e0e0e0;
                }
                
                .chat-input-container {
                    background: white;
                    border-top: 1px solid #e0e0e0;
                }
                
                .chat-input-wrapper {
                    display: flex;
                    padding: 15px 20px;
                    gap: 10px;
                }
                
                .chat-input {
                    flex: 1;
                    border: 1px solid #e0e0e0;
                    border-radius: 25px;
                    padding: 12px 16px;
                    font-size: 14px;
                    outline: none;
                    transition: border-color 0.2s ease;
                }
                
                .chat-input:focus {
                    border-color: #D2691E;
                }
                
                .chat-input:disabled {
                    background: #f5f5f5;
                    color: #999;
                }
                
                .chat-send-btn {
                    width: 40px;
                    height: 40px;
                    background: linear-gradient(135deg, #D2691E 0%, #8B4513 100%);
                    border: none;
                    border-radius: 50%;
                    color: white;
                    cursor: pointer;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    transition: all 0.2s ease;
                }
                
                .chat-send-btn:hover:not(:disabled) {
                    transform: scale(1.1);
                }
                
                .chat-send-btn:disabled {
                    background: #ccc;
                    cursor: not-allowed;
                    transform: none;
                }
                
                /* Mobile responsive */
                @media (max-width: 480px) {
                    .chat-window {
                        width: calc(100vw - 40px);
                        height: 400px;
                        bottom: 80px;
                        right: 20px;
                    }
                    
                    .chatbox-container {
                        right: 20px;
                        bottom: 20px;
                    }
                }
            </style>
        `;
        
        document.head.insertAdjacentHTML('beforeend', styles);
    }
    
    bindEvents() {
        // Toggle chat window
        document.getElementById('chat-toggle').addEventListener('click', () => {
            this.toggleChat();
        });
        
        // Minimize chat
        document.getElementById('chat-minimize').addEventListener('click', () => {
            this.minimizeChat();
        });
        
        // Send message on Enter key
        document.getElementById('chat-input').addEventListener('keypress', (e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                this.sendMessage();
            }
        });
        
        // Send message on button click
        document.getElementById('chat-send').addEventListener('click', () => {
            this.sendMessage();
        });
        
        // Auto-resize input
        document.getElementById('chat-input').addEventListener('input', (e) => {
            // Auto-expand textarea functionality could be added here
        });
    }
    
    toggleChat() {
        const chatWindow = document.getElementById('chat-window');
        const isVisible = chatWindow.style.display !== 'none';
        
        if (isVisible) {
            this.minimizeChat();
        } else {
            this.maximizeChat();
        }
    }
    
    minimizeChat() {
        document.getElementById('chat-window').style.display = 'none';
        this.isMinimized = true;
    }
    
    maximizeChat() {
        document.getElementById('chat-window').style.display = 'block';
        this.isMinimized = false;
        this.clearUnreadBadge();
        
        // Initialize session if needed
        if (!this.sessionId) {
            this.initializeSession();
        }
        
        // Focus input
        setTimeout(() => {
            const input = document.getElementById('chat-input');
            if (input && !input.disabled) {
                input.focus();
            }
        }, 100);
    }
    
    async initializeSession() {
        try {
            const response = await fetch('/api/chat/init', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            });
            
            if (response.ok) {
                const data = await response.json();
                this.sessionId = data.sessionId;
                this.saveSessionToStorage();
                this.loadChatHistory();
                this.enableInput();
                this.showUserInfoForm();
            } else {
                this.showError('Không thể khởi tạo phiên chat');
            }
        } catch (error) {
            console.error('Error initializing chat:', error);
            this.showError('Lỗi kết nối. Vui lòng thử lại sau.');
        }
    }
    
    async loadChatHistory() {
        if (!this.sessionId) return;
        
        try {
            const response = await fetch(`/api/chat/history/${this.sessionId}`);
            if (response.ok) {
                const messages = await response.json();
                this.displayMessages(messages);
            }
        } catch (error) {
            console.error('Error loading chat history:', error);
        }
    }
    
    displayMessages(messages) {
        const messagesContainer = document.getElementById('chat-messages');
        messagesContainer.innerHTML = '';
        
        messages.forEach(message => {
            this.addMessageToUI(message);
        });
        
        this.scrollToBottom();
    }
    
    addMessageToUI(message) {
        const messagesContainer = document.getElementById('chat-messages');
        const messageDiv = document.createElement('div');
        messageDiv.className = `chat-message message-${message.messageType.toLowerCase()}`;
        
        const time = new Date(message.createdAt).toLocaleTimeString('vi-VN', {
            hour: '2-digit',
            minute: '2-digit'
        });
        
        let senderName = '';
        if (message.messageType === 'USER' && message.senderName) {
            senderName = message.senderName;
        } else if (message.messageType === 'ADMIN') {
            senderName = 'Hỗ trợ viên';
        }
        
        messageDiv.innerHTML = `
            <div class="message-bubble">
                ${message.message}
            </div>
            <div class="message-time">
                ${senderName ? senderName + ' • ' : ''}${time}
            </div>
        `;
        
        messagesContainer.appendChild(messageDiv);
        this.scrollToBottom();
    }
    
    scrollToBottom() {
        const messagesContainer = document.getElementById('chat-messages');
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }
    
    showUserInfoForm() {
        const userInfo = this.getUserInfoFromStorage();
        if (!userInfo.name) {
            document.getElementById('chat-user-info').style.display = 'block';
            document.getElementById('chat-name').focus();
        } else {
            document.getElementById('chat-name').value = userInfo.name;
            document.getElementById('chat-email').value = userInfo.email || '';
        }
    }
    
    enableInput() {
        document.getElementById('chat-input').disabled = false;
        document.getElementById('chat-send').disabled = false;
        document.querySelector('.chat-loading').style.display = 'none';
    }
    
    async sendMessage() {
        const input = document.getElementById('chat-input');
        const message = input.value.trim();
        
        if (!message || !this.sessionId) return;
        
        // Get user info
        const name = document.getElementById('chat-name').value.trim();
        const email = document.getElementById('chat-email').value.trim();
        
        // Save user info
        if (name) {
            this.saveUserInfoToStorage(name, email);
            document.getElementById('chat-user-info').style.display = 'none';
        }
        
        // Clear input
        input.value = '';
        
        // Create message object
        const messageData = {
            sessionId: this.sessionId,
            senderName: name,
            senderEmail: email,
            message: message
        };
        
        // Add to UI immediately
        this.addMessageToUI({
            messageType: 'USER',
            message: message,
            senderName: name,
            createdAt: new Date().toISOString()
        });
        
        // Send via WebSocket if connected, otherwise use REST API
        if (this.isConnected && this.stompClient) {
            this.stompClient.send('/app/chat.sendMessage', {}, JSON.stringify(messageData));
        } else {
            this.sendMessageViaAPI(messageData);
        }
    }
    
    async sendMessageViaAPI(messageData) {
        try {
            const response = await fetch('/api/chat/send', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(messageData)
            });
            
            if (!response.ok) {
                this.showError('Không thể gửi tin nhắn. Vui lòng thử lại.');
            }
        } catch (error) {
            console.error('Error sending message:', error);
            this.showError('Lỗi kết nối. Vui lòng thử lại sau.');
        }
    }
    
    connectWebSocket() {
        if (typeof SockJS === 'undefined' || typeof Stomp === 'undefined') {
            console.warn('SockJS or Stomp not loaded, using REST API fallback');
            return;
        }
        
        try {
            const socket = new SockJS('/ws');
            this.stompClient = Stomp.over(socket);
            
            // Disable debug logging
            this.stompClient.debug = null;
            
            this.stompClient.connect({}, 
                () => this.onWebSocketConnected(),
                (error) => this.onWebSocketError(error)
            );
        } catch (error) {
            console.error('WebSocket connection error:', error);
        }
    }
    
    onWebSocketConnected() {
        this.isConnected = true;
        console.log('WebSocket connected');
        
        // Subscribe to session-specific messages
        if (this.sessionId) {
            this.stompClient.subscribe(`/topic/chat/${this.sessionId}`, (message) => {
                const chatMessage = JSON.parse(message.body);
                this.addMessageToUI(chatMessage);
                
                if (this.isMinimized) {
                    this.incrementUnreadBadge();
                }
            });
        }
    }
    
    onWebSocketError(error) {
        console.error('WebSocket error:', error);
        this.isConnected = false;
    }
    
    showError(message) {
        this.addMessageToUI({
            messageType: 'SYSTEM',
            message: `❌ ${message}`,
            createdAt: new Date().toISOString()
        });
    }
    
    incrementUnreadBadge() {
        this.unreadCount++;
        const badge = document.getElementById('chat-badge');
        badge.textContent = this.unreadCount;
        badge.style.display = 'flex';
    }
    
    clearUnreadBadge() {
        this.unreadCount = 0;
        document.getElementById('chat-badge').style.display = 'none';
    }
    
    saveSessionToStorage() {
        if (this.sessionId) {
            localStorage.setItem('chatSessionId', this.sessionId);
        }
    }
    
    loadSessionFromStorage() {
        this.sessionId = localStorage.getItem('chatSessionId');
    }
    
    saveUserInfoToStorage(name, email) {
        localStorage.setItem('chatUserInfo', JSON.stringify({ name, email }));
    }
    
    getUserInfoFromStorage() {
        try {
            const info = localStorage.getItem('chatUserInfo');
            return info ? JSON.parse(info) : { name: '', email: '' };
        } catch {
            return { name: '', email: '' };
        }
    }
}

// Initialize chatbox when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    // Only initialize if not in admin area
    if (!window.location.pathname.startsWith('/admin')) {
        window.chatbox = new ChatBox();
    }
});