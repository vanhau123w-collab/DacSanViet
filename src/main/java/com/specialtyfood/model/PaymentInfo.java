package com.specialtyfood.model;

import com.specialtyfood.config.EncryptedStringConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Payment information entity with encrypted sensitive fields
 */
@Entity
@Table(name = "payment_info", indexes = {
    @Index(name = "idx_payment_user_id", columnList = "user_id"),
    @Index(name = "idx_payment_order_id", columnList = "order_id")
})
public class PaymentInfo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
    
    @Column(name = "payment_method", nullable = false, length = 50)
    @NotBlank(message = "Payment method is required")
    private String paymentMethod; // CREDIT_CARD, DEBIT_CARD, PAYPAL, etc.
    
    // Encrypted fields for sensitive payment data
    @Column(name = "card_number", length = 500)
    @Convert(converter = EncryptedStringConverter.class)
    private String cardNumber; // Encrypted
    
    @Column(name = "card_holder_name", length = 500)
    @Convert(converter = EncryptedStringConverter.class)
    private String cardHolderName; // Encrypted
    
    @Column(name = "expiry_month")
    private Integer expiryMonth;
    
    @Column(name = "expiry_year")
    private Integer expiryYear;
    
    // CVV should never be stored - this is just for demonstration
    // In real applications, CVV should only be used for transaction and immediately discarded
    @Transient
    private String cvv;
    
    @Column(name = "billing_address", length = 1000)
    @Convert(converter = EncryptedStringConverter.class)
    private String billingAddress; // Encrypted
    
    @Column(name = "payment_processor", length = 100)
    private String paymentProcessor; // Stripe, PayPal, etc.
    
    @Column(name = "processor_payment_id", length = 255)
    private String processorPaymentId; // External payment ID
    
    @Column(name = "transaction_status", length = 50)
    private String transactionStatus; // PENDING, COMPLETED, FAILED, REFUNDED
    
    @Column(name = "is_default_payment")
    private Boolean isDefaultPayment = false;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    // Masked card number for display purposes (last 4 digits)
    @Column(name = "masked_card_number", length = 20)
    private String maskedCardNumber;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Default constructor
    public PaymentInfo() {}
    
    // Constructor with required fields
    public PaymentInfo(User user, String paymentMethod) {
        this.user = user;
        this.paymentMethod = paymentMethod;
    }
    
    // All args constructor
    public PaymentInfo(Long id, User user, Order order, String paymentMethod, String cardNumber, 
                       String cardHolderName, Integer expiryMonth, Integer expiryYear, String cvv,
                       String billingAddress, String paymentProcessor, String processorPaymentId,
                       String transactionStatus, Boolean isDefaultPayment, Boolean isActive,
                       String maskedCardNumber, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.user = user;
        this.order = order;
        this.paymentMethod = paymentMethod;
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;
        this.cvv = cvv;
        this.billingAddress = billingAddress;
        this.paymentProcessor = paymentProcessor;
        this.processorPaymentId = processorPaymentId;
        this.transactionStatus = transactionStatus;
        this.isDefaultPayment = isDefaultPayment;
        this.isActive = isActive;
        this.maskedCardNumber = maskedCardNumber;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public String getCardNumber() { return cardNumber; }
    
    public String getCardHolderName() { return cardHolderName; }
    public void setCardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; }
    
    public Integer getExpiryMonth() { return expiryMonth; }
    public void setExpiryMonth(Integer expiryMonth) { this.expiryMonth = expiryMonth; }
    
    public Integer getExpiryYear() { return expiryYear; }
    public void setExpiryYear(Integer expiryYear) { this.expiryYear = expiryYear; }
    
    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }
    
    public String getBillingAddress() { return billingAddress; }
    public void setBillingAddress(String billingAddress) { this.billingAddress = billingAddress; }
    
    public String getPaymentProcessor() { return paymentProcessor; }
    public void setPaymentProcessor(String paymentProcessor) { this.paymentProcessor = paymentProcessor; }
    
    public String getProcessorPaymentId() { return processorPaymentId; }
    public void setProcessorPaymentId(String processorPaymentId) { this.processorPaymentId = processorPaymentId; }
    
    public String getTransactionStatus() { return transactionStatus; }
    public void setTransactionStatus(String transactionStatus) { this.transactionStatus = transactionStatus; }
    
    public Boolean getIsDefaultPayment() { return isDefaultPayment; }
    public void setIsDefaultPayment(Boolean isDefaultPayment) { this.isDefaultPayment = isDefaultPayment; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public String getMaskedCardNumber() { return maskedCardNumber; }
    public void setMaskedCardNumber(String maskedCardNumber) { this.maskedCardNumber = maskedCardNumber; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Custom setter to handle masking
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
        // Generate masked version for display
        if (cardNumber != null && cardNumber.length() >= 4) {
            this.maskedCardNumber = "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
        }
    }
    
    // Helper methods
    public boolean isExpired() {
        if (expiryMonth == null || expiryYear == null) {
            return true;
        }
        
        LocalDateTime now = LocalDateTime.now();
        return now.getYear() > expiryYear || 
               (now.getYear() == expiryYear && now.getMonthValue() > expiryMonth);
    }
}