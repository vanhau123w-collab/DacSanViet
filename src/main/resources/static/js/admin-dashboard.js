// Admin Dashboard JavaScript
let salesChart = null;
let currentPeriod = '30days';

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    loadDashboardStats();
    loadSalesChart();
    loadTopProducts();
    loadRecentOrders();
    
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
                <img src="${product.imageUrl || '/images/placeholder.jpg'}" alt="${product.name}" 
                     style="width: 50px; height: 50px; object-fit: cover; border-radius: 8px; margin-right: 1rem;">
                <div style="flex: 1;">
                    <div style="font-weight: 600; margin-bottom: 0.25rem;">${product.name}</div>
                    <div style="font-size: 0.875rem; color: var(--text-secondary);">${product.category || 'N/A'}</div>
                </div>
                <div style="text-align: right;">
                    <div style="font-weight: 600; color: var(--primary-color);">${formatCurrency(product.price)}</div>
                    <div style="font-size: 0.875rem; color: var(--text-secondary);">${product.stock} đơn vị</div>
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
        const response = await fetch('/admin/dashboard/recent-orders?limit=10');
        
        if (!response.ok) {
            console.error('Failed to load recent orders:', response.status);
            return;
        }
        
        const orders = await response.json();
        console.log('Recent orders:', orders);
        
        if (!Array.isArray(orders)) {
            console.error('Orders is not an array:', orders);
            return;
        }
        
        const tbody = document.getElementById('recentOrdersTable');
        
        if (orders.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; padding: 2rem;">Không có đơn hàng nào</td></tr>';
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
    } catch (error) {
        console.error('Error loading recent orders:', error);
    }
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

function editOrder(orderId) {
    window.location.href = `/admin/orders/${orderId}/edit`;
}

function exportReport() {
    window.location.href = `/admin/dashboard/export?period=${currentPeriod}`;
}
