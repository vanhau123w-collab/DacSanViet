// Admin Analytics JavaScript
let salesChart, trafficChart, demographicsChart;

document.addEventListener('DOMContentLoaded', function() {
    loadAnalytics();
    initCharts();
    
    document.getElementById('dateRange').addEventListener('change', loadAnalytics);
});

async function loadAnalytics() {
    const period = document.getElementById('dateRange').value;
    
    try {
        const response = await fetch(`/admin/api/dashboard/stats?period=${period}`);
        const data = await response.json();
        
        document.getElementById('totalRevenue').textContent = formatCurrency(data.totalRevenue);
        document.getElementById('totalOrders').textContent = formatNumber(data.totalOrders);
        document.getElementById('avgOrderValue').textContent = formatCurrency(data.avgOrderValue);
        document.getElementById('newCustomers').textContent = formatNumber(data.newCustomers);
        
        updateCharts(period);
    } catch (error) {
        console.error('Error loading analytics:', error);
    }
}

async function initCharts() {
    // Sales Trend Chart
    const salesCtx = document.getElementById('salesTrendChart').getContext('2d');
    salesChart = new Chart(salesCtx, {
        type: 'line',
        data: {
            labels: [],
            datasets: [{
                label: 'Revenue',
                data: [],
                borderColor: '#D2691E',
                backgroundColor: 'rgba(210, 105, 30, 0.1)',
                tension: 0.4,
                fill: true
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            plugins: { legend: { display: false } }
        }
    });
    
    // Traffic Sources Chart
    const trafficCtx = document.getElementById('trafficChart').getContext('2d');
    trafficChart = new Chart(trafficCtx, {
        type: 'doughnut',
        data: {
            labels: ['Organic Search', 'Social Media', 'Direct', 'Email'],
            datasets: [{
                data: [45, 32, 18, 5],
                backgroundColor: ['#2196f3', '#9c27b0', '#ff9800', '#4caf50']
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true
        }
    });
    
    // Demographics Chart
    const demoCtx = document.getElementById('demographicsChart').getContext('2d');
    demographicsChart = new Chart(demoCtx, {
        type: 'pie',
        data: {
            labels: ['Hanoi', 'HCMC', 'Da Nang', 'Other'],
            datasets: [{
                data: [40, 30, 20, 10],
                backgroundColor: ['#D2691E', '#4ec2b6', '#ff9800', '#9c27b0']
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true
        }
    });
}

async function updateCharts(period) {
    try {
        const response = await fetch(`/admin/api/dashboard/sales-chart?period=${period}`);
        const data = await response.json();
        
        salesChart.data.labels = data.labels;
        salesChart.data.datasets[0].data = data.data;
        salesChart.update();
        
        loadProductPerformance();
    } catch (error) {
        console.error('Error updating charts:', error);
    }
}

async function loadProductPerformance() {
    try {
        const response = await fetch('/admin/api/dashboard/top-products?limit=5');
        const products = await response.json();
        
        const container = document.getElementById('productPerformance');
        container.innerHTML = products.map(product => `
            <div style="padding: 1rem; border-bottom: 1px solid var(--border-color);">
                <div style="display: flex; justify-content: space-between; margin-bottom: 0.5rem;">
                    <span style="font-weight: 600;">${product.name}</span>
                    <span style="color: var(--primary-color);">${formatCurrency(product.price)}</span>
                </div>
                <div style="background: var(--dark-bg); height: 8px; border-radius: 4px; overflow: hidden;">
                    <div style="background: var(--primary-color); height: 100%; width: ${Math.min(product.stock, 100)}%;"></div>
                </div>
                <div style="font-size: 0.875rem; color: var(--text-secondary); margin-top: 0.25rem;">
                    ${product.stock} units sold
                </div>
            </div>
        `).join('');
    } catch (error) {
        console.error('Error loading product performance:', error);
    }
}

function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
}

function formatNumber(num) {
    return new Intl.NumberFormat('vi-VN').format(num);
}

function exportReport() {
    const period = document.getElementById('dateRange').value;
    window.location.href = `/admin/api/analytics/export?period=${period}`;
}
