// Admin Analytics JavaScript
let salesChart, trafficChart, demographicsChart, newsViewsChart, categoriesChart;

document.addEventListener('DOMContentLoaded', function() {
    loadAnalytics();
    loadNewsAnalytics();
    initCharts();
    
    document.getElementById('dateRange').addEventListener('change', function() {
        loadAnalytics();
        loadNewsAnalytics();
    });
});

async function loadAnalytics() {
    const period = document.getElementById('dateRange').value;
    
    try {
        const response = await fetch(`/admin/dashboard/stats?period=${period}`);
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

async function loadNewsAnalytics() {
    const period = document.getElementById('dateRange').value;
    
    try {
        const response = await fetch(`/admin/api/news/analytics?period=${period}`);
        const data = await response.json();
        
        // Update news statistics
        document.getElementById('totalArticles').textContent = formatNumber(data.totalPublishedArticles || 0);
        document.getElementById('totalViews').textContent = formatNumber(data.totalViews || 0);
        document.getElementById('featuredArticles').textContent = formatNumber(data.featuredArticlesCount || 0);
        document.getElementById('recentArticles').textContent = formatNumber(data.recentArticlesCount || 0);
        
        // Update most viewed articles
        updateMostViewedArticles(data.mostViewedArticles || []);
        
        // Update news charts
        updateNewsCharts();
        
    } catch (error) {
        console.error('Error loading news analytics:', error);
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
    
    // News Views Chart
    const newsViewsCtx = document.getElementById('newsViewsChart').getContext('2d');
    newsViewsChart = new Chart(newsViewsCtx, {
        type: 'line',
        data: {
            labels: [],
            datasets: [{
                label: 'Lượt xem',
                data: [],
                borderColor: '#3f51b5',
                backgroundColor: 'rgba(63, 81, 181, 0.1)',
                tension: 0.4,
                fill: true
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            plugins: { 
                legend: { display: false },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return 'Lượt xem: ' + formatNumber(context.parsed.y);
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) {
                            return formatNumber(value);
                        }
                    }
                }
            }
        }
    });
    
    // Categories Chart
    const categoriesCtx = document.getElementById('categoriesChart').getContext('2d');
    categoriesChart = new Chart(categoriesCtx, {
        type: 'doughnut',
        data: {
            labels: [],
            datasets: [{
                data: [],
                backgroundColor: ['#3f51b5', '#4caf50', '#ff9800', '#e91e63', '#9c27b0', '#2196f3']
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            plugins: {
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return context.label + ': ' + formatNumber(context.parsed) + ' bài viết';
                        }
                    }
                }
            }
        }
    });
}

async function updateCharts(period) {
    try {
        const response = await fetch(`/admin/dashboard/sales-chart?period=${period}`);
        const data = await response.json();
        
        salesChart.data.labels = data.labels;
        salesChart.data.datasets[0].data = data.data;
        salesChart.update();
        
        loadProductPerformance();
    } catch (error) {
        console.error('Error updating charts:', error);
    }
}

async function updateNewsCharts() {
    try {
        // Update news views chart
        const viewsResponse = await fetch('/admin/api/news/views-chart?months=12');
        const viewsData = await viewsResponse.json();
        
        newsViewsChart.data.labels = viewsData.labels;
        newsViewsChart.data.datasets[0].data = viewsData.data;
        newsViewsChart.update();
        
        // Update categories chart
        const categoriesResponse = await fetch('/admin/api/news/top-categories?limit=6');
        const categoriesData = await categoriesResponse.json();
        
        categoriesChart.data.labels = categoriesData.map(cat => cat.name);
        categoriesChart.data.datasets[0].data = categoriesData.map(cat => cat.articleCount);
        categoriesChart.update();
        
    } catch (error) {
        console.error('Error updating news charts:', error);
    }
}

function updateMostViewedArticles(articles) {
    const container = document.getElementById('mostViewedArticles');
    
    if (!articles || articles.length === 0) {
        container.innerHTML = '<div style="padding: 2rem; text-align: center; color: var(--text-secondary);">Chưa có dữ liệu</div>';
        return;
    }
    
    container.innerHTML = articles.map((article, index) => `
        <div style="padding: 1rem; border-bottom: 1px solid var(--border-color); display: flex; align-items: center; gap: 1rem;">
            <div style="
                width: 32px; 
                height: 32px; 
                border-radius: 50%; 
                background: var(--primary-color); 
                color: white; 
                display: flex; 
                align-items: center; 
                justify-content: center; 
                font-weight: 600;
                font-size: 0.875rem;
            ">
                ${index + 1}
            </div>
            <div style="flex: 1;">
                <div style="font-weight: 600; margin-bottom: 0.25rem;">
                    <a href="/news/${article.slug}" target="_blank" style="color: var(--text-primary); text-decoration: none;">
                        ${article.title}
                    </a>
                </div>
                <div style="font-size: 0.875rem; color: var(--text-secondary);">
                    ${article.categoryName} • ${formatDate(article.publishedAt)}
                </div>
            </div>
            <div style="text-align: right;">
                <div style="font-weight: 600; color: var(--primary-color);">
                    ${formatNumber(article.viewCount)}
                </div>
                <div style="font-size: 0.875rem; color: var(--text-secondary);">
                    lượt xem
                </div>
            </div>
        </div>
    `).join('');
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

function formatDate(dateString) {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('vi-VN', { 
        year: 'numeric', 
        month: 'short', 
        day: 'numeric' 
    }).format(date);
}

function exportReport() {
    const period = document.getElementById('dateRange').value;
    window.location.href = `/admin/api/analytics/export?period=${period}`;
}
