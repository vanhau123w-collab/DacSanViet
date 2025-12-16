# Design Document - Specialty Food E-commerce Website

## Overview

Hệ thống website bán hàng đặc sản quê hương được thiết kế theo kiến trúc MVC với Spring Boot, sử dụng Thymeleaf cho server-side rendering và WebSocket cho real-time communication. Hệ thống được chia thành các module chính: User Management, Product Management, Order Management, Payment Processing, và Admin Dashboard.

## Architecture

### High-Level Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   Backend       │    │   Database      │
│   (Thymeleaf)   │◄──►│  (Spring Boot)  │◄──►│   (MySQL)       │
│   + CSS Framework│    │   + Security    │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
                              ▼
                       ┌─────────────────┐
                       │   WebSocket     │
                       │   (Real-time)   │
                       └─────────────────┘
```

### Technology Stack

- **Backend**: Spring Boot 3.x, Spring Security, Spring Data JPA
- **Frontend**: Thymeleaf, Tailwind CSS/Bootstrap CSS
- **Database**: MySQL/SQL Server với JPA/Hibernate
- **Authentication**: JWT + Spring Security
- **Real-time**: WebSocket với STOMP
- **File Storage**: Local storage hoặc cloud storage cho hình ảnh

## Components and Interfaces

### 1. User Management Module

**Components:**
- `UserController`: Xử lý authentication, registration, profile management
- `UserService`: Business logic cho user operations
- `UserRepository`: Data access layer
- `JwtAuthenticationFilter`: JWT token validation
- `UserDetailsServiceImpl`: Spring Security integration

**Key Interfaces:**
```java
public interface UserService {
    UserDto registerUser(RegisterRequest request);
    UserDto authenticateUser(LoginRequest request);
    UserDto updateProfile(Long userId, UpdateProfileRequest request);
    List<OrderDto> getUserOrderHistory(Long userId);
}
```

### 2. Product Management Module

**Components:**
- `ProductController`: CRUD operations cho products
- `CategoryController`: Category management
- `ProductService`: Business logic
- `ProductRepository`, `CategoryRepository`: Data access
- `ImageUploadService`: File upload handling

**Key Interfaces:**
```java
public interface ProductService {
    Page<ProductDto> searchProducts(String keyword, Long categoryId, Pageable pageable);
    ProductDto getProductById(Long id);
    ProductDto createProduct(CreateProductRequest request);
    ProductDto updateProduct(Long id, UpdateProductRequest request);
    void deleteProduct(Long id);
}
```

### 3. Shopping Cart & Order Module

**Components:**
- `CartController`: Shopping cart operations
- `OrderController`: Order processing
- `CartService`, `OrderService`: Business logic
- `CartRepository`, `OrderRepository`: Data persistence
- `PaymentService`: Payment processing integration

**Key Interfaces:**
```java
public interface CartService {
    CartDto addToCart(Long userId, Long productId, Integer quantity);
    CartDto updateCartItem(Long userId, Long productId, Integer quantity);
    CartDto removeFromCart(Long userId, Long productId);
    CartDto getCart(Long userId);
}

public interface OrderService {
    OrderDto createOrder(Long userId, CreateOrderRequest request);
    OrderDto updateOrderStatus(Long orderId, OrderStatus status);
    Page<OrderDto> getOrdersByUser(Long userId, Pageable pageable);
}
```

### 4. Admin Management Module

**Components:**
- `AdminController`: Admin dashboard và management
- `AdminService`: Admin-specific business logic
- `ReportService`: Analytics và reporting
- `NotificationService`: System notifications

### 5. Real-time Communication Module

**Components:**
- `WebSocketConfig`: WebSocket configuration
- `NotificationController`: WebSocket message handling
- `NotificationService`: Real-time notification logic

## Data Models

### Core Entities

```java
@Entity
public class User {
    @Id @GeneratedValue
    private Long id;
    private String username;
    private String email;
    private String password; // BCrypt encoded
    private String fullName;
    private String phoneNumber;
    @Enumerated(EnumType.STRING)
    private Role role; // USER, ADMIN
    @OneToMany(mappedBy = "user")
    private List<Address> addresses;
    @OneToMany(mappedBy = "user")
    private List<Order> orders;
    // timestamps, etc.
}

@Entity
public class Product {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageUrl;
    private Boolean active;
    @ManyToOne
    private Category category;
    @OneToMany(mappedBy = "product")
    private List<CartItem> cartItems;
    // timestamps, etc.
}

@Entity
public class Category {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String description;
    @OneToMany(mappedBy = "category")
    private List<Product> products;
}

@Entity
public class Order {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private User user;
    private BigDecimal totalAmount;
    @Enumerated(EnumType.STRING)
    private OrderStatus status; // PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;
    @ManyToOne
    private Address shippingAddress;
    private LocalDateTime orderDate;
    // payment info, tracking, etc.
}

@Entity
public class CartItem {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private User user;
    @ManyToOne
    private Product product;
    private Integer quantity;
    private LocalDateTime addedDate;
}
```

### Database Schema Relationships

- User (1) ←→ (N) Order
- User (1) ←→ (N) CartItem  
- Product (1) ←→ (N) CartItem
- Product (1) ←→ (N) OrderItem
- Category (1) ←→ (N) Product
- Order (1) ←→ (N) OrderItem

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Product search relevance
*For any* search keyword and product database, all returned search results should contain the keyword in product name, description, or category
**Validates: Requirements 1.2**

### Property 2: Category filtering accuracy
*For any* selected category, all filtered products should belong only to that specific category
**Validates: Requirements 1.3**

### Property 3: Product detail completeness
*For any* product, the detail view should display all required fields: name, description, price, images, and availability status
**Validates: Requirements 1.4**

### Property 4: Product sorting correctness
*For any* product list and sort criteria (price/popularity), the sorted results should be in correct ascending or descending order
**Validates: Requirements 1.5**

### Property 5: Cart addition accuracy
*For any* product and quantity, adding to cart should result in the cart containing exactly that product with the specified quantity
**Validates: Requirements 2.1**

### Property 6: Cart total calculation
*For any* cart contents, the displayed total should equal the sum of (product price × quantity) for all items
**Validates: Requirements 2.2**

### Property 7: Cart quantity update consistency
*For any* cart item quantity modification, the total price should be recalculated correctly and inventory should be updated accordingly
**Validates: Requirements 2.3**

### Property 8: Order creation from payment
*For any* successful payment, an order should be created with correct customer information, items, and total amount
**Validates: Requirements 2.5**

### Property 9: User registration completeness
*For any* valid registration data, a new user account should be created with all provided information stored correctly
**Validates: Requirements 3.1**

### Property 10: Authentication access control
*For any* valid user credentials, authentication should grant access to user-specific features and data
**Validates: Requirements 3.2**

### Property 11: Profile update persistence
*For any* profile information changes, the updates should be saved and retrievable in subsequent requests
**Validates: Requirements 3.3**

### Property 12: Order history completeness
*For any* user, the order history should display all orders associated with that user with correct status and details
**Validates: Requirements 3.4**

### Property 13: Address management operations
*For any* address CRUD operation (create, read, update, delete), the operation should complete successfully and maintain data integrity
**Validates: Requirements 3.5**

### Property 14: Product creation integrity
*For any* valid product data, creating a product should result in a new product entry with all provided information and updated inventory
**Validates: Requirements 4.1**

### Property 15: Product update consistency
*For any* product information changes, updates should be applied correctly while maintaining referential integrity
**Validates: Requirements 4.2**

### Property 16: Category management operations
*For any* category CRUD operation, the operation should complete successfully and maintain product-category relationships
**Validates: Requirements 4.3**

### Property 17: Image upload and storage
*For any* valid image upload, the image should be stored correctly and be retrievable for display
**Validates: Requirements 4.4**

### Property 18: Inventory overselling prevention
*For any* product availability change, the system should prevent orders that exceed available stock quantity
**Validates: Requirements 4.5**

### Property 19: Order status update notifications
*For any* order status change, the customer should receive a notification and order tracking should be updated
**Validates: Requirements 5.2**

### Property 20: Order search accuracy
*For any* order search criteria, returned results should match the search parameters (customer, date range, status, etc.)
**Validates: Requirements 5.3**

### Property 21: Customer information display
*For any* customer, the admin view should display complete customer details and associated order history
**Validates: Requirements 5.4**

### Property 22: Sales report accuracy
*For any* time period, generated sales reports should contain accurate revenue calculations and product statistics
**Validates: Requirements 5.5**

### Property 23: Data encryption compliance
*For any* sensitive data (passwords, payment info), the data should be encrypted before storage using secure algorithms
**Validates: Requirements 6.1**

### Property 24: JWT token security
*For any* authentication request, JWT tokens should be generated with proper expiration and signature validation
**Validates: Requirements 6.2**

### Property 25: Unauthorized access prevention
*For any* unauthorized access attempt, the system should deny access and log the security event with relevant details
**Validates: Requirements 6.3**

### Property 26: Password reset security
*For any* password reset request, a secure time-limited reset link should be generated and sent to the user's email
**Validates: Requirements 6.4**

### Property 27: Admin privilege verification
*For any* admin function access, the system should verify admin privileges and log the administrative action
**Validates: Requirements 6.5**

### Property 28: Real-time order notifications
*For any* order status change, real-time notifications should be sent immediately to the customer via WebSocket
**Validates: Requirements 7.1**

### Property 29: WebSocket message delivery
*For any* new message or notification, it should be delivered immediately to connected clients via WebSocket
**Validates: Requirements 7.2**

### Property 30: Low inventory alerts
*For any* product with stock below threshold, admin notifications should be triggered for restocking
**Validates: Requirements 7.3**

### Property 31: Payment confirmation delivery
*For any* completed payment, immediate confirmation notifications should be sent to both customer and admin
**Validates: Requirements 7.4**

### Property 32: Maintenance notifications
*For any* system maintenance event, all active users should receive notifications about service interruptions
**Validates: Requirements 7.5**

### Property 33: Responsive layout adaptation
*For any* screen size or device type, the website layout should adapt appropriately to provide optimal viewing experience
**Validates: Requirements 8.1**

### Property 34: Form validation feedback
*For any* invalid form input, clear and specific error messages should be displayed to guide user correction
**Validates: Requirements 8.3**

### Property 35: Loading indicator display
*For any* page or content loading operation, appropriate loading indicators should be shown to inform users of progress
**Validates: Requirements 8.4**

### Property 36: Accessibility compliance
*For any* user interface element, it should be accessible via screen readers and keyboard navigation
**Validates: Requirements 8.5**

## Error Handling

### Exception Handling Strategy

**Global Exception Handler:**
- `@ControllerAdvice` class để xử lý exceptions toàn cục
- Custom exception classes cho business logic errors
- Standardized error response format với error codes và messages

**Common Exception Types:**
- `ProductNotFoundException`: Khi product không tồn tại
- `InsufficientStockException`: Khi số lượng trong kho không đủ
- `UnauthorizedAccessException`: Khi user không có quyền truy cập
- `InvalidPaymentException`: Khi payment processing thất bại
- `ValidationException`: Khi dữ liệu input không hợp lệ

**Error Response Format:**
```json
{
  "timestamp": "2024-01-01T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Product quantity exceeds available stock",
  "path": "/api/cart/add",
  "errorCode": "INSUFFICIENT_STOCK"
}
```

### Validation Strategy

- **Bean Validation**: Sử dụng `@Valid` annotations cho request DTOs
- **Custom Validators**: Cho business rules phức tạp
- **Database Constraints**: Foreign keys, unique constraints, not null
- **Frontend Validation**: JavaScript validation cho UX tốt hơn

## Testing Strategy

### Dual Testing Approach

Hệ thống sẽ sử dụng cả unit testing và property-based testing để đảm bảo tính đúng đắn:

**Unit Testing:**
- Sử dụng JUnit 5 và Mockito cho unit tests
- Test các specific examples và edge cases
- Integration tests với `@SpringBootTest` cho các component interactions
- MockMvc cho controller testing
- TestContainers cho database integration tests

**Property-Based Testing:**
- Sử dụng **jqwik** library cho property-based testing trong Java
- Mỗi property-based test sẽ chạy tối thiểu 100 iterations
- Mỗi test sẽ được tag với comment tham chiếu đến correctness property tương ứng
- Format: `**Feature: specialty-food-ecommerce, Property {number}: {property_text}**`
- Mỗi correctness property sẽ được implement bởi một property-based test duy nhất

**Test Coverage Requirements:**
- Unit tests: Verify specific examples, edge cases, error conditions
- Property tests: Verify universal properties across all inputs
- Integration tests: Test component interactions và end-to-end workflows
- Security tests: Verify authentication, authorization, và data protection

### Testing Framework Configuration

```java
// Example property-based test structure
@Property
@Label("Feature: specialty-food-ecommerce, Property 1: Product search relevance")
void searchResultsShouldContainKeyword(@ForAll String keyword, @ForAll List<Product> products) {
    // Test implementation
}
```

## Security Implementation

### Authentication & Authorization

**JWT Implementation:**
- Access tokens với short expiration (15 minutes)
- Refresh tokens với longer expiration (7 days)
- Token blacklisting cho logout functionality
- Secure token storage recommendations

**Spring Security Configuration:**
- Method-level security với `@PreAuthorize`
- Role-based access control (USER, ADMIN)
- CSRF protection cho state-changing operations
- CORS configuration cho frontend integration

**Password Security:**
- BCrypt encoding với strength 12
- Password complexity requirements
- Account lockout after failed attempts
- Secure password reset workflow

### Data Protection

**Encryption:**
- Database encryption for sensitive fields
- HTTPS enforcement for all communications
- Secure session management
- PII data anonymization options

## Performance Considerations

### Database Optimization

**Indexing Strategy:**
- Primary keys và foreign keys
- Search fields (product name, category)
- Frequently queried fields (user email, order date)
- Composite indexes cho complex queries

**Query Optimization:**
- JPA query optimization với `@Query`
- Lazy loading cho relationships
- Pagination cho large datasets
- Database connection pooling

### Caching Strategy

**Application-Level Caching:**
- Spring Cache với Redis/Ehcache
- Product catalog caching
- User session caching
- Search results caching

**Database Caching:**
- Query result caching
- Second-level Hibernate cache
- Database query plan caching

### Real-time Performance

**WebSocket Optimization:**
- Connection pooling
- Message queuing cho high volume
- Graceful degradation khi WebSocket không available
- Heartbeat mechanism cho connection health

## Deployment Architecture

### Environment Configuration

**Development:**
- H2 in-memory database
- Local file storage
- Debug logging enabled
- Hot reload với Spring Boot DevTools

**Production:**
- MySQL/SQL Server cluster
- Cloud storage cho images
- Optimized logging levels
- Health checks và monitoring

### Scalability Considerations

**Horizontal Scaling:**
- Stateless application design
- Load balancer configuration
- Database read replicas
- CDN cho static assets

**Monitoring & Observability:**
- Application metrics với Micrometer
- Health endpoints
- Structured logging
- Error tracking và alerting