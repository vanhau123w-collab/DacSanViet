// Admin Customers Management
let currentPage = 1;
let pageSize = 10;
let totalPages = 1;

document.addEventListener('DOMContentLoaded', () => loadCustomers());

async function loadCustomers() {
    try {
        const params = new URLSearchParams({
            page: currentPage - 1,
            size: pageSize
        });
        
        const response = await fetch(`/api/admin/users?${params}`);
        const data = await response.json();
        
        displayCustomers(data.content);
        updatePagination(data);
    } catch (error) {
        console.error('Error:', error);
    }
}

function displayCustomers(customers) {
    const tbody = document.getElementById('customersTableBody');
    tbody.innerHTML = customers.map(customer => `
        <tr>
            <td>
                <div style="display: flex; align-items: center; gap: 1rem;">
                    <div style="width: 40px; height: 40px; border-radius: 50%; background: var(--primary-color); 
                                display: flex; align-items: center; justify-content: center; font-weight: 700;">
                        ${customer.fullName ? customer.fullName.charAt(0).toUpperCase() : 'U'}
                    </div>
                    <div>
                        <div style="font-weight: 600;">${customer.fullName || 'N/A'}</div>
                        <div style="font-size: 0.875rem; color: var(--text-secondary);">ID: ${customer.id}</div>
                    </div>
                </div>
            </td>
            <td>
                <div><i class="bi bi-envelope"></i> ${customer.email}</div>
                <div style="font-size: 0.875rem; color: var(--text-secondary);">
                    <i class="bi bi-telephone"></i> ${customer.phoneNumber || 'N/A'}
                </div>
            </td>
            <td>${customer.totalOrders || 0} orders</td>
            <td><strong>${formatCurrency(customer.totalSpent || 0)}</strong></td>
            <td><span class="status-badge status-active">Active</span></td>
            <td>
                <div class="action-buttons">
                    <button class="action-btn" onclick="viewCustomer(${customer.id})">
                        <i class="bi bi-eye"></i>
                    </button>
                    <button class="action-btn" onclick="editCustomer(${customer.id})">
                        <i class="bi bi-pencil"></i>
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

function updatePagination(data) {
    totalPages = data.totalPages;
    document.getElementById('showingStart').textContent = (currentPage - 1) * pageSize + 1;
    document.getElementById('showingEnd').textContent = Math.min(currentPage * pageSize, data.totalElements);
    document.getElementById('totalCustomers').textContent = data.totalElements;
    
    const buttons = document.getElementById('paginationButtons');
    let html = `<button class="page-btn" onclick="changePage(${currentPage - 1})" ${currentPage === 1 ? 'disabled' : ''}>
                <i class="bi bi-chevron-left"></i></button>`;
    
    for (let i = 1; i <= totalPages; i++) {
        if (i === 1 || i === totalPages || (i >= currentPage - 2 && i <= currentPage + 2)) {
            html += `<button class="page-btn ${i === currentPage ? 'active' : ''}" onclick="changePage(${i})">${i}</button>`;
        }
    }
    
    html += `<button class="page-btn" onclick="changePage(${currentPage + 1})" ${currentPage === totalPages ? 'disabled' : ''}>
             <i class="bi bi-chevron-right"></i></button>`;
    buttons.innerHTML = html;
}

function changePage(page) {
    if (page < 1 || page > totalPages) return;
    currentPage = page;
    loadCustomers();
}

let searchTimeout;
function searchCustomers() {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
        currentPage = 1;
        loadCustomers();
    }, 500);
}

function filterCustomers() {
    currentPage = 1;
    loadCustomers();
}

function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
}

function viewCustomer(id) {
    window.location.href = `/admin/customers/${id}`;
}

function editCustomer(id) {
    window.location.href = `/admin/customers/${id}/edit`;
}

function addCustomer() {
    window.location.href = '/admin/customers/create';
}

function exportCustomers() {
    window.location.href = '/admin/api/customers/export';
}
