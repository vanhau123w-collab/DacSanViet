package com.dacsanviet.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Order entity representing customer orders
 */
@Entity
@Table(name = "orders", indexes = { @Index(name = "idx_order_user", columnList = "user_id"),
		@Index(name = "idx_order_status", columnList = "status"),
		@Index(name = "idx_order_date", columnList = "order_date"),
		@Index(name = "idx_order_number", columnList = "order_number") })
@Getter
@Setter
@NoArgsConstructor
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "order_number", unique = true, nullable = false, length = 50)
	private String orderNumber;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = true)
	private User user;

	@Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
	@DecimalMin(value = "0.0", inclusive = false, message = "Total amount must be greater than 0")
	private BigDecimal totalAmount;

	@Column(name = "shipping_fee", precision = 10, scale = 2)
	private BigDecimal shippingFee = BigDecimal.ZERO;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OrderStatus status = OrderStatus.PENDING;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<OrderItem> orderItems = new ArrayList<>();

	@Column(name = "order_date", nullable = false)
	private LocalDateTime orderDate;

	@Column(name = "shipped_date")
	private LocalDateTime shippedDate;

	@Column(name = "delivered_date")
	private LocalDateTime deliveredDate;

	@Column(name = "tracking_number", length = 100)
	@Size(max = 100, message = "Tracking number must not exceed 100 characters")
	private String trackingNumber;

	@Column(name = "payment_method", length = 50)
	@Size(max = 50, message = "Payment method must not exceed 50 characters")
	private String paymentMethod;

	@Enumerated(EnumType.STRING)
	@Column(name = "payment_status")
	private PaymentStatus paymentStatus = PaymentStatus.PENDING;
	
	@Column(name = "shipping_method", length = 50)
	@Size(max = 50, message = "Shipping method must not exceed 50 characters")
	private String shippingMethod;
	
	@Column(name = "shipping_carrier", length = 100)
	@Size(max = 100, message = "Shipping carrier must not exceed 100 characters")
	private String shippingCarrier;

	@Column(name = "customer_name", length = 100)
	@Size(max = 100, message = "Customer name must not exceed 100 characters")
	private String customerName;

	@Column(name = "customer_phone", length = 20)
	@Size(max = 20, message = "Customer phone must not exceed 20 characters")
	private String customerPhone;

	@Column(name = "customer_email", length = 100)
	@Size(max = 100, message = "Customer email must not exceed 100 characters")
	private String customerEmail;

	@Column(name = "shipping_address_text", length = 500)
	@Size(max = 500, message = "Shipping address must not exceed 500 characters")
	private String shippingAddressText;

	@Column(name = "notes", length = 1000)
	@Size(max = 1000, message = "Notes must not exceed 1000 characters")
	private String notes;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	// Constructor with required fields
	public Order(User user, BigDecimal totalAmount) {
		this.orderDate = LocalDateTime.now();
		this.orderNumber = generateOrderNumber();
		this.user = user;
		this.totalAmount = totalAmount;
	}

	// Custom Setter for Status with auto-date setting
	public void setStatus(OrderStatus status) {
		this.status = status;

		// Auto-set dates based on status
		if (status == OrderStatus.SHIPPED && shippedDate == null) {
			shippedDate = LocalDateTime.now();
		} else if (status == OrderStatus.DELIVERED && deliveredDate == null) {
			deliveredDate = LocalDateTime.now();
		}
	}

	// Helper methods
	public void addOrderItem(OrderItem orderItem) {
		orderItems.add(orderItem);
		orderItem.setOrder(this);
	}

	public void removeOrderItem(OrderItem orderItem) {
		orderItems.remove(orderItem);
		orderItem.setOrder(null);
	}

	public BigDecimal calculateSubtotal() {
		return orderItems.stream().map(OrderItem::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public BigDecimal calculateGrandTotal() {
		BigDecimal subtotal = calculateSubtotal();
		BigDecimal shipping = shippingFee != null ? shippingFee : BigDecimal.ZERO;
		return subtotal.add(shipping);
	}

	public Integer getTotalItems() {
		return orderItems.stream().mapToInt(OrderItem::getQuantity).sum();
	}

	public boolean canBeCancelled() {
		return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
	}

	public boolean isCompleted() {
		return status == OrderStatus.DELIVERED;
	}

	private String generateOrderNumber() {
		// Generate order number: DSV{yy}{mm}{dd}{random_6_chars}
		// Example: DSV2412250A3B5C
		LocalDateTime now = LocalDateTime.now();
		String year = String.format("%02d", now.getYear() % 100);
		String month = String.format("%02d", now.getMonthValue());
		String day = String.format("%02d", now.getDayOfMonth());
		
		// Generate 6 random alphanumeric characters
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		StringBuilder random = new StringBuilder();
		java.util.Random rnd = new java.util.Random();
		for (int i = 0; i < 6; i++) {
			random.append(chars.charAt(rnd.nextInt(chars.length())));
		}
		
		return "DSV" + year + month + day + random.toString();
	}
}