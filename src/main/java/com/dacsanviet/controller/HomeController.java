package com.dacsanviet.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dacsanviet.dao.CategoryDao;
import com.dacsanviet.dao.ProductDao;
import com.dacsanviet.service.CategoryService;
import com.dacsanviet.service.EmailService;
import com.dacsanviet.service.ProductService;

import jakarta.validation.Valid;

/**
 * Home controller for basic navigation and public pages
 */
@Controller
public class HomeController {

	@Autowired
	private ProductService productService;

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private EmailService emailService;

	@GetMapping("/")
	public String home(Model model) {
		try {
			// Get featured products for homepage
			Pageable featuredPageable = PageRequest.of(0, 8);
			Page<ProductDao> featuredProducts = productService.getFeaturedProducts(featuredPageable);
			model.addAttribute("featuredProducts", featuredProducts.getContent());

			// Get new products for homepage (load more for pagination)
			Pageable newPageable = PageRequest.of(0, 20);
			Page<ProductDao> newProducts = productService.getAllProducts(newPageable);
			model.addAttribute("newProducts", newProducts.getContent());

			// Get active categories
			List<CategoryDao> categories = categoryService.getAllActiveCategories();
			model.addAttribute("categories", categories);

		} catch (Exception e) {
			// If there's an error, just continue without data
			model.addAttribute("featuredProducts", List.of());
			model.addAttribute("newProducts", List.of());
			model.addAttribute("categories", List.of());
		}

		return "index";
	}

	@GetMapping("/health")
	public String health() {
		return "index";
	}

	// Products mapping moved to ProductController

	@GetMapping("/products/search")
	public String searchProducts(Model model, @RequestParam(required = false) String keyword,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "12") int size) {
		try {
			model.addAttribute("pageTitle", "Tìm Kiếm Sản Phẩm");
			model.addAttribute("searchKeyword", keyword);

			if (keyword != null && !keyword.trim().isEmpty()) {
				Pageable pageable = PageRequest.of(page, size);
				Page<ProductDao> products = productService.searchProducts(keyword.trim(), null, pageable);

				model.addAttribute("products", products.getContent());
				model.addAttribute("currentPage", page);
				model.addAttribute("totalPages", products.getTotalPages());
				model.addAttribute("totalElements", products.getTotalElements());
			} else {
				model.addAttribute("products", List.of());
			}

		} catch (Exception e) {
			model.addAttribute("products", List.of());
			model.addAttribute("error", "Không thể tìm kiếm sản phẩm");
		}

		return "products/search";
	}

	@GetMapping("/categories")
	public String categories(Model model) {
		try {
			model.addAttribute("pageTitle", "Danh Mục Sản Phẩm");

			// Get categories with product count
			List<CategoryDao> categories = categoryService.getCategoriesWithProductCount();
			model.addAttribute("categories", categories);

		} catch (Exception e) {
			model.addAttribute("categories", List.of());
			model.addAttribute("error", "Không thể tải danh sách danh mục");
		}

		return "categories/index";
	}

	@GetMapping("/login")
	public String login(Model model) {
		model.addAttribute("pageTitle", "Đăng Nhập");
		model.addAttribute("categories", categoryService.getAllActiveCategories());
		return "auth/login";
	}

	@GetMapping("/register")
	public String register(Model model) {
		model.addAttribute("pageTitle", "Đăng Ký");
		model.addAttribute("categories", categoryService.getAllActiveCategories());
		return "auth/register";
	}

	@GetMapping("/test-simple")
	public String testSimple() {
		return "test-simple";
	}

	@GetMapping("/privacy-policy")
	public String privacyPolicy(Model model) {
		model.addAttribute("pageTitle", "Chính Sách Bảo Mật");
		model.addAttribute("categories", categoryService.getAllActiveCategories());
		return "privacy-policy";
	}

	@GetMapping("/terms-of-service")
	public String termsOfService(Model model) {
		model.addAttribute("pageTitle", "Điều Khoản Sử Dụng");
		model.addAttribute("categories", categoryService.getAllActiveCategories());
		return "terms-of-service";
	}

	@GetMapping("/about")
	public String about(Model model) {
		model.addAttribute("pageTitle", "Giới Thiệu - Đặc Sản Việt Nam");
		model.addAttribute("categories", categoryService.getAllActiveCategories());
		return "pages/about";
	}

	@PostMapping("/api/consultation")
	@ResponseBody
	public org.springframework.http.ResponseEntity<?> submitConsultation(
			@Valid @RequestBody com.dacsanviet.dto.ConsultationRequest request) {
		try {
			// Log consultation request
			System.out.println("=== YÊU CẦU TƯ VẤN MỚI ===");
			System.out.println("Tên: " + request.getName());
			System.out.println("Số điện thoại: " + request.getPhone());
			System.out.println("Email: " + request.getEmail());
			System.out.println("Quan tâm: " + request.getInterest());
			System.out.println("Ghi chú: " + request.getMessage());
			System.out.println("Thời gian: " + java.time.LocalDateTime.now());
			System.out.println("Gửi đến: dacsanviethotro@gmail.com");
			System.out.println("========================");

			// Send email notification with timeout handling
			try {
				emailService.sendConsultationEmail(request);
				System.out.println("✅ Email sent successfully!");
			} catch (Exception emailError) {
				System.err.println("❌ Failed to send email: " + emailError.getMessage());
				emailError.printStackTrace();
				// Continue anyway - don't fail the user request
			}

			java.util.Map<String, String> response = new java.util.HashMap<>();
			response.put("message", "Cảm ơn bạn! Chúng tôi sẽ liên hệ tư vấn trong vòng 24h.");
			return org.springframework.http.ResponseEntity.ok(response);

		} catch (Exception e) {
			System.err.println("❌ Error in consultation endpoint: " + e.getMessage());
			e.printStackTrace();
			java.util.Map<String, String> error = new java.util.HashMap<>();
			error.put("error", "Có lỗi xảy ra. Vui lòng thử lại sau!");
			return org.springframework.http.ResponseEntity.status(500).body(error);
		}
	}

}