// Admin Orders Management JavaScript
let currentPage = 1;
let pageSize = 10;
let totalPages = 1;
let currentFilters = {
    search: '',
    status: '',
    startDate: '',
    endDate: ''
};

// Initialize
document.addEventListener('DOMContentLoaded', function() {
    loadOrders();
    
    // Set default date range (last 30 days)
    const endDate = new Date();
    const startDate = new Date();
    startDate.setDate(startDate.getDate() - 30);
    
    document.getElementById('startDate').valueAsDate = startDate;
    document.getElementById('endDate').valueAsDate = endDate;
});

// Load Orders with pagination (AJAX - no page reload)
async function loadOrders() {
    try {
        const params = new URLSearchParams({
            page: currentPage - 1,
            size: pageSize,
            search: currentFilters.search,
            status: currentFilters.status,
            startDate: currentFilters.startDate,
            endDate: currentFilters.endDate
        });
        
        console.log('Loading orders with params:', params.toString());
        const response = await fetch(`/api/admin/orders?${params}`);
        console.log('Response status:', response.status);
        
        if (!response.ok) {
            console.error('Failed to load orders:', response.statusText);
            return;
        }
        
        const data = await response.json();
        console.log('Orders data:', data);
        
        // Filter out null orders
        if (data.content) {
            data.content = data.content.filter(order => order !== null && order !== undefined);
            console.log('Filtered orders:', data.content);
        }
        
        displayOrders(data.content);
        updatePagination(data);
        
    } catch (error) {
        console.error('Error loading orders:', error);
    }
}

// Display Orders in table
function displayOrders(orders) {
    const tbody = document.getElementById('ordersTableBody');
    
    console.log('Displaying orders:', orders);
    
    if (!orders || orders.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align: center; padding: 2rem;">Không tìm thấy đơn hàng</td></tr>';
        return;
    }
    
    tbody.innerHTML = orders.map(order => {
        try {
            return `
                <tr>
                    <td><strong>${order.orderNumber || 'N/A'}</strong></td>
                    <td>
                        <div>${order.customerName || order.userFullName || 'Khách'}</div>
                        <div style="font-size: 0.875rem; color: var(--text-secondary);">${order.customerEmail || order.userEmail || ''}</div>
                    </td>
                    <td>${order.orderDate ? formatDate(order.orderDate) : 'N/A'}</td>
                    <td><strong>${order.totalAmount ? formatCurrency(order.totalAmount) : '0₫'}</strong></td>
                    <td>${order.status ? getStatusBadge(order.status) : 'N/A'}</td>
                    <td>${order.paymentStatus ? getPaymentBadge(order.paymentStatus) : 'N/A'}</td>
                    <td>
                        <div class="action-buttons">
                            <button class="action-btn" onclick="viewOrder(${order.id})" title="Xem">
                                <i class="bi bi-eye"></i>
                            </button>
                            <button class="action-btn" onclick="updateOrderStatus(${order.id})" title="Cập Nhật">
                                <i class="bi bi-pencil"></i>
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        } catch (error) {
            console.error('Error rendering order:', order, error);
            return '';
        }
    }).filter(html => html).join('');
}

// Update Pagination
function updatePagination(data) {
    totalPages = data.totalPages;
    const totalOrders = data.totalElements;
    
    document.getElementById('showingStart').textContent = (currentPage - 1) * pageSize + 1;
    document.getElementById('showingEnd').textContent = Math.min(currentPage * pageSize, totalOrders);
    document.getElementById('totalOrders').textContent = totalOrders;
    
    const paginationButtons = document.getElementById('paginationButtons');
    let buttons = '';
    
    buttons += `<button class="page-btn" onclick="changePage(${currentPage - 1})" 
                ${currentPage === 1 ? 'disabled' : ''}>
                <i class="bi bi-chevron-left"></i>
                </button>`;
    
    for (let i = 1; i <= totalPages; i++) {
        if (i === 1 || i === totalPages || (i >= currentPage - 2 && i <= currentPage + 2)) {
            buttons += `<button class="page-btn ${i === currentPage ? 'active' : ''}" 
                        onclick="changePage(${i})">${i}</button>`;
        } else if (i === currentPage - 3 || i === currentPage + 3) {
            buttons += '<span style="padding: 0 0.5rem;">...</span>';
        }
    }
    
    buttons += `<button class="page-btn" onclick="changePage(${currentPage + 1})" 
                ${currentPage === totalPages ? 'disabled' : ''}>
                <i class="bi bi-chevron-right"></i>
                </button>`;
    
    paginationButtons.innerHTML = buttons;
}

// Change Page
function changePage(page) {
    if (page < 1 || page > totalPages) return;
    currentPage = page;
    loadOrders();
}

// Search Orders
let searchTimeout;
function searchOrders() {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
        currentFilters.search = document.getElementById('searchInput').value;
        currentPage = 1;
        loadOrders();
    }, 500);
}

// Filter Orders
function filterOrders() {
    currentFilters.status = document.getElementById('statusFilter').value;
    currentPage = 1;
    loadOrders();
}

// Apply Filters
function applyFilters() {
    currentFilters.startDate = document.getElementById('startDate').value;
    currentFilters.endDate = document.getElementById('endDate').value;
    currentPage = 1;
    loadOrders();
}

// View Order Details
async function viewOrder(id) {
    try {
        const response = await fetch(`/api/admin/orders/${id}`);
        
        if (!response.ok) {
            alert('Lỗi khi tải chi tiết đơn hàng: ' + response.status);
            return;
        }
        
        const order = await response.json();
        console.log('Order details:', order);
        
        // Handle missing orderItems
        const orderItems = order.orderItems || [];
        const shippingFee = order.shippingFee || 0;
        const subtotal = order.totalAmount - shippingFee;
        
        const content = `
            <div class="order-section">
                <h4>Thông Tin Đơn Hàng</h4>
                <div class="info-grid">
                    <div class="info-item">
                        <div class="info-label">Mã Đơn Hàng</div>
                        <div class="info-value">${order.orderNumber || 'N/A'}</div>
                    </div>
                    <div class="info-item">
                        <div class="info-label">Ngày Đặt</div>
                        <div class="info-value">${order.orderDate ? formatDate(order.orderDate) : 'N/A'}</div>
                    </div>
                    <div class="info-item">
                        <div class="info-label">Trạng Thái</div>
                        <div class="info-value">${order.status ? getStatusBadge(order.status) : 'N/A'}</div>
                    </div>
                    <div class="info-item">
                        <div class="info-label">Thanh Toán</div>
                        <div class="info-value">${order.paymentStatus ? getPaymentBadge(order.paymentStatus) : 'N/A'}</div>
                    </div>
                </div>
            </div>
            
            <div class="order-section">
                <h4>Thông Tin Khách Hàng</h4>
                <div class="info-grid">
                    <div class="info-item">
                        <div class="info-label">Tên</div>
                        <div class="info-value">${order.customerName || 'N/A'}</div>
                    </div>
                    <div class="info-item">
                        <div class="info-label">Email</div>
                        <div class="info-value">${order.customerEmail || 'N/A'}</div>
                    </div>
                    <div class="info-item">
                        <div class="info-label">Số Điện Thoại</div>
                        <div class="info-value">${order.customerPhone || 'N/A'}</div>
                    </div>
                    <div class="info-item">
                        <div class="info-label">Địa Chỉ Giao Hàng</div>
                        <div class="info-value">${order.shippingAddressText || 'N/A'}</div>
                    </div>
                </div>
            </div>
            
            ${orderItems.length > 0 ? `
            <div class="order-section">
                <h4>Sản Phẩm</h4>
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Sản Phẩm</th>
                            <th>Số Lượng</th>
                            <th>Đơn Giá</th>
                            <th>Tổng</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${orderItems.map(item => `
                            <tr>
                                <td>${item.productName || 'N/A'}</td>
                                <td>${item.quantity || 0}</td>
                                <td>${formatCurrency(item.unitPrice || 0)}</td>
                                <td><strong>${formatCurrency(item.totalPrice || 0)}</strong></td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            </div>
            ` : '<div class="order-section"><p>Không có thông tin sản phẩm</p></div>'}
            
            <div class="order-section">
                <h4>Tổng Kết</h4>
                <div class="info-grid">
                    <div class="info-item">
                        <div class="info-label">Tạm Tính</div>
                        <div class="info-value">${formatCurrency(subtotal)}</div>
                    </div>
                    <div class="info-item">
                        <div class="info-label">Phí Vận Chuyển</div>
                        <div class="info-value">${formatCurrency(shippingFee)}</div>
                    </div>
                    <div class="info-item" style="grid-column: span 2;">
                        <div class="info-label">Tổng Cộng</div>
                        <div class="info-value" style="font-size: 1.5rem; color: var(--primary-color);">
                            ${formatCurrency(order.totalAmount || 0)}
                        </div>
                    </div>
                </div>
            </div>
            
            <div style="display: flex; gap: 1rem; justify-content: flex-end; margin-top: 2rem;">
                <button class="btn btn-secondary" onclick="closeOrderModal()">Đóng</button>
                <button class="btn btn-primary" onclick="updateOrderStatus(${order.id})">Cập Nhật Trạng Thái</button>
            </div>
        `;
        
        document.getElementById('orderDetailsContent').innerHTML = content;
        document.getElementById('orderModal').style.display = 'flex';
        
    } catch (error) {
        console.error('Error loading order details:', error);
        alert('Lỗi khi tải chi tiết đơn hàng');
    }
}

// Update Order Status
async function updateOrderStatus(id) {
    const newStatus = prompt('Nhập trạng thái mới (PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED):');
    if (!newStatus) return;
    
    try {
        const response = await fetch(`/api/admin/orders/${id}/status`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ status: newStatus.toUpperCase() })
        });
        
        if (response.ok) {
            loadOrders();
            closeOrderModal();
            alert('Đã cập nhật trạng thái đơn hàng thành công!');
        } else {
            alert('Lỗi khi cập nhật trạng thái đơn hàng');
        }
    } catch (error) {
        console.error('Error updating order status:', error);
        alert('Lỗi khi cập nhật trạng thái đơn hàng');
    }
}

// Close Modal
function closeOrderModal() {
    document.getElementById('orderModal').style.display = 'none';
}

// Helper Functions
function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('vi-VN', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    }).format(date);
}

function getStatusBadge(status) {
    const statusMap = {
        'PENDING': 'pending',
        'CONFIRMED': 'processing',
        'PROCESSING': 'processing',
        'SHIPPED': 'delivered',
        'DELIVERED': 'active',
        'CANCELLED': 'cancelled'
    };
    return `<span class="status-badge status-${statusMap[status] || 'pending'}">${status}</span>`;
}

function getPaymentBadge(status) {
    const statusMap = {
        'PENDING': 'pending',
        'COMPLETED': 'active',
        'FAILED': 'cancelled'
    };
    return `<span class="status-badge status-${statusMap[status] || 'pending'}">${status}</span>`;
}

function createOrder() {
    window.location.href = '/admin/orders/create';
}

function exportOrders() {
    window.location.href = '/admin/api/orders/export';
}
