// Admin Promotions Management JavaScript
let currentPage = 0;
let pageSize = 10;
let totalPages = 1;
let currentFilters = {
    search: '',
    status: ''
};

// Initialize
document.addEventListener('DOMContentLoaded', function() {
    loadPromotions();
});

// Load Promotions
async function loadPromotions() {
    try {
        const params = new URLSearchParams({
            page: currentPage,
            size: pageSize,
            search: currentFilters.search,
            status: currentFilters.status
        });
        
        const response = await fetch(`/admin/promotions/api/list?${params}`);
        const data = await response.json();
        
        displayPromotions(data.content);
        updatePagination(data);
        
    } catch (error) {
        console.error('Error loading promotions:', error);
        showNotification('Lỗi khi tải danh sách khuyến mãi', 'error');
    }
}

// Display Promotions
function displayPromotions(promotions) {
    const tbody = document.getElementById('promotionsTableBody');
    
    if (!promotions || promotions.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" style="text-align: center; padding: 2rem;">Không tìm thấy khuyến mãi</td></tr>';
        return;
    }
    
    tbody.innerHTML = promotions.map(promo => {
        const now = new Date();
        const startDate = new Date(promo.startDate);
        const endDate = new Date(promo.endDate);
        
        let statusClass = 'pending';
        let statusText = 'Chưa bắt đầu';
        
        if (now >= startDate && now <= endDate && promo.isActive) {
            statusClass = 'active';
            statusText = 'Đang hoạt động';
        } else if (now > endDate) {
            statusClass = 'cancelled';
            statusText = 'Đã hết hạn';
        } else if (!promo.isActive) {
            statusClass = 'cancelled';
            statusText = 'Tạm dừng';
        }
        
        const discountDisplay = promo.discountType === 'PERCENTAGE' 
            ? `${promo.discountValue}%` 
            : formatCurrency(promo.discountValue);
        
        const usageDisplay = promo.usageLimit 
            ? `${promo.usedCount}/${promo.usageLimit}` 
            : `${promo.usedCount}/∞`;
        
        return `
            <tr>
                <td><strong>${promo.code}</strong></td>
                <td>${promo.description || '-'}</td>
                <td>${promo.discountType === 'PERCENTAGE' ? 'Phần trăm' : 'Số tiền'}</td>
                <td><strong>${discountDisplay}</strong></td>
                <td>${usageDisplay}</td>
                <td>
                    <div style="font-size: 0.875rem;">
                        <div>${formatDateTime(promo.startDate)}</div>
                        <div style="color: var(--text-secondary);">đến ${formatDateTime(promo.endDate)}</div>
                    </div>
                </td>
                <td><span class="status-badge status-${statusClass}">${statusText}</span></td>
                <td>
                    <div class="action-buttons">
                        <button class="action-btn" onclick="editPromotion(${promo.id})" title="Sửa">
                            <i class="bi bi-pencil"></i>
                        </button>
                        <button class="action-btn" onclick="toggleStatus(${promo.id})" title="${promo.isActive ? 'Tắt' : 'Bật'}">
                            <i class="bi bi-${promo.isActive ? 'toggle-on' : 'toggle-off'}"></i>
                        </button>
                        <button class="action-btn" onclick="deletePromotion(${promo.id})" title="Xóa">
                            <i class="bi bi-trash"></i>
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

// Update Pagination
function updatePagination(data) {
    totalPages = data.totalPages;
    const totalPromotions = data.totalElements;
    
    document.getElementById('showingStart').textContent = currentPage * pageSize + 1;
    document.getElementById('showingEnd').textContent = Math.min((currentPage + 1) * pageSize, totalPromotions);
    document.getElementById('totalPromotions').textContent = totalPromotions;
    
    const paginationButtons = document.getElementById('paginationButtons');
    let buttons = '';
    
    buttons += `<button class="page-btn" onclick="changePage(${currentPage - 1})" 
                ${currentPage === 0 ? 'disabled' : ''}>
                <i class="bi bi-chevron-left"></i>
                </button>`;
    
    for (let i = 0; i < totalPages; i++) {
        if (i === 0 || i === totalPages - 1 || (i >= currentPage - 2 && i <= currentPage + 2)) {
            buttons += `<button class="page-btn ${i === currentPage ? 'active' : ''}" 
                        onclick="changePage(${i})">${i + 1}</button>`;
        } else if (i === currentPage - 3 || i === currentPage + 3) {
            buttons += '<span style="padding: 0 0.5rem;">...</span>';
        }
    }
    
    buttons += `<button class="page-btn" onclick="changePage(${currentPage + 1})" 
                ${currentPage === totalPages - 1 ? 'disabled' : ''}>
                <i class="bi bi-chevron-right"></i>
                </button>`;
    
    paginationButtons.innerHTML = buttons;
}

// Change Page
function changePage(page) {
    if (page < 0 || page >= totalPages) return;
    currentPage = page;
    loadPromotions();
}

// Search Promotions
let searchTimeout;
function searchPromotions() {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
        currentFilters.search = document.getElementById('searchInput').value;
        currentPage = 0;
        loadPromotions();
    }, 500);
}

// Filter Promotions
function filterPromotions() {
    currentFilters.status = document.getElementById('statusFilter').value;
    currentPage = 0;
    loadPromotions();
}

// Show Add Modal
function showAddModal() {
    document.getElementById('modalTitle').textContent = 'Thêm Khuyến Mãi';
    document.getElementById('promotionForm').reset();
    document.getElementById('promotionId').value = '';
    document.getElementById('isActive').checked = true;
    
    // Set default dates
    const now = new Date();
    const tomorrow = new Date(now);
    tomorrow.setDate(tomorrow.getDate() + 1);
    const nextMonth = new Date(now);
    nextMonth.setMonth(nextMonth.getMonth() + 1);
    
    document.getElementById('startDate').value = formatDateTimeLocal(tomorrow);
    document.getElementById('endDate').value = formatDateTimeLocal(nextMonth);
    
    updateDiscountFields();
    document.getElementById('promotionModal').classList.add('show');
}

// Edit Promotion
async function editPromotion(id) {
    try {
        const response = await fetch(`/admin/promotions/api/${id}`);
        const promo = await response.json();
        
        document.getElementById('modalTitle').textContent = 'Chỉnh Sửa Khuyến Mãi';
        document.getElementById('promotionId').value = promo.id;
        document.getElementById('code').value = promo.code;
        document.getElementById('description').value = promo.description || '';
        document.getElementById('discountType').value = promo.discountType;
        document.getElementById('discountValue').value = promo.discountValue;
        document.getElementById('maxDiscountAmount').value = promo.maxDiscountAmount || '';
        document.getElementById('minOrderValue').value = promo.minOrderValue || 0;
        document.getElementById('usageLimit').value = promo.usageLimit || '';
        document.getElementById('startDate').value = formatDateTimeLocal(new Date(promo.startDate));
        document.getElementById('endDate').value = formatDateTimeLocal(new Date(promo.endDate));
        document.getElementById('isActive').checked = promo.isActive;
        
        updateDiscountFields();
        document.getElementById('promotionModal').classList.add('show');
        
    } catch (error) {
        console.error('Error loading promotion:', error);
        showNotification('Lỗi khi tải thông tin khuyến mãi', 'error');
    }
}

// Save Promotion
async function savePromotion() {
    const id = document.getElementById('promotionId').value;
    
    // Get datetime values and convert to ISO string without timezone conversion
    const startDateInput = document.getElementById('startDate').value;
    const endDateInput = document.getElementById('endDate').value;
    
    // Parse as local time and format as ISO string
    const startDate = new Date(startDateInput);
    const endDate = new Date(endDateInput);
    
    const data = {
        code: document.getElementById('code').value.toUpperCase().trim(),
        description: document.getElementById('description').value.trim(),
        discountType: document.getElementById('discountType').value,
        discountValue: parseFloat(document.getElementById('discountValue').value),
        maxDiscountAmount: document.getElementById('maxDiscountAmount').value 
            ? parseFloat(document.getElementById('maxDiscountAmount').value) : null,
        minOrderValue: parseFloat(document.getElementById('minOrderValue').value) || 0,
        usageLimit: document.getElementById('usageLimit').value 
            ? parseInt(document.getElementById('usageLimit').value) : null,
        // Send as local datetime string (server will parse as local time)
        startDate: startDateInput,
        endDate: endDateInput,
        isActive: document.getElementById('isActive').checked
    };
    
    // Validation
    if (!data.code || !data.discountValue || !data.startDate || !data.endDate) {
        showNotification('Vui lòng điền đầy đủ thông tin bắt buộc', 'error');
        return;
    }
    
    if (data.discountType === 'PERCENTAGE' && (data.discountValue < 0 || data.discountValue > 100)) {
        showNotification('Giá trị giảm % phải từ 0-100', 'error');
        return;
    }
    
    if (endDate <= startDate) {
        showNotification('Ngày kết thúc phải sau ngày bắt đầu', 'error');
        return;
    }
    
    try {
        const url = id ? `/admin/promotions/api/${id}` : '/admin/promotions/api/create';
        const method = id ? 'PUT' : 'POST';
        
        const response = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        
        if (response.ok) {
            showNotification(id ? 'Đã cập nhật khuyến mãi' : 'Đã thêm khuyến mãi mới', 'success');
            closeModal();
            loadPromotions();
        } else {
            const error = await response.json();
            showNotification(error.error || 'Có lỗi xảy ra', 'error');
        }
    } catch (error) {
        console.error('Error saving promotion:', error);
        showNotification('Lỗi khi lưu khuyến mãi', 'error');
    }
}

// Delete Promotion
async function deletePromotion(id) {
    if (!confirm('Bạn có chắc muốn xóa khuyến mãi này?')) return;
    
    try {
        const response = await fetch(`/admin/promotions/api/${id}`, {
            method: 'DELETE'
        });
        
        if (response.ok) {
            showNotification('Đã xóa khuyến mãi', 'success');
            loadPromotions();
        } else {
            const error = await response.json();
            showNotification(error.error || 'Không thể xóa khuyến mãi', 'error');
        }
    } catch (error) {
        console.error('Error deleting promotion:', error);
        showNotification('Lỗi khi xóa khuyến mãi', 'error');
    }
}

// Toggle Status
async function toggleStatus(id) {
    try {
        const response = await fetch(`/admin/promotions/api/${id}/toggle`, {
            method: 'PUT'
        });
        
        if (response.ok) {
            showNotification('Đã cập nhật trạng thái', 'success');
            loadPromotions();
        } else {
            showNotification('Không thể cập nhật trạng thái', 'error');
        }
    } catch (error) {
        console.error('Error toggling status:', error);
        showNotification('Lỗi khi cập nhật trạng thái', 'error');
    }
}

// Update Discount Fields
function updateDiscountFields() {
    const type = document.getElementById('discountType').value;
    const hint = document.getElementById('discountHint');
    const maxField = document.getElementById('maxDiscountAmount');
    
    if (type === 'PERCENTAGE') {
        hint.textContent = 'Nhập % giảm giá (0-100)';
        maxField.disabled = false;
        maxField.parentElement.style.opacity = '1';
    } else {
        hint.textContent = 'Nhập số tiền giảm (VNĐ)';
        maxField.disabled = true;
        maxField.value = '';
        maxField.parentElement.style.opacity = '0.5';
    }
}

// Close Modal
function closeModal() {
    document.getElementById('promotionModal').classList.remove('show');
}

// Helper Functions
function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}

function formatDateTime(dateString) {
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    }).format(date);
}

function formatDateTimeLocal(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}`;
}

function showNotification(message, type = 'info') {
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
    setTimeout(() => notification.classList.add('show'), 10);
    setTimeout(() => {
        notification.classList.remove('show');
        setTimeout(() => notification.remove(), 300);
    }, 3000);
}
