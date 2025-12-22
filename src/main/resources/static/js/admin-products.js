// Admin Products Management JavaScript
let currentPage = 1;
let pageSize = 10;
let totalPages = 1;
let currentFilters = {
    search: '',
    category: '',
    status: ''
};

// Initialize
document.addEventListener('DOMContentLoaded', function() {
    loadCategories();
    loadProducts();
});

// Load Categories for filters and form
async function loadCategories() {
    try {
        const response = await fetch('/api/categories/active');
        const categories = await response.json();
        
        // Populate category filter
        const categoryFilter = document.getElementById('categoryFilter');
        categoryFilter.innerHTML = '<option value="">Tất Cả Danh Mục</option>' +
            categories.map(cat => `<option value="${cat.id}">${cat.name}</option>`).join('');
        
        // Populate product form category
        const productCategory = document.getElementById('productCategory');
        productCategory.innerHTML = '<option value="">Chọn Danh Mục</option>' +
            categories.map(cat => `<option value="${cat.id}">${cat.name}</option>`).join('');
            
    } catch (error) {
        console.error('Error loading categories:', error);
    }
}

// Load Products with pagination (AJAX - no page reload)
async function loadProducts() {
    try {
        const params = new URLSearchParams({
            page: currentPage - 1,
            size: pageSize,
            search: currentFilters.search,
            categoryId: currentFilters.category,
            status: currentFilters.status
        });
        
        const response = await fetch(`/api/inventory/products?${params}`);
        const data = await response.json();
        
        displayProducts(data.content);
        updatePagination(data);
        
    } catch (error) {
        console.error('Error loading products:', error);
    }
}

// Display Products in table
function displayProducts(products) {
    const tbody = document.getElementById('productsTableBody');
    
    if (products.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align: center; padding: 2rem;">Không tìm thấy sản phẩm</td></tr>';
        return;
    }
    
    tbody.innerHTML = products.map(product => `
        <tr>
            <td><input type="checkbox" class="product-checkbox" value="${product.id}"></td>
            <td>
                <div style="display: flex; align-items: center; gap: 1rem;">
                    <img src="${product.imageUrl || '/images/placeholder.jpg'}" 
                         alt="${product.name}"
                         style="width: 50px; height: 50px; object-fit: cover; border-radius: 8px;">
                    <div>
                        <div style="font-weight: 600;">${product.name}</div>
                        <div style="font-size: 0.875rem; color: var(--text-secondary);">SKU: ${product.id}</div>
                    </div>
                </div>
            </td>
            <td>${product.categoryName || 'N/A'}</td>
            <td><strong>${formatCurrency(product.price)}</strong></td>
            <td>${product.stockQuantity} đơn vị</td>
            <td>${getStockStatus(product.stockQuantity)}</td>
            <td>
                <div class="action-buttons">
                    <button class="action-btn" onclick="editProduct(${product.id})" title="Sửa">
                        <i class="bi bi-pencil"></i>
                    </button>
                    <button class="action-btn" onclick="deleteProduct(${product.id})" title="Xóa">
                        <i class="bi bi-trash"></i>
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

// Update Pagination (AJAX - no page reload)
function updatePagination(data) {
    totalPages = data.totalPages;
    const totalProducts = data.totalElements;
    
    // Update info
    document.getElementById('showingStart').textContent = (currentPage - 1) * pageSize + 1;
    document.getElementById('showingEnd').textContent = Math.min(currentPage * pageSize, totalProducts);
    document.getElementById('totalProducts').textContent = totalProducts;
    
    // Generate pagination buttons
    const paginationButtons = document.getElementById('paginationButtons');
    let buttons = '';
    
    // Previous button
    buttons += `<button class="page-btn" onclick="changePage(${currentPage - 1})" 
                ${currentPage === 1 ? 'disabled' : ''}>
                <i class="bi bi-chevron-left"></i>
                </button>`;
    
    // Page numbers
    for (let i = 1; i <= totalPages; i++) {
        if (i === 1 || i === totalPages || (i >= currentPage - 2 && i <= currentPage + 2)) {
            buttons += `<button class="page-btn ${i === currentPage ? 'active' : ''}" 
                        onclick="changePage(${i})">${i}</button>`;
        } else if (i === currentPage - 3 || i === currentPage + 3) {
            buttons += '<span style="padding: 0 0.5rem;">...</span>';
        }
    }
    
    // Next button
    buttons += `<button class="page-btn" onclick="changePage(${currentPage + 1})" 
                ${currentPage === totalPages ? 'disabled' : ''}>
                <i class="bi bi-chevron-right"></i>
                </button>`;
    
    paginationButtons.innerHTML = buttons;
}

// Change Page (AJAX - no page reload)
function changePage(page) {
    if (page < 1 || page > totalPages) return;
    currentPage = page;
    loadProducts();
}

// Search Products
let searchTimeout;
function searchProducts() {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
        currentFilters.search = document.getElementById('searchInput').value;
        currentPage = 1;
        loadProducts();
    }, 500);
}

// Filter Products
function filterProducts() {
    currentFilters.category = document.getElementById('categoryFilter').value;
    currentFilters.status = document.getElementById('statusFilter').value;
    currentPage = 1;
    loadProducts();
}

// Show Add Product Modal - Redirect to create page
function showAddProductModal() {
    window.location.href = '/admin/products/create';
}

// Edit Product - Redirect to edit page
async function editProduct(id) {
    window.location.href = `/admin/products/edit/${id}`;
}

// Delete Product
async function deleteProduct(id) {
    if (!confirm('Bạn có chắc chắn muốn xóa sản phẩm này?')) return;
    
    try {
        const response = await fetch(`/api/products/${id}`, {
            method: 'DELETE'
        });
        
        if (response.ok) {
            loadProducts();
            alert('Đã xóa sản phẩm thành công!');
        } else {
            alert('Lỗi khi xóa sản phẩm');
        }
    } catch (error) {
        console.error('Error deleting product:', error);
        alert('Lỗi khi xóa sản phẩm');
    }
}

// Helper Functions
function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}

function getStockStatus(stock) {
    if (stock === 0) {
        return '<span class="status-badge status-cancelled">Hết Hàng</span>';
    } else if (stock < 10) {
        return '<span class="status-badge status-pending">Sắp Hết</span>';
    } else {
        return '<span class="status-badge status-active">Còn Hàng</span>';
    }
}

function toggleSelectAll() {
    const checkboxes = document.querySelectorAll('.product-checkbox');
    const selectAll = document.getElementById('selectAll').checked;
    checkboxes.forEach(cb => cb.checked = selectAll);
}

function exportProducts() {
    window.location.href = '/admin/api/products/export';
}
