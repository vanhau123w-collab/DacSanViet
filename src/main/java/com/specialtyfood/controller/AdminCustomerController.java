package com.specialtyfood.controller;

import com.specialtyfood.dto.CreateUserRequest;
import com.specialtyfood.dto.UpdateUserRequest;
import com.specialtyfood.dao.UserDao;
import com.specialtyfood.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

/**
 * Admin Customer Controller for customer management
 */
@Controller
@RequestMapping("/admin/customers")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCustomerController {
    
    @Autowired
    private UserService userService;
    
    /**
     * List all customers (admin view)
     */
    @GetMapping
    public String listCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String searchTerm,
            Model model) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : 
                   Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<UserDao> customers;
        
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            customers = userService.searchUsers(searchTerm, pageable);
            model.addAttribute("searchTerm", searchTerm);
        } else {
            customers = userService.getAllUsers(pageable);
        }
        
        model.addAttribute("customers", customers);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("pageTitle", "Quản Lý Khách Hàng");
        
        return "admin/customers/simple-list";
    }
    
    /**
     * Show customer details
     */
    @GetMapping("/{id}")
    public String viewCustomer(@PathVariable Long id, Model model) {
        try {
            UserDao customer = userService.getUserById(id);
            model.addAttribute("customer", customer);
            model.addAttribute("pageTitle", "Chi Tiết Khách Hàng - " + customer.getFullName());
            return "admin/customers/simple-view";
        } catch (RuntimeException e) {
            model.addAttribute("error", "Không tìm thấy khách hàng");
            return "redirect:/admin/customers";
        }
    }
    
    /**
     * Show create customer form
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("customer", new CreateUserRequest());
        model.addAttribute("pageTitle", "Thêm Khách Hàng Mới");
        return "admin/customers/simple-create";
    }
    
    /**
     * Handle create customer form submission
     */
    @PostMapping("/create")
    public String createCustomer(
            @Valid @ModelAttribute("customer") CreateUserRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Thêm Khách Hàng Mới");
            return "admin/customers/simple-create";
        }
        
        try {
            UserDao createdCustomer = userService.createUser(request);
            redirectAttributes.addFlashAttribute("message", 
                "Khách hàng '" + createdCustomer.getUsername() + "' đã được tạo thành công!");
            
            return "redirect:/admin/customers";
            
        } catch (RuntimeException e) {
            model.addAttribute("error", "Lỗi tạo khách hàng: " + e.getMessage());
            model.addAttribute("pageTitle", "Thêm Khách Hàng Mới");
            return "admin/customers/simple-create";
        }
    }
    
    /**
     * Show edit customer form
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            UserDao customer = userService.getUserById(id);
            
            // Convert UserDao to UpdateUserRequest
            UpdateUserRequest updateRequest = new UpdateUserRequest();
            updateRequest.setUsername(customer.getUsername());
            updateRequest.setEmail(customer.getEmail());
            updateRequest.setFullName(customer.getFullName());
            updateRequest.setPhoneNumber(customer.getPhoneNumber());
            updateRequest.setAdmin(customer.getAdmin());
            updateRequest.setIsActive(customer.getIsActive());
            
            model.addAttribute("customer", updateRequest);
            model.addAttribute("customerId", id);
            model.addAttribute("pageTitle", "Chỉnh Sửa Khách Hàng - " + customer.getUsername());
            
            return "admin/customers/simple-edit";
            
        } catch (RuntimeException e) {
            model.addAttribute("error", "Không tìm thấy khách hàng");
            return "redirect:/admin/customers";
        }
    }
    
    /**
     * Handle edit customer form submission
     */
    @PostMapping("/{id}/edit")
    public String updateCustomer(
            @PathVariable Long id,
            @Valid @ModelAttribute("customer") UpdateUserRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("customerId", id);
            model.addAttribute("pageTitle", "Chỉnh Sửa Khách Hàng");
            return "admin/customers/simple-edit";
        }
        
        try {
            UserDao updatedCustomer = userService.updateUser(id, request);
            redirectAttributes.addFlashAttribute("message", 
                "Khách hàng '" + updatedCustomer.getUsername() + "' đã được cập nhật thành công!");
            
            return "redirect:/admin/customers";
            
        } catch (RuntimeException e) {
            model.addAttribute("customerId", id);
            model.addAttribute("error", "Lỗi cập nhật khách hàng: " + e.getMessage());
            model.addAttribute("pageTitle", "Chỉnh Sửa Khách Hàng");
            return "admin/customers/simple-edit";
        }
    }
    
    /**
     * Delete customer (soft delete)
     */
    @PostMapping("/{id}/delete")
    public String deleteCustomer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            UserDao customer = userService.getUserById(id);
            userService.deleteUser(id);
            
            redirectAttributes.addFlashAttribute("message", 
                "Khách hàng '" + customer.getUsername() + "' đã được xóa thành công!");
            
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi xóa khách hàng: " + e.getMessage());
        }
        
        return "redirect:/admin/customers";
    }
    
    /**
     * Toggle customer active status
     */
    @PostMapping("/{id}/toggle-status")
    public String toggleCustomerStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            UserDao customer = userService.toggleUserStatus(id);
            String status = customer.getIsActive() ? "kích hoạt" : "tạm khóa";
            
            redirectAttributes.addFlashAttribute("message", 
                "Khách hàng '" + customer.getUsername() + "' đã được " + status + " thành công!");
            
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi thay đổi trạng thái khách hàng: " + e.getMessage());
        }
        
        return "redirect:/admin/customers";
    }
}