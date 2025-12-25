let currentPage = 0;
let pageSize = 10;
let currentSearch = '';
let currentStatus = '';

// Load suppliers on page load
document.addEventListener('DOMContentLoaded', function() {
    loadSuppliers();
});

// Load suppliers with pagination
async function loadSuppliers(page = 0) {
    currentPage = page;
    
    try {
        const params = new URLSearchParams({
            page: currentPage,
            size: pageSize
        });
        
        if (currentSearch) params.append('search', currentSearch);
        if (currentStatus) params.append('isActive', currentStatus);
        
        const response = await fetch(`/admin/suppliers/list?${params}`);
        const data = await response.json();
        
        renderSuppliers(data.content);
        renderPagination(data);
        updatePaginationInfo(data);
    } catch (error) {
        console.error('Error loading suppliers:', error);
        showNotification('Lỗi khi tải danh sách nhà phân phối', 'error');
    }
}

// Render suppliers table
function renderSuppliers(suppliers) {
    const tbody = document.getElementById('suppliersTableBody');
    
    if (suppliers.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" style="text-align: center; padding: 2rem; color: var(--text-secondary);">
                    <i class="bi bi-inbox" style="font-size: 3rem; display: block; margin-bottom: 1rem; opacity: 0.5;"></i>
                    Không có nhà phân phối nào
                </td>
            </tr>
        `;
        return;
    }
    
    tbody.innerHTML = suppliers.map(supplier => `
        <tr>
            <td style="font-weight: 600;">${supplier.name}</td>
            <td>${supplier.contactPerson || '-'}</td>
            <td>${supplier.phone || '-'}</td>
            <td>${supplier.email || '-'}</td>
            <td style="max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;" title="${supplier.address || ''}">${supplier.address || '-'}</td>
            <td>
                <span class="badge ${supplier.isActive ? 'badge-success' : 'badge-secondary'}">
                    ${supplier.isActive ? 'Hoạt động' : 'Ngừng'}
                </span>
            </td>
            <td>
                <div class="action-buttons">
                    <button class="action-btn" onclick="editSupplier(${supplier.id})" title="Chỉnh sửa">
                        <i class="bi bi-pencil"></i>
                    </button>
                    <button class="action-btn" onclick="deleteSupplier(${supplier.id}, '${supplier.name.replace(/'/g, "\\'")}'))" title="Xóa">
                        <i class="bi bi-trash"></i>
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

// Render pagination
function renderPagination(data) {
    const container = document.getElementById('paginationButtons');
    const totalPages = data.totalPages;
    const currentPage = data.currentPage;
    
    if (totalPages <= 1) {
        container.innerHTML = '';
        return;
    }
    
    let html = '';
    
    // Previous button
    html += `<button class="pagination-btn" ${currentPage === 0 ? 'disabled' : ''} onclick="loadSuppliers(${currentPage - 1})">
        <i class="bi bi-chevron-left"></i>
    </button>`;
    
    // Page numbers
    for (let i = 0; i < totalPages; i++) {
        if (i === 0 || i === totalPages - 1 || (i >= currentPage - 1 && i <= currentPage + 1)) {
            html += `<button class="pagination-btn ${i === currentPage ? 'active' : ''}" onclick="loadSuppliers(${i})">${i + 1}</button>`;
        } else if (i === currentPage - 2 || i === currentPage + 2) {
            html += `<span class="pagination-ellipsis">...</span>`;
        }
    }
    
    // Next button
    html += `<button class="pagination-btn" ${currentPage === totalPages - 1 ? 'disabled' : ''} onclick="loadSuppliers(${currentPage + 1})">
        <i class="bi bi-chevron-right"></i>
    </button>`;
    
    container.innerHTML = html;
}

// Update pagination info
function updatePaginationInfo(data) {
    const start = data.totalElements === 0 ? 0 : data.currentPage * pageSize + 1;
    const end = Math.min((data.currentPage + 1) * pageSize, data.totalElements);
    
    document.getElementById('showingStart').textContent = start;
    document.getElementById('showingEnd').textContent = end;
    document.getElementById('totalSuppliers').textContent = data.totalElements;
}

// Search suppliers
function searchSuppliers() {
    currentSearch = document.getElementById('searchInput').value;
    loadSuppliers(0);
}

// Filter suppliers
function filterSuppliers() {
    currentStatus = document.getElementById('statusFilter').value;
    loadSuppliers(0);
}

// Open supplier modal
function openSupplierModal(supplierId = null) {
    const modal = document.getElementById('supplierModal');
    const form = document.getElementById('supplierForm');
    const title = document.getElementById('modalTitle');
    
    form.reset();
    document.getElementById('supplierId').value = '';
    title.textContent = 'Thêm Nhà Phân Phối Mới';
    
    if (supplierId) {
        title.textContent = 'Chỉnh Sửa Nhà Phân Phối';
        loadSupplierData(supplierId);
    }
    
    modal.style.display = 'flex';
}

// Close supplier modal
function closeSupplierModal() {
    document.getElementById('supplierModal').style.display = 'none';
}

// Load supplier data for editing
async function loadSupplierData(id) {
    try {
        const response = await fetch(`/admin/suppliers/${id}`);
        const supplier = await response.json();
        
        document.getElementById('supplierId').value = supplier.id;
        document.getElementById('supplierName').value = supplier.name;
        document.getElementById('supplierContactPerson').value = supplier.contactPerson || '';
        document.getElementById('supplierPhone').value = supplier.phone || '';
        document.getElementById('supplierEmail').value = supplier.email || '';
        document.getElementById('supplierAddress').value = supplier.address || '';
        document.getElementById('supplierTaxCode').value = supplier.taxCode || '';
        document.getElementById('supplierDescription').value = supplier.description || '';
        document.getElementById('supplierIsActive').value = supplier.isActive.toString();
    } catch (error) {
        console.error('Error loading supplier:', error);
        showNotification('Lỗi khi tải thông tin nhà phân phối', 'error');
    }
}

// Edit supplier
function editSupplier(id) {
    openSupplierModal(id);
}

// Save supplier
async function saveSupplier(event) {
    event.preventDefault();
    
    const id = document.getElementById('supplierId').value;
    const supplierData = {
        name: document.getElementById('supplierName').value.trim(),
        contactPerson: document.getElementById('supplierContactPerson').value.trim(),
        phone: document.getElementById('supplierPhone').value.trim(),
        email: document.getElementById('supplierEmail').value.trim(),
        address: document.getElementById('supplierAddress').value.trim(),
        taxCode: document.getElementById('supplierTaxCode').value.trim(),
        description: document.getElementById('supplierDescription').value.trim(),
        isActive: document.getElementById('supplierIsActive').value === 'true'
    };
    
    try {
        const url = id ? `/admin/suppliers/${id}` : '/admin/suppliers';
        const method = id ? 'PUT' : 'POST';
        
        const response = await fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(supplierData)
        });
        
        if (response.ok) {
            showNotification(id ? 'Cập nhật nhà phân phối thành công' : 'Thêm nhà phân phối thành công', 'success');
            closeSupplierModal();
            loadSuppliers(currentPage);
        } else {
            const error = await response.text();
            showNotification(error || 'Có lỗi xảy ra', 'error');
        }
    } catch (error) {
        console.error('Error saving supplier:', error);
        showNotification('Lỗi khi lưu nhà phân phối', 'error');
    }
}

// Delete supplier
async function deleteSupplier(id, name) {
    if (!confirm(`Bạn có chắc chắn muốn xóa nhà phân phối "${name}"?`)) {
        return;
    }
    
    try {
        const response = await fetch(`/admin/suppliers/${id}`, {
            method: 'DELETE'
        });
        
        if (response.ok) {
            showNotification('Xóa nhà phân phối thành công', 'success');
            loadSuppliers(currentPage);
        } else {
            const error = await response.text();
            showNotification(error || 'Có lỗi xảy ra', 'error');
        }
    } catch (error) {
        console.error('Error deleting supplier:', error);
        showNotification('Lỗi khi xóa nhà phân phối', 'error');
    }
}

// Show notification
function showNotification(message, type = 'info') {
    const existing = document.querySelector('.notification');
    if (existing) existing.remove();
    
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.innerHTML = `
        <i class="bi bi-${type === 'success' ? 'check-circle' : type === 'error' ? 'x-circle' : 'info-circle'}"></i>
        <span>${message}</span>
    `;
    
    document.body.appendChild(notification);
    
    setTimeout(() => notification.classList.add('show'), 10);
    setTimeout(() => {
        notification.classList.remove('show');
        setTimeout(() => notification.remove(), 300);
    }, 3000);
}

// Close modal when clicking outside
window.addEventListener('click', function(event) {
    const modal = document.getElementById('supplierModal');
    if (event.target === modal) {
        closeSupplierModal();
    }
});
