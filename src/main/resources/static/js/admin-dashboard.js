// Admin Dashboard JavaScript
let salesChart = null;
let orderStatusChart = null;
let topCategoriesChart = null;
let currentPeriod = '30days';
let recentOrdersPage = 0;
let recentOrdersSize = 5;
let recentOrdersTotalPages = 1;

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    loadDashboardStats();
    loadSalesChart();
    loadTopProducts();
    loadRecentOrders();
    loadOrderStatusChart();
    loadTopCategoriesChart();
    
    // Period selector
    document.getElementById('periodSelect').addEventListener('change', function() {
        currentPeriod = this.value;
        loadDashboardStats();
        loadSalesChart();
    });
});

// Load Dashboard Statistics
async function loadDashboardStats() {
    try {
        const response = await fetch(`/admin/dashboard/stats?period=${currentPeriod}`);
        const data = await response.json();
        
        // Update stats
        document.getElementById('totalRevenue').textContent = formatCurrency(data.totalRevenue);
        document.getElementById('totalOrders').textContent = formatNumber(data.totalOrders);
        document.getElementById('avgOrderValue').textContent = formatCurrency(data.avgOrderValue);
        document.getElementById('newCustomers').textContent = formatNumber(data.newCustomers);
        
        // Update changes
        updateChange('revenueChange', data.revenueChange);
        updateChange('ordersChange', data.ordersChange);
        updateChange('customersChange', data.customersChange);
        
    } catch (error) {
        console.error('Error loading stats:', error);
    }
}

// Load Sales Chart
async function loadSalesChart() {
    try {
        const response = await fetch(`/admin/dashboard/sales-chart?period=${currentPeriod}`);
        const data = await response.json();
        
        const ctx = document.getElementById('salesChart').getContext('2d');
        
        if (salesChart) {
            salesChart.destroy();
        }
        
        salesChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: data.labels,
                datasets: [{
                    label: 'Revenue',
                    data: data.data,
                    borderColor: '#D2691E',
                    backgroundColor: 'rgba(210, 105, 30, 0.1)',
                    tension: 0.4,
                    fill: true
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: {
                        display: false
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                return 'Revenue: ' + formatCurrency(context.parsed.y);
                            }
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            callback: function(value) {
                                return formatCurrency(value);
                            }
                        },
                        grid: {
                            color: 'rgba(255, 255, 255, 0.1)'
                        }
                    },
                    x: {
                        grid: {
                            color: 'rgba(255, 255, 255, 0.1)'
                        }
                    }
                }
            }
        });
    } catch (error) {
        console.error('Error loading chart:', error);
    }
}

// Load Top Products
async function loadTopProducts() {
    try {
        const response = await fetch('/admin/dashboard/top-products?limit=5');
        
        if (!response.ok) {
            console.error('Failed to load top products:', response.status);
            return;
        }
        
        const products = await response.json();
        console.log('Top products:', products);
        
        if (!Array.isArray(products)) {
            console.error('Products is not an array:', products);
            return;
        }
        
        const container = document.getElementById('topProductsList');
        
        if (products.length === 0) {
            container.innerHTML = '<div style="padding: 2rem; text-align: center;">Không có sản phẩm nào</div>';
            return;
        }
        
        container.innerHTML = products.map(product => `
            <div style="display: flex; align-items: center; padding: 1rem; border-bottom: 1px solid var(--border-color);">
                <img src="${product.imageUrl || 'data:image/svg+xml,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 width=%2250%22 height=%2250%22%3E%3Crect fill=%22%23ddd%22 width=%2250%22 height=%2250%22/%3E%3Ctext x=%2250%25%22 y=%2250%25%22 dominant-baseline=%22middle%22 text-anchor=%22middle%22 fill=%22%23999%22 font-size=%2210%22%3ENo Image%3C/text%3E%3C/svg%3E'}" 
                     alt="${product.name}" 
                     style="width: 50px; height: 50px; object-fit: cover; border-radius: 8px; margin-right: 1rem;">
                <div style="flex: 1;">
                    <div style="font-weight: 600; margin-bottom: 0.25rem;">${product.name}</div>
                    <div style="font-size: 0.875rem; color: var(--text-secondary);">${product.category || 'N/A'}</div>
                </div>
                <div style="text-align: right;">
                    <div style="font-weight: 600; color: var(--primary-color);">${formatCurrency(product.price)}</div>
                    <div style="font-size: 0.875rem; color: var(--text-secondary);">${product.totalSold || 0} đã bán</div>
                </div>
            </div>
        `).join('');
    } catch (error) {
        console.error('Error loading top products:', error);
    }
}

// Load Recent Orders
async function loadRecentOrders() {
    try {
        const response = await fetch(`/admin/dashboard/recent-orders?page=${recentOrdersPage}&size=${recentOrdersSize}`);
        
        if (!response.ok) {
            console.error('Failed to load recent orders:', response.status);
            return;
        }
        
        const data = await response.json();
        console.log('Recent orders:', data);
        
        const orders = data.content || [];
        recentOrdersTotalPages = data.totalPages || 1;
        
        if (!Array.isArray(orders)) {
            console.error('Orders is not an array:', orders);
            return;
        }
        
        const tbody = document.getElementById('recentOrdersTable');
        
        if (orders.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; padding: 2rem;">Không có đơn hàng nào trong 3 ngày gần đây</td></tr>';
            updateRecentOrdersPagination(data);
            return;
        }
        
        tbody.innerHTML = orders.map(order => `
            <tr>
                <td><strong>${order.orderNumber}</strong></td>
                <td>
                    <div>${order.customerName || 'Khách'}</div>
                    <div style="font-size: 0.875rem; color: var(--text-secondary);">${order.customerEmail || ''}</div>
                </td>
                <td>${formatDate(order.orderDate)}</td>
                <td><strong>${formatCurrency(order.totalAmount)}</strong></td>
                <td>${getStatusBadge(order.status)}</td>
                <td>
                    <div class="action-buttons">
                        <button class="action-btn" onclick="viewOrder(${order.id})" title="Xem">
                            <i class="bi bi-eye"></i>
                        </button>
                    </div>
                </td>
            </tr>
        `).join('');
        
        updateRecentOrdersPagination(data);
    } catch (error) {
        console.error('Error loading recent orders:', error);
    }
}

// Update Recent Orders Pagination
function updateRecentOrdersPagination(data) {
    const paginationContainer = document.getElementById('recentOrdersPagination');
    if (!paginationContainer) return;
    
    const totalPages = data.totalPages || 1;
    const currentPage = data.currentPage || 0;
    const totalElements = data.totalElements || 0;
    
    if (totalPages <= 1) {
        paginationContainer.innerHTML = '';
        return;
    }
    
    let buttons = '';
    
    // Previous button
    buttons += `<button class="page-btn-sm" onclick="changeRecentOrdersPage(${currentPage - 1})" 
                ${currentPage === 0 ? 'disabled' : ''}>
                <i class="bi bi-chevron-left"></i>
                </button>`;
    
    // Page numbers
    for (let i = 0; i < totalPages; i++) {
        if (i === 0 || i === totalPages - 1 || (i >= currentPage - 1 && i <= currentPage + 1)) {
            buttons += `<button class="page-btn-sm ${i === currentPage ? 'active' : ''}" 
                        onclick="changeRecentOrdersPage(${i})">${i + 1}</button>`;
        } else if (i === currentPage - 2 || i === currentPage + 2) {
            buttons += '<span style="padding: 0 0.25rem;">...</span>';
        }
    }
    
    // Next button
    buttons += `<button class="page-btn-sm" onclick="changeRecentOrdersPage(${currentPage + 1})" 
                ${currentPage === totalPages - 1 ? 'disabled' : ''}>
                <i class="bi bi-chevron-right"></i>
                </button>`;
    
    paginationContainer.innerHTML = `
        <div style="display: flex; align-items: center; justify-content: space-between; padding: 1rem 1.5rem; border-top: 1px solid var(--border-color);">
            <div style="font-size: 0.875rem; color: var(--text-secondary);">
                Hiển thị ${currentPage * recentOrdersSize + 1} - ${Math.min((currentPage + 1) * recentOrdersSize, totalElements)} trong tổng ${totalElements} đơn hàng
            </div>
            <div style="display: flex; gap: 0.25rem;">
                ${buttons}
            </div>
        </div>
    `;
}

// Change Recent Orders Page
function changeRecentOrdersPage(page) {
    if (page < 0 || page >= recentOrdersTotalPages) return;
    recentOrdersPage = page;
    loadRecentOrders();
}

function getStatusBadge(status) {
    const statusMap = {
        'PENDING': { class: 'pending', text: 'Chờ Xử Lý' },
        'CONFIRMED': { class: 'processing', text: 'Đã Xác Nhận' },
        'PROCESSING': { class: 'processing', text: 'Đang Xử Lý' },
        'SHIPPED': { class: 'delivered', text: 'Đang Giao' },
        'DELIVERED': { class: 'active', text: 'Đã Giao' },
        'CANCELLED': { class: 'cancelled', text: 'Đã Hủy' }
    };
    
    const statusInfo = statusMap[status] || { class: 'pending', text: status };
    return `<span class="status-badge status-${statusInfo.class}">${statusInfo.text}</span>`;
}

// Helper Functions
function formatCurrency(amount) {
    if (!amount) return '0₫';
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}

function formatNumber(num) {
    if (!num) return '0';
    return new Intl.NumberFormat('vi-VN').format(num);
}

function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('vi-VN', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    }).format(date);
}

function updateChange(elementId, value) {
    const element = document.getElementById(elementId);
    const span = element.querySelector('span');
    const icon = element.querySelector('i');
    
    if (value > 0) {
        element.classList.remove('negative');
        element.classList.add('positive');
        icon.className = 'bi bi-arrow-up';
        span.textContent = `${value.toFixed(1)}%`;
    } else if (value < 0) {
        element.classList.remove('positive');
        element.classList.add('negative');
        icon.className = 'bi bi-arrow-down';
        span.textContent = `${Math.abs(value).toFixed(1)}%`;
    } else {
        element.classList.remove('positive', 'negative');
        icon.className = 'bi bi-dash';
        span.textContent = '0%';
    }
}

function changePeriod(period) {
    currentPeriod = period;
    document.getElementById('periodSelect').value = period;
    loadDashboardStats();
    loadSalesChart();
}

function viewOrder(orderId) {
    window.location.href = `/admin/orders/${orderId}`;
}

function exportReport() {
    window.location.href = `/admin/dashboard/export?period=${currentPeriod}`;
}

// Load Order Status Distribution Chart
async function loadOrderStatusChart() {
    try {
        const response = await fetch('/admin/dashboard/order-status-chart');
        const data = await response.json();
        
        const ctx = document.getElementById('orderStatusChart').getContext('2d');
        
        if (orderStatusChart) {
            orderStatusChart.destroy();
        }
        
        orderStatusChart = new Chart(ctx, {
            type: 'pie',
            data: {
                labels: data.labels,
                datasets: [{
                    data: data.data,
                    backgroundColor: data.colors,
                    borderWidth: 2,
                    borderColor: '#1a1a1a'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            color: '#e0e0e0',
                            padding: 15,
                            font: {
                                size: 12
                            }
                        }
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                const label = context.label || '';
                                const value = context.parsed || 0;
                                const total = context.dataset.data.reduce((a, b) => a + b, 0);
                                const percentage = ((value / total) * 100).toFixed(1);
                                return `${label}: ${value} đơn (${percentage}%)`;
                            }
                        }
                    }
                }
            }
        });
    } catch (error) {
        console.error('Error loading order status chart:', error);
    }
}

// Load Top Categories Chart
async function loadTopCategoriesChart() {
    try {
        const response = await fetch('/admin/dashboard/top-categories-chart');
        const data = await response.json();
        
        const ctx = document.getElementById('topCategoriesChart').getContext('2d');
        
        if (topCategoriesChart) {
            topCategoriesChart.destroy();
        }
        
        topCategoriesChart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: data.labels,
                datasets: [{
                    label: 'Doanh Thu',
                    data: data.revenue,
                    backgroundColor: 'rgba(210, 105, 30, 0.8)',
                    borderColor: '#D2691E',
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: {
                        display: false
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                const revenue = formatCurrency(context.parsed.y);
                                const orders = data.orders[context.dataIndex];
                                return [
                                    `Doanh thu: ${revenue}`,
                                    `Số đơn: ${orders}`
                                ];
                            }
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            callback: function(value) {
                                return formatCurrency(value);
                            },
                            color: '#e0e0e0'
                        },
                        grid: {
                            color: 'rgba(255, 255, 255, 0.1)'
                        }
                    },
                    x: {
                        ticks: {
                            color: '#e0e0e0'
                        },
                        grid: {
                            color: 'rgba(255, 255, 255, 0.1)'
                        }
                    }
                }
            }
        });
    } catch (error) {
        console.error('Error loading top categories chart:', error);
    }
}
