// Admin Categories Management JavaScript
let currentPage = 0;
let pageSize = 10;
let totalPages = 1;
let searchTimeout = null;

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    loadCategories();
});

// Load Categories
async function loadCategories() {
    try {
        const search = document.getElementById('searchInput').value;
        const status = document.getElementById('statusFilter').value;
        
        let url = `/admin/categories/list?page=${currentPage}&size=${pageSize}`;
        if (search) url += `&search=${encodeURIComponent(search)}`;
        if (status) url += `&isActive=${status}`;
        
        const response = await fetch(url);
        const data = await response.json();
        
        displayCategories(data.content);
        updatePagination(data);
    } catch (error) {
        console.error('Error loading categories:', error);
        showNotification('Lỗi khi tải danh mục', 'error');
    }
}

// Display Categories
function displayCategories(categories) {
    const tbody = document.getElementById('categoriesTable');
    
    if (categories.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align: center; padding: 2rem;">Không có danh mục nào</td></tr>';
        return;
    }
    
    tbody.innerHTML = categories.map(category => `
        <tr>
            <td>${category.id}</td>
            <td>
                <div style="display: flex; align-items: center; gap: 0.75rem;">
                    ${category.imageUrl ? 
                        `<img src="${category.imageUrl}" alt="${category.name}" 
                             style="width: 40px; height: 40px; object-fit: cover; border-radius: 8px;">` : 
                        '<div style="width: 40px; height: 40px; background: var(--border-color); border-radius: 8px;"></div>'}
                    <strong>${category.name}</strong>
                </div>
            </td>
            <td>${category.description || '-'}</td>
            <td><span class="badge badge-info">${category.productCount || 0} sản phẩm</span></td>
            <td>
                <span class="status-badge status-${category.isActive ? 'active' : 'cancelled'}">
                    ${category.isActive ? 'Hoạt động' : 'Tạm dừng'}
                </span>
            </td>
            <td>${formatDate(category.createdAt)}</td>
            <td>
                <div class="action-buttons">
                    <button class="action-btn" onclick="editCategory(${category.id})" title="Chỉnh sửa">
                        <i class="bi bi-pencil"></i>
                    </button>
                    <button class="action-btn" onclick="toggleCategoryStatus(${category.id})" title="Đổi trạng thái">
                        <i class="bi bi-toggle-${category.isActive ? 'on' : 'off'}"></i>
                    </button>
                    <button class="action-btn action-btn-delete" onclick="deleteCategory(${category.id}, ${category.productCount})" title="Xóa">
                        <i class="bi bi-trash"></i>
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

// Update Pagination
function updatePagination(data) {
    totalPages = data.totalPages;
    const paginationContainer = document.getElementById('pagination');
    
    if (totalPages <= 1) {
        paginationContainer.innerHTML = '';
        return;
    }
    
    let buttons = '';
    
    // Previous button
    buttons += `<button class="page-btn" onclick="changePage(${currentPage - 1})" 
                ${currentPage === 0 ? 'disabled' : ''}>
                <i class="bi bi-chevron-left"></i> Trước
                </button>`;
    
    // Page numbers
    for (let i = 0; i < totalPages; i++) {
        if (i === 0 || i === totalPages - 1 || (i >= currentPage - 1 && i <= currentPage + 1)) {
            buttons += `<button class="page-btn ${i === currentPage ? 'active' : ''}" 
                        onclick="changePage(${i})">${i + 1}</button>`;
        } else if (i === currentPage - 2 || i === currentPage + 2) {
            buttons += '<span style="padding: 0 0.5rem;">...</span>';
        }
    }
    
    // Next button
    buttons += `<button class="page-btn" onclick="changePage(${currentPage + 1})" 
                ${currentPage === totalPages - 1 ? 'disabled' : ''}>
                Sau <i class="bi bi-chevron-right"></i>
                </button>`;
    
    paginationContainer.innerHTML = `
        <div style="display: flex; align-items: center; justify-content: space-between; padding: 1rem 1.5rem; border-top: 1px solid var(--border-color);">
            <div style="font-size: 0.875rem; color: var(--text-secondary);">
                Hiển thị ${currentPage * pageSize + 1} - ${Math.min((currentPage + 1) * pageSize, data.totalElements)} trong tổng ${data.totalElements} danh mục
            </div>
            <div style="display: flex; gap: 0.5rem;">
                ${buttons}
            </div>
        </div>
    `;
}

// Change Page
function changePage(page) {
    if (page < 0 || page >= totalPages) return;
    currentPage = page;
    loadCategories();
}

// Handle Search
function handleSearch() {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
        currentPage = 0;
        loadCategories();
    }, 500);
}

// Open Add Page
function openAddModal() {
    window.location.href = '/admin/categories/create';
}

// Edit Category
function editCategory(id) {
    window.location.href = `/admin/categories/edit/${id}`;
}

// Toggle Category Status
async function toggleCategoryStatus(id) {
    try {
        const response = await fetch(`/admin/categories/${id}/toggle-active`, {
            method: 'PATCH'
        });
        
        if (response.ok) {
            showNotification('Cập nhật trạng thái thành công', 'success');
            loadCategories();
        } else {
            showNotification('Lỗi khi cập nhật trạng thái', 'error');
        }
    } catch (error) {
        console.error('Error toggling status:', error);
        showNotification('Lỗi khi cập nhật trạng thái', 'error');
    }
}

// Delete Category
async function deleteCategory(id, productCount) {
    if (productCount > 0) {
        showNotification(`Không thể xóa danh mục có ${productCount} sản phẩm`, 'error');
        return;
    }
    
    if (!confirm('Bạn có chắc chắn muốn xóa danh mục này?')) {
        return;
    }
    
    try {
        const response = await fetch(`/admin/categories/${id}`, {
            method: 'DELETE'
        });
        
        if (response.ok) {
            showNotification('Xóa danh mục thành công', 'success');
            loadCategories();
        } else {
            const error = await response.text();
            showNotification(error || 'Lỗi khi xóa danh mục', 'error');
        }
    } catch (error) {
        console.error('Error deleting category:', error);
        showNotification('Lỗi khi xóa danh mục', 'error');
    }
}

// Format Date
function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('vi-VN', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
    }).format(date);
}

// Show Notification
function showNotification(message, type = 'info') {
    // Remove existing notifications
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
