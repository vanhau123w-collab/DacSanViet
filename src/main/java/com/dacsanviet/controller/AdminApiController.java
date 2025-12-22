package com.dacsanviet.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dacsanviet.dao.OrderDao;
import com.dacsanviet.dao.UserDao;
import com.dacsanviet.model.Order;
import com.dacsanviet.model.OrderStatus;
import com.dacsanviet.model.User;
import com.dacsanviet.repository.OrderRepository;
import com.dacsanviet.repository.UserRepository;
import com.dacsanviet.service.OrderService;

/**
 * Admin API Controller for AJAX requests
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminApiController {

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private OrderService orderService;

	/**
	 * Get Orders with pagination and filters (AJAX)
	 */
	@GetMapping("/orders")
	@Transactional
	public ResponseEntity<?> getOrders(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, @RequestParam(required = false) String search,
			@RequestParam(required = false) String status, @RequestParam(required = false) String startDate,
			@RequestParam(required = false) String endDate) {

		try {
			Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());

			Page<Order> orders;

			if (status != null && !status.isEmpty()) {
				OrderStatus orderStatus = OrderStatus.valueOf(status);
				orders = orderRepository.findByStatus(orderStatus, pageable);
			} else if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
				LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
				LocalDateTime end = LocalDate.parse(endDate).atTime(23, 59, 59);
				orders = orderRepository.findByOrderDateBetween(start, end, pageable);
			} else {
				orders = orderRepository.findAll(pageable);
			}

			// Convert orders to DAO
			Page<OrderDao> orderDaos = orders.map(order -> {
				try {
					OrderDao dao = orderService.convertToDao(order);
					if (dao == null) {
						System.err.println("convertToDao returned null for order " + order.getId());
					}
					return dao;
				} catch (Exception e) {
					System.err.println("Error converting order " + order.getId() + ": " + e.getMessage());
					e.printStackTrace();

					// Create a minimal DAO as fallback
					OrderDao fallback = new OrderDao();
					fallback.setId(order.getId());
					fallback.setOrderNumber(order.getOrderNumber());
					fallback.setTotalAmount(order.getTotalAmount());
					fallback.setStatus(order.getStatus());
					fallback.setOrderDate(order.getOrderDate());
					fallback.setPaymentStatus(order.getPaymentStatus());
					fallback.setCustomerName(order.getCustomerName());
					fallback.setCustomerEmail(order.getCustomerEmail());
					return fallback;
				}
			});

			return ResponseEntity.ok(orderDaos);
		} catch (Exception e) {
			System.err.println("Error getting orders: " + e.getMessage());
			e.printStackTrace();
			return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
		}
	}

	/**
	 * Get Order by ID (AJAX)
	 */
	@GetMapping("/orders/{id}")
	@Transactional
	public ResponseEntity<?> getOrder(@PathVariable Long id) {
		try {
			Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
			
			// Force load orderItems to avoid LazyInitializationException
			if (order.getOrderItems() != null) {
				order.getOrderItems().size();
			}

			OrderDao dao = orderService.convertToDao(order);

			if (dao == null) {
				System.err.println("convertToDao returned null for order " + id);
				// Create fallback
				dao = new OrderDao();
				dao.setId(order.getId());
				dao.setOrderNumber(order.getOrderNumber());
				dao.setTotalAmount(order.getTotalAmount());
				dao.setShippingFee(order.getShippingFee() != null ? order.getShippingFee() : BigDecimal.ZERO);
				dao.setStatus(order.getStatus());
				dao.setOrderDate(order.getOrderDate());
				dao.setPaymentStatus(order.getPaymentStatus());
				dao.setCustomerName(order.getCustomerName());
				dao.setCustomerEmail(order.getCustomerEmail());
				dao.setCustomerPhone(order.getCustomerPhone());
				dao.setShippingAddressText(order.getShippingAddressText());
				dao.setOrderItems(new ArrayList<>());
			}

			return ResponseEntity.ok(dao);
		} catch (Exception e) {
			System.err.println("Error getting order " + id + ": " + e.getMessage());
			e.printStackTrace();
			return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
		}
	}

	/**
	 * Update Order Status (AJAX)
	 */
	@PutMapping("/orders/{id}/status")
	public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {

		Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));

		String statusStr = request.get("status");
		OrderStatus newStatus = OrderStatus.valueOf(statusStr);

		order.setStatus(newStatus);
		orderRepository.save(order);

		return ResponseEntity.ok(Map.of("message", "Order status updated successfully"));
	}

	/**
	 * Get Users with pagination (AJAX)
	 */
	@GetMapping("/users")
	public ResponseEntity<Page<UserDao>> getUsers(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {

		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		Page<User> users = userRepository.findAll(pageable);

		Page<UserDao> userDaos = users.map(user -> {
			UserDao dao = new UserDao();
			dao.setId(user.getId());
			dao.setUsername(user.getUsername());
			dao.setEmail(user.getEmail());
			dao.setFullName(user.getFullName());
			dao.setPhoneNumber(user.getPhoneNumber());
			dao.setCreatedAt(user.getCreatedAt());

			// Calculate total orders and spent
			long totalOrders = orderRepository.countByUserId(user.getId());
			dao.setTotalOrders(totalOrders);

			return dao;
		});

		return ResponseEntity.ok(userDaos);
	}

	/**
	 * Export Orders CSV
	 */
	@GetMapping("/orders/export")
	public ResponseEntity<String> exportOrders() {
		// TODO: Implement CSV export
		return ResponseEntity.ok("CSV export not implemented yet");
	}

	/**
	 * Export Customers CSV
	 */
	@GetMapping("/customers/export")
	public ResponseEntity<String> exportCustomers() {
		// TODO: Implement CSV export
		return ResponseEntity.ok("CSV export not implemented yet");
	}

	/**
	 * Export Analytics Report
	 */
	@GetMapping("/analytics/export")
	public ResponseEntity<String> exportAnalytics(@RequestParam String period) {
		// TODO: Implement report export
		return ResponseEntity.ok("Analytics export not implemented yet");
	}
}
