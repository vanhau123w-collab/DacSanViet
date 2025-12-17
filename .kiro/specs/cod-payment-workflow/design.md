# Design Document

## Overview

Tính năng thanh toán khi nhận hàng (COD) cho phép khách hàng đặt hàng mà không cần thanh toán trước. Hệ thống sẽ quản lý quy trình từ đặt hàng, xử lý, giao hàng đến xác nhận nhận hàng của khách hàng.

## Architecture

### Luồng xử lý COD:
1. **Checkout**: Khách hàng chọn COD → Tạo đơn hàng với trạng thái PROCESSING
2. **Admin Processing**: Admin xử lý đơn hàng → Cập nhật trạng thái SHIPPED
3. **User Confirmation**: Khách hàng xác nhận nhận hàng → Trạng thái DELIVERED
4. **Dashboard Update**: Cập nhật thống kê và báo cáo

## Components and Interfaces

### 1. Frontend Components

#### CheckoutController
- Thêm xử lý COD payment method
- Hiển thị thông báo thành công cho COD
- Redirect đến trang order details

#### OrderController  
- Thêm endpoint xác nhận nhận hàng
- Hiển thị nút "Xác nhận đã nhận hàng" cho đơn SHIPPED
- Cập nhật UI trạng thái đơn hàng

#### AdminController
- Hiển thị thông tin payment method trong order list
- Thêm filter theo payment method
- Cập nhật dashboard với COD statistics

### 2. Backend Services

#### OrderService
- `confirmDelivery(Long orderId, Long userId)`: Xác nhận nhận hàng
- `updateOrderStatusToCODProcessing()`: Cập nhật trạng thái COD
- Validation cho COD workflow

#### PaymentService (mới)
- `processCODPayment()`: Xử lý thanh toán COD
- `updatePaymentStatus()`: Cập nhật trạng thái thanh toán

### 3. Database Changes

#### Order Table
- Thêm `delivery_confirmed_at` TIMESTAMP
- Thêm `payment_status` ENUM ('PENDING', 'COMPLETED', 'FAILED')

#### Enum Updates
- OrderStatus: Đảm bảo có PROCESSING status
- PaymentMethod: Thêm 'COD' nếu chưa có

## Data Models

### Order Model Updates
```java
@Column(name = "delivery_confirmed_at")
private LocalDateTime deliveryConfirmedAt;

@Enumerated(EnumType.STRING)
@Column(name = "payment_status")
private PaymentStatus paymentStatus = PaymentStatus.PENDING;
```

### PaymentStatus Enum
```java
public enum PaymentStatus {
    PENDING,
    COMPLETED, 
    FAILED
}
```

### OrderDao Updates
```java
private LocalDateTime deliveryConfirmedAt;
private PaymentStatus paymentStatus;
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property Reflection

Reviewing the prework analysis, I identify several areas where properties can be consolidated:

**Redundancy Analysis:**
- Properties 1.4, 1.5, 2.3, 2.4 all relate to order state management and can be combined into comprehensive state transition properties
- Properties 2.2, 2.3, 2.4, 2.5 all relate to delivery confirmation workflow and can be consolidated
- Properties 5.1, 5.2, 5.3, 5.4 all relate to validation and can be combined into comprehensive validation properties

**Consolidated Properties:**

Property 1: COD Order Creation State Management
*For any* COD checkout request, creating the order should result in PROCESSING status, PENDING payment status, and empty user cart
**Validates: Requirements 1.2, 1.4, 1.5**

Property 2: Delivery Confirmation Workflow
*For any* order in SHIPPED status, when user confirms delivery, the system should update status to DELIVERED, set payment status to COMPLETED, record confirmation timestamp, and prevent further user status changes
**Validates: Requirements 2.2, 2.3, 2.4, 2.5**

Property 3: Payment Method Filtering
*For any* search query with payment method filter, all returned orders should match the specified payment method
**Validates: Requirements 3.5**

Property 4: COD Order Validation
*For any* COD order creation attempt, the system should validate required customer information, and reject orders with missing data
**Validates: Requirements 5.1**

Property 5: Order Status Transition Validation
*For any* order status change attempt, the system should validate transition rules and reject invalid transitions
**Validates: Requirements 5.2**

Property 6: Delivery Confirmation Authorization
*For any* delivery confirmation attempt, the system should validate the order belongs to the requesting user and reject unauthorized attempts
**Validates: Requirements 5.3**

Property 7: Payment Status Data Consistency
*For any* payment status update, all related order data should remain consistent and valid
**Validates: Requirements 5.4**

Property 8: Vietnamese Error Messages
*For any* system error condition, the error message should be in Vietnamese and provide meaningful information to the user
**Validates: Requirements 5.5**

## Error Handling

### COD-Specific Error Scenarios
1. **Invalid Payment Method**: Khi payment method không hợp lệ
2. **Order Not Found**: Khi không tìm thấy đơn hàng để xác nhận
3. **Unauthorized Confirmation**: Khi user cố xác nhận đơn hàng không phải của mình
4. **Invalid Status Transition**: Khi cố cập nhật trạng thái không hợp lệ
5. **Missing Customer Info**: Khi thiếu thông tin khách hàng cho COD

### Error Response Format
```json
{
    "error": true,
    "message": "Thông báo lỗi bằng tiếng Việt",
    "code": "ERROR_CODE",
    "timestamp": "2024-01-01T10:00:00Z"
}
```

## Testing Strategy

### Unit Testing Approach
- Test individual methods in OrderService, PaymentService
- Test validation logic for COD orders
- Test status transition rules
- Test authorization checks

### Property-Based Testing Approach
- Use **JQwik** as the property-based testing library for Java
- Configure each property-based test to run a minimum of 100 iterations
- Each property-based test will be tagged with comments referencing the design document properties
- Tag format: **Feature: cod-payment-workflow, Property {number}: {property_text}**

**Property-Based Testing Requirements:**
- Generate random COD orders and verify state management
- Generate random delivery confirmations and verify workflow
- Generate random status transitions and verify validation
- Generate random user authorization scenarios
- Test error conditions with random invalid inputs

**Unit Testing Requirements:**
- Test specific COD checkout scenarios
- Test delivery confirmation button display logic
- Test admin dashboard COD statistics
- Test Vietnamese error message formatting
- Integration tests for complete COD workflow

Both unit tests and property-based tests are essential for comprehensive coverage:
- Unit tests verify specific examples and integration points
- Property tests verify universal properties across all inputs
- Together they ensure both concrete functionality and general correctness