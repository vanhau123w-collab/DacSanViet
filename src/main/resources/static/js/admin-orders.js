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

// View Order Details - Navigate to detail page
function viewOrder(id) {
    window.location.href = `/admin/orders/${id}`;
}

// Update Order Status - Open Edit Modal
async function updateOrderStatus(id) {
    try {
        const response = await fetch(`/api/admin/orders/${id}`);
        
        if (!response.ok) {
            alert('Lỗi khi tải chi tiết đơn hàng');
            return;
        }
        
        const order = await response.json();
        showEditOrderModal(order);
        
    } catch (error) {
        console.error('Error loading order:', error);
        alert('Lỗi khi tải chi tiết đơn hàng');
    }
}

// Helper functions for dropdown text
function getStatusText(status) {
    const statusMap = {
        'PENDING': 'Chờ Xác Nhận',
        'CONFIRMED': 'Đã Xác Nhận',
        'PROCESSING': 'Đang Xử Lý',
        'SHIPPED': 'Đang Giao',
        'DELIVERED': 'Đã Giao',
        'CANCELLED': 'Đã Hủy'
    };
    return statusMap[status] || status;
}

function getPaymentStatusText(status) {
    const statusMap = {
        'PENDING': 'Chờ Thanh Toán',
        'COMPLETED': 'Đã Thanh Toán',
        'FAILED': 'Thanh Toán Thất Bại'
    };
    return statusMap[status] || status;
}

function getCarrierText(carrier) {
    const carrierMap = {
        'GHN': 'Giao Hàng Nhanh',
        'GHTK': 'Giao Hàng Tiết Kiệm',
        'VIETTEL_POST': 'Viettel Post',
        'VN_POST': 'Bưu Điện Việt Nam',
        'JT': 'J&T Express'
    };
    return carrierMap[carrier] || carrier;
}

// Toggle dropdown
function toggleDropdown(dropdownName) {
    const dropdown = document.querySelector(`[data-dropdown="${dropdownName}"]`);
    const menu = dropdown.querySelector('.dropdown-menu');
    const isOpen = menu.classList.contains('show');
    
    // Close all dropdowns first
    document.querySelectorAll('.dropdown-menu').forEach(m => m.classList.remove('show'));
    
    // Toggle current dropdown
    if (!isOpen) {
        menu.classList.add('show');
        
        // Focus search input if exists
        const searchInput = menu.querySelector('.dropdown-search input');
        if (searchInput) {
            setTimeout(() => searchInput.focus(), 100);
        }
    }
}

// Select dropdown item
function selectDropdownItem(dropdownName, value, text) {
    const dropdown = document.querySelector(`[data-dropdown="${dropdownName}"]`);
    const selectedText = dropdown.querySelector('.selected-text');
    const hiddenInput = document.getElementById(`edit${dropdownName.charAt(0).toUpperCase() + dropdownName.slice(1)}`);
    const menu = dropdown.querySelector('.dropdown-menu');
    
    // Update display
    selectedText.textContent = text;
    selectedText.setAttribute('data-value', value);
    
    // Update hidden input
    if (hiddenInput) {
        hiddenInput.value = value;
    }
    
    // Mark selected item
    menu.querySelectorAll('.dropdown-item').forEach(item => {
        item.classList.remove('selected');
        if (item.getAttribute('data-value') === value) {
            item.classList.add('selected');
        }
    });
    
    // Close dropdown
    menu.classList.remove('show');
}

// Filter dropdown items
function filterDropdown(dropdownName, searchText) {
    const dropdown = document.querySelector(`[data-dropdown="${dropdownName}"]`);
    const items = dropdown.querySelectorAll('.dropdown-item');
    const search = searchText.toLowerCase().trim();
    
    items.forEach(item => {
        const text = item.textContent.toLowerCase();
        const searchAttr = item.getAttribute('data-search') || '';
        const matches = text.includes(search) || searchAttr.includes(search);
        item.style.display = matches ? 'flex' : 'none';
    });
}

// Close dropdown when clicking outside
function closeDropdownOnClickOutside(event) {
    if (!event.target.closest('.custom-dropdown')) {
        document.querySelectorAll('.dropdown-menu').forEach(menu => {
            menu.classList.remove('show');
        });
    }
}

// Show Edit Order Modal
function showEditOrderModal(order) {
    const isExpress5H = order.shippingMethod === 'EXPRESS_5H';
    
    const content = `
        <div class="edit-modal-header">
            <h3>Cập Nhật Đơn Hàng</h3>
            <button class="modal-close-btn" onclick="closeEditModal()">
                <i class="bi bi-x-lg"></i>
            </button>
        </div>
        
        <div class="edit-modal-body">
            <!-- Customer Info Card -->
            <div class="info-card">
                <div class="info-card-header">
                    <i class="bi bi-person-circle"></i>
                    <span>Thông Tin Khách Hàng</span>
                </div>
                <div class="info-card-body">
                    <div class="info-row">
                        <span class="info-label">Tên khách hàng:</span>
                        <span class="info-value">${order.customerName || 'N/A'}</span>
                    </div>
                    <div class="info-row">
                        <span class="info-label">Email:</span>
                        <span class="info-value">${order.customerEmail || 'N/A'}</span>
                    </div>
                    <div class="info-row">
                        <span class="info-label">Số điện thoại:</span>
                        <span class="info-value">${order.customerPhone || 'N/A'}</span>
                    </div>
                    <div class="info-row">
                        <span class="info-label">Địa chỉ giao hàng:</span>
                        <span class="info-value">${order.shippingAddressText || 'N/A'}</span>
                    </div>
                </div>
            </div>

            <!-- Shipping & Payment Info -->
            <div class="info-card">
                <div class="info-card-header">
                    <i class="bi bi-truck"></i>
                    <span>Vận Chuyển & Thanh Toán</span>
                </div>
                <div class="info-card-body">
                    <div class="info-row">
                        <span class="info-label">Phương thức vận chuyển:</span>
                        <span class="info-value">
                            ${order.shippingMethod === 'EXPRESS_5H' ? '⚡ Giao Nhanh 5H' : 
                              order.shippingMethod === 'STANDARD' ? '📦 Giao Tiêu Chuẩn' : 
                              order.shippingMethod || 'Chưa chọn'}
                        </span>
                    </div>
                    <div class="info-row">
                        <span class="info-label">Đơn vị vận chuyển:</span>
                        <span class="info-value">
                            ${order.shippingCarrier ? getCarrierText(order.shippingCarrier) : 'Chưa chọn'}
                        </span>
                    </div>
                    <div class="info-row">
                        <span class="info-label">Phương thức thanh toán:</span>
                        <span class="info-value">
                            ${order.paymentMethod === 'COD' ? '💵 Thanh toán khi nhận hàng' :
                              order.paymentMethod === 'BANK_TRANSFER' ? '🏦 Chuyển khoản ngân hàng' :
                              order.paymentMethod === 'MOMO' ? '📱 Ví MoMo' :
                              order.paymentMethod === 'VNPAY' ? '💳 VNPay' :
                              order.paymentMethod || 'Chưa chọn'}
                        </span>
                    </div>
                </div>
            </div>

            <!-- Update Form -->
            <div class="update-form-card">
                <div class="form-row">
                    <div class="form-col">
                        <label for="editOrderStatus">Trạng Thái Đơn Hàng *</label>
                        <div class="custom-dropdown" data-dropdown="orderStatus">
                            <div class="dropdown-selected" onclick="toggleDropdown('orderStatus')">
                                <span class="selected-text" data-value="${order.status}">
                                    ${getStatusText(order.status)}
                                </span>
                                <i class="bi bi-chevron-down"></i>
                            </div>
                            <div class="dropdown-menu">
                                <div class="dropdown-item ${order.status === 'PENDING' ? 'selected' : ''}" data-value="PENDING" onclick="selectDropdownItem('orderStatus', 'PENDING', 'Chờ Xác Nhận')">
                                    <span>Chờ Xác Nhận</span>
                                </div>
                                <div class="dropdown-item ${order.status === 'CONFIRMED' ? 'selected' : ''}" data-value="CONFIRMED" onclick="selectDropdownItem('orderStatus', 'CONFIRMED', 'Đã Xác Nhận')">
                                    <span>Đã Xác Nhận</span>
                                </div>
                                <div class="dropdown-item ${order.status === 'PROCESSING' ? 'selected' : ''}" data-value="PROCESSING" onclick="selectDropdownItem('orderStatus', 'PROCESSING', 'Đang Xử Lý')">
                                    <span>Đang Xử Lý</span>
                                </div>
                                <div class="dropdown-item ${order.status === 'SHIPPED' ? 'selected' : ''}" data-value="SHIPPED" onclick="selectDropdownItem('orderStatus', 'SHIPPED', 'Đang Giao')">
                                    <span>Đang Giao</span>
                                </div>
                                <div class="dropdown-item ${order.status === 'DELIVERED' ? 'selected' : ''}" data-value="DELIVERED" onclick="selectDropdownItem('orderStatus', 'DELIVERED', 'Đã Giao')">
                                    <span>Đã Giao</span>
                                </div>
                                <div class="dropdown-item ${order.status === 'CANCELLED' ? 'selected' : ''}" data-value="CANCELLED" onclick="selectDropdownItem('orderStatus', 'CANCELLED', 'Đã Hủy')">
                                    <span>Đã Hủy</span>
                                </div>
                            </div>
                        </div>
                        <input type="hidden" id="editOrderStatus" value="${order.status}">
                    </div>
                    
                    <div class="form-col">
                        <label for="editPaymentStatus">Trạng Thái Thanh Toán *</label>
                        <div class="custom-dropdown" data-dropdown="paymentStatus">
                            <div class="dropdown-selected" onclick="toggleDropdown('paymentStatus')">
                                <span class="selected-text" data-value="${order.paymentStatus}">
                                    ${getPaymentStatusText(order.paymentStatus)}
                                </span>
                                <i class="bi bi-chevron-down"></i>
                            </div>
                            <div class="dropdown-menu">
                                <div class="dropdown-item ${order.paymentStatus === 'PENDING' ? 'selected' : ''}" data-value="PENDING" onclick="selectDropdownItem('paymentStatus', 'PENDING', 'Chờ Thanh Toán')">
                                    <span>Chờ Thanh Toán</span>
                                </div>
                                <div class="dropdown-item ${order.paymentStatus === 'COMPLETED' ? 'selected' : ''}" data-value="COMPLETED" onclick="selectDropdownItem('paymentStatus', 'COMPLETED', 'Đã Thanh Toán')">
                                    <span>Đã Thanh Toán</span>
                                </div>
                                <div class="dropdown-item ${order.paymentStatus === 'FAILED' ? 'selected' : ''}" data-value="FAILED" onclick="selectDropdownItem('paymentStatus', 'FAILED', 'Thanh Toán Thất Bại')">
                                    <span>Thanh Toán Thất Bại</span>
                                </div>
                            </div>
                        </div>
                        <input type="hidden" id="editPaymentStatus" value="${order.paymentStatus}">
                    </div>
                </div>

                ${!isExpress5H ? `
                <div class="form-group">
                    <label for="editShippingCarrier">Đơn Vị Vận Chuyển ${order.status === 'SHIPPED' || order.status === 'DELIVERED' ? '*' : ''}</label>
                    <div class="custom-dropdown" data-dropdown="shippingCarrier">
                        <div class="dropdown-selected" onclick="toggleDropdown('shippingCarrier')">
                            <span class="selected-text" data-value="${order.shippingCarrier || ''}">
                                ${order.shippingCarrier ? getCarrierText(order.shippingCarrier) : '-- Chọn đơn vị vận chuyển --'}
                            </span>
                            <i class="bi bi-chevron-down"></i>
                        </div>
                        <div class="dropdown-menu">
                            <div class="dropdown-search">
                                <i class="bi bi-search"></i>
                                <input type="text" placeholder="Tìm kiếm..." onkeyup="filterDropdown('shippingCarrier', this.value)">
                            </div>
                            <div class="dropdown-item ${!order.shippingCarrier ? 'selected' : ''}" data-value="" onclick="selectDropdownItem('shippingCarrier', '', '-- Chọn đơn vị vận chuyển --')">
                                <span>-- Chọn đơn vị vận chuyển --</span>
                            </div>
                            <div class="dropdown-item ${order.shippingCarrier === 'GHN' ? 'selected' : ''}" data-value="GHN" data-search="giao hang nhanh ghn" onclick="selectDropdownItem('shippingCarrier', 'GHN', 'Giao Hàng Nhanh')">
                                <img src="/images/carriers/ghn.png" alt="GHN" class="carrier-icon" onerror="this.style.display='none'">
                                <span>Giao Hàng Nhanh</span>
                            </div>
                            <div class="dropdown-item ${order.shippingCarrier === 'GHTK' ? 'selected' : ''}" data-value="GHTK" data-search="giao hang tiet kiem ghtk" onclick="selectDropdownItem('shippingCarrier', 'GHTK', 'Giao Hàng Tiết Kiệm')">
                                <img src="/images/carriers/ghtk.png" alt="GHTK" class="carrier-icon" onerror="this.style.display='none'">
                                <span>Giao Hàng Tiết Kiệm</span>
                            </div>
                            <div class="dropdown-item ${order.shippingCarrier === 'VIETTEL_POST' ? 'selected' : ''}" data-value="VIETTEL_POST" data-search="viettel post" onclick="selectDropdownItem('shippingCarrier', 'VIETTEL_POST', 'Viettel Post')">
                                <img src="/images/carriers/viettel-post.png" alt="Viettel Post" class="carrier-icon" onerror="this.style.display='none'">
                                <span>Viettel Post</span>
                            </div>
                            <div class="dropdown-item ${order.shippingCarrier === 'VN_POST' ? 'selected' : ''}" data-value="VN_POST" data-search="buu dien viet nam vnpost" onclick="selectDropdownItem('shippingCarrier', 'VN_POST', 'Bưu Điện Việt Nam')">
                                <img src="/images/carriers/vnpost.png" alt="VN Post" class="carrier-icon" onerror="this.style.display='none'">
                                <span>Bưu Điện Việt Nam</span>
                            </div>
                            <div class="dropdown-item ${order.shippingCarrier === 'JT' ? 'selected' : ''}" data-value="JT" data-search="jt express j&t" onclick="selectDropdownItem('shippingCarrier', 'JT', 'J&T Express')">
                                <img src="/images/carriers/jt.png" alt="J&T" class="carrier-icon" onerror="this.style.display='none'">
                                <span>J&T Express</span>
                            </div>
                        </div>
                    </div>
                    <input type="hidden" id="editShippingCarrier" value="${order.shippingCarrier || ''}">
                    <small class="form-hint">Bắt buộc khi chuyển sang trạng thái "Đang Giao"</small>
                </div>
                ` : `
                <div class="form-group">
                    <label>Đơn Vị Vận Chuyển</label>
                    <div class="carrier-display">
                        <img src="/images/carriers/dacsanviet.png" alt="DacSanViet" class="carrier-logo" onerror="this.src='data:image/svg+xml,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 width=%22100%22 height=%2240%22%3E%3Crect fill=%22%23FF6B35%22 width=%22100%22 height=%2240%22/%3E%3Ctext x=%2250%25%22 y=%2250%25%22 dominant-baseline=%22middle%22 text-anchor=%22middle%22 fill=%22white%22 font-family=%22Arial%22 font-size=%2212%22 font-weight=%22bold%22%3EDacSanViet%3C/text%3E%3C/svg%3E'">
                        <span>DacSanVietShip (Giao nhanh 5H)</span>
                    </div>
                    <input type="hidden" id="editShippingCarrier" value="DacSanVietShip">
                </div>
                `}
                
                <div class="form-group">
                    <label for="editTrackingNumber">Mã Vận Đơn</label>
                    <input type="text" id="editTrackingNumber" class="custom-input" 
                           value="${order.trackingNumber || ''}" 
                           placeholder="Nhập mã vận đơn (nếu có)">
                </div>
                
                <div class="form-group">
                    <label for="editNotes">Ghi Chú</label>
                    <textarea id="editNotes" class="custom-textarea" rows="3" 
                              placeholder="Thêm ghi chú cho đơn hàng...">${order.notes || ''}</textarea>
                </div>
            </div>
        </div>
        
        <div class="edit-modal-footer">
            <button class="btn-cancel" onclick="closeEditModal()">Hủy</button>
            <button class="btn-save" onclick="saveOrderChanges(${order.id})">
                <i class="bi bi-check-lg"></i>
                Lưu Thay Đổi
            </button>
        </div>
    `;
    
    document.getElementById('editOrderContent').innerHTML = content;
    const modal = document.getElementById('editOrderModal');
    modal.classList.add('show');
    
    // Close dropdown when clicking outside
    document.addEventListener('click', closeDropdownOnClickOutside);
}

// Close Edit Modal
function closeEditModal() {
    const modal = document.getElementById('editOrderModal');
    modal.classList.remove('show');
    
    // Remove event listener
    document.removeEventListener('click', closeDropdownOnClickOutside);
}
async function saveOrderChanges(orderId) {
    const status = document.getElementById('editOrderStatus').value;
    const paymentStatus = document.getElementById('editPaymentStatus').value;
    const shippingCarrier = document.getElementById('editShippingCarrier').value;
    const trackingNumber = document.getElementById('editTrackingNumber').value;
    const notes = document.getElementById('editNotes').value;
    
    // Validate: if status is SHIPPED or DELIVERED, shipping carrier is required
    if ((status === 'SHIPPED' || status === 'DELIVERED') && !shippingCarrier) {
        showNotification('⚠️ Vui lòng chọn đơn vị vận chuyển khi chuyển sang trạng thái "Đang Giao" hoặc "Đã Giao"', 'warning');
        return;
    }
    
    // Get CSRF token from meta tag
    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
    
    try {
        const response = await fetch(`/api/admin/orders/${orderId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify({
                status: status,
                paymentStatus: paymentStatus,
                shippingCarrier: shippingCarrier,
                trackingNumber: trackingNumber,
                notes: notes
            })
        });
        
        if (response.ok) {
            showNotification('Đã cập nhật đơn hàng thành công!', 'success');
            closeEditModal();
            loadOrders();
        } else {
            const error = await response.json();
            showNotification('Lỗi: ' + (error.error || 'Không thể cập nhật đơn hàng'), 'error');
        }
    } catch (error) {
        console.error('Error updating order:', error);
        showNotification('Lỗi khi cập nhật đơn hàng', 'error');
    }
}

// Show notification
function showNotification(message, type = 'info') {
    // Remove existing notification
    const existing = document.querySelector('.custom-notification');
    if (existing) existing.remove();
    
    const notification = document.createElement('div');
    notification.className = `custom-notification notification-${type}`;
    notification.innerHTML = `
        <div class="notification-content">
            <span>${message}</span>
            <button onclick="this.parentElement.parentElement.remove()" class="notification-close">
                <i class="bi bi-x"></i>
            </button>
        </div>
    `;
    
    document.body.appendChild(notification);
    
    setTimeout(() => {
        notification.classList.add('show');
    }, 10);
    
    setTimeout(() => {
        notification.classList.remove('show');
        setTimeout(() => notification.remove(), 300);
    }, 3000);
}

// Close Edit Modal
function closeEditModal() {
    const modal = document.getElementById('editOrderModal');
    modal.classList.remove('show');
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
