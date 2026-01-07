package com.dacsanviet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for policy pages
 */
@Controller
public class PolicyController {

	@GetMapping("/shipping-policy")
	public String shippingPolicy(Model model) {
		model.addAttribute("pageTitle", "Chính Sách Vận Chuyển");
		return "shipping-policy";
	}

	@GetMapping("/privacy-policy")
	public String privacyPolicy(Model model) {
		model.addAttribute("pageTitle", "Chính Sách Bảo Mật");
		return "privacy-policy";
	}

	@GetMapping("/terms-of-service")
	public String termsOfService(Model model) {
		model.addAttribute("pageTitle", "Điều Khoản Sử Dụng");
		return "terms-of-service";
	}
}
