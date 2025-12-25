// Custom Select Class for Supplier Dropdown
class CustomSelect {
    constructor(buttonId, dropdownId, searchId, optionsId) {
        this.button = document.getElementById(buttonId);
        this.dropdown = document.getElementById(dropdownId);
        this.searchInput = document.getElementById(searchId);
        this.optionsContainer = document.getElementById(optionsId);
        this.options = [];
        this.selectedValue = null;
        this.onSelect = null;
        
        this.init();
    }
    
    init() {
        // Toggle dropdown
        this.button.addEventListener('click', (e) => {
            e.stopPropagation();
            this.toggle();
        });
        
        // Search functionality
        this.searchInput.addEventListener('input', () => {
            this.filterOptions(this.searchInput.value);
        });
        
        // Close on outside click
        document.addEventListener('click', (e) => {
            if (!this.button.contains(e.target) && !this.dropdown.contains(e.target)) {
                this.close();
            }
        });
    }
    
    toggle() {
        const isOpen = this.dropdown.classList.contains('show');
        if (isOpen) {
            this.close();
        } else {
            this.open();
        }
    }
    
    open() {
        this.dropdown.classList.add('show');
        this.button.classList.add('active');
        this.searchInput.value = '';
        this.searchInput.focus();
        this.filterOptions('');
    }
    
    close() {
        this.dropdown.classList.remove('show');
        this.button.classList.remove('active');
    }
    
    setOptions(options) {
        this.options = options;
        this.renderOptions(options);
    }
    
    renderOptions(options) {
        this.optionsContainer.innerHTML = options.map(opt => `
            <div class="custom-select-option" data-value="${opt.id || opt.code}" data-name="${opt.name}">
                ${opt.name}
            </div>
        `).join('');
        
        // Add click handlers
        this.optionsContainer.querySelectorAll('.custom-select-option').forEach(option => {
            option.addEventListener('click', () => {
                const value = option.dataset.value;
                const name = option.dataset.name;
                this.select(value, name);
            });
        });
    }
    
    filterOptions(searchTerm) {
        const filtered = this.options.filter(opt => 
            opt.name.toLowerCase().includes(searchTerm.toLowerCase())
        );
        this.renderOptions(filtered);
    }
    
    select(value, name) {
        this.selectedValue = { value, name };
        this.button.innerHTML = `
            <span class="select-value">${name}</span>
            <i class="bi bi-chevron-down"></i>
        `;
        this.close();
        
        if (this.onSelect) {
            this.onSelect(value, name);
        }
    }
    
    reset() {
        this.selectedValue = null;
        this.button.innerHTML = `
            <span class="select-placeholder">Chọn...</span>
            <i class="bi bi-chevron-down"></i>
        `;
    }
}

// Initialize supplier dropdown
let supplierSelect, supplierProvinceSelect;
let provinces = [];

// Load suppliers and provinces on page load
document.addEventListener('DOMContentLoaded', function() {
    // Initialize supplier dropdown
    supplierSelect = new CustomSelect('supplierBtn', 'supplierDropdown', 'supplierSearch', 'supplierOptions');
    supplierSelect.onSelect = (value, name) => {
        document.getElementById('supplierId').value = value;
    };
    
    loadSuppliers();
});

// Load suppliers for dropdown
async function loadSuppliers() {
    try {
        const response = await fetch('/admin/suppliers/active');
        const suppliers = await response.json();
        supplierSelect.setOptions(suppliers);
    } catch (error) {
        console.error('Error loading suppliers:', error);
    }
}

// Load provinces for modal
async function loadProvinces() {
    try {
        const response = await fetch('https://provinces.open-api.vn/api/p/');
        provinces = await response.json();
        
        if (supplierProvinceSelect) {
            supplierProvinceSelect.setOptions(provinces);
        }
    } catch (error) {
        console.error('Error loading provinces:', error);
    }
}

// Open supplier modal
function openSupplierModal() {
    const modal = document.getElementById('supplierModal');
    
    document.getElementById('supplierForm').reset();
    modal.classList.add('show');
    
    // Initialize province dropdown if not already done
    if (!supplierProvinceSelect) {
        supplierProvinceSelect = new CustomSelect(
            'supplierProvinceBtn', 
            'supplierProvinceDropdown', 
            'supplierProvinceSearch', 
            'supplierProvinceOptions'
        );
        supplierProvinceSelect.onSelect = (code, name) => {
            document.getElementById('supplierProvince').value = name;
        };
        loadProvinces();
    }
}

// Close supplier modal
function closeSupplierModal() {
    const modal = document.getElementById('supplierModal');
    modal.classList.remove('show');
}

// Save supplier
async function saveSupplier(event) {
    event.preventDefault();
    
    const supplierData = {
        name: document.getElementById('supplierName').value.trim(),
        contactPerson: document.getElementById('supplierContactPerson').value.trim(),
        phone: document.getElementById('supplierPhone').value.trim(),
        email: document.getElementById('supplierEmail').value.trim(),
        address: document.getElementById('supplierAddress').value.trim(),
        city: document.getElementById('supplierCity').value.trim(),
        province: document.getElementById('supplierProvince').value.trim()
    };
    
    try {
        const response = await fetch('/admin/suppliers/quick-add', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(supplierData)
        });
        
        if (response.ok) {
            const newSupplier = await response.json();
            
            // Reload suppliers and select the new one
            await loadSuppliers();
            supplierSelect.select(newSupplier.id, newSupplier.name);
            document.getElementById('supplierId').value = newSupplier.id;
            
            showNotification('Thêm nhà phân phối thành công', 'success');
            closeSupplierModal();
        } else {
            const error = await response.text();
            showNotification(error || 'Có lỗi xảy ra', 'error');
        }
    } catch (error) {
        console.error('Error saving supplier:', error);
        showNotification('Lỗi khi lưu nhà phân phối', 'error');
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
