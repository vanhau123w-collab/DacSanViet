package com.dacsanviet.controller;

import com.dacsanviet.model.Supplier;
import com.dacsanviet.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin Supplier Controller
 */
@Controller
@RequestMapping("/admin/suppliers")
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class AdminSupplierController {

    @Autowired
    private SupplierRepository supplierRepository;

    /**
     * Show suppliers management page
     */
    @GetMapping
    public String index(Model model) {
        model.addAttribute("activePage", "suppliers");
        return "admin/suppliers/index";
    }

    /**
     * Get all suppliers with pagination
     */
    @GetMapping("/list")
    @ResponseBody
    public ResponseEntity<?> getSuppliers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
            Page<Supplier> suppliersPage;
            
            if (search != null && !search.trim().isEmpty()) {
                suppliersPage = supplierRepository.searchSuppliers(search, pageable);
            } else if (isActive != null) {
                suppliersPage = supplierRepository.findByIsActiveOrderByNameAsc(isActive, pageable);
            } else {
                suppliersPage = supplierRepository.findAll(pageable);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("content", suppliersPage.getContent());
            response.put("totalElements", suppliersPage.getTotalElements());
            response.put("totalPages", suppliersPage.getTotalPages());
            response.put("currentPage", suppliersPage.getNumber());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error loading suppliers: " + e.getMessage());
        }
    }

    /**
     * Get all active suppliers for dropdown
     */
    @GetMapping("/active")
    @ResponseBody
    public ResponseEntity<?> getActiveSuppliers() {
        try {
            List<Supplier> suppliers = supplierRepository.findByIsActiveTrueOrderByNameAsc();
            return ResponseEntity.ok(suppliers);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error loading suppliers: " + e.getMessage());
        }
    }

    /**
     * Get supplier by ID
     */
    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> getSupplier(@PathVariable Long id) {
        try {
            return supplierRepository.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error loading supplier: " + e.getMessage());
        }
    }

    /**
     * Create new supplier
     */
    @PostMapping
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<?> createSupplier(@RequestBody Supplier supplier) {
        try {
            if (supplier.getName() == null || supplier.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Supplier name is required");
            }
            
            if (supplierRepository.existsByName(supplier.getName())) {
                return ResponseEntity.badRequest().body("Supplier name already exists");
            }
            
            supplier.setIsActive(true);
            Supplier savedSupplier = supplierRepository.save(supplier);
            
            return ResponseEntity.ok(savedSupplier);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating supplier: " + e.getMessage());
        }
    }

    /**
     * Update supplier
     */
    @PutMapping("/{id}")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<?> updateSupplier(@PathVariable Long id, @RequestBody Supplier supplier) {
        try {
            return supplierRepository.findById(id)
                    .map(existingSupplier -> {
                        existingSupplier.setName(supplier.getName());
                        existingSupplier.setContactPerson(supplier.getContactPerson());
                        existingSupplier.setPhone(supplier.getPhone());
                        existingSupplier.setEmail(supplier.getEmail());
                        existingSupplier.setAddress(supplier.getAddress());
                        existingSupplier.setTaxCode(supplier.getTaxCode());
                        existingSupplier.setDescription(supplier.getDescription());
                        existingSupplier.setIsActive(supplier.getIsActive());
                        
                        Supplier updated = supplierRepository.save(existingSupplier);
                        return ResponseEntity.ok(updated);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating supplier: " + e.getMessage());
        }
    }

    /**
     * Delete supplier
     */
    @DeleteMapping("/{id}")
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteSupplier(@PathVariable Long id) {
        try {
            return supplierRepository.findById(id)
                    .map(supplier -> {
                        supplierRepository.delete(supplier);
                        return ResponseEntity.ok().body("Supplier deleted successfully");
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting supplier: " + e.getMessage());
        }
    }

    /**
     * Toggle supplier active status
     */
    @PatchMapping("/{id}/toggle-active")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<?> toggleActive(@PathVariable Long id) {
        try {
            return supplierRepository.findById(id)
                    .map(supplier -> {
                        supplier.setIsActive(!supplier.getIsActive());
                        Supplier updated = supplierRepository.save(supplier);
                        return ResponseEntity.ok(updated);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error toggling supplier status: " + e.getMessage());
        }
    }
}
