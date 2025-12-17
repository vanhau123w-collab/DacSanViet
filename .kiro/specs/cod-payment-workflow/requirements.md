# Requirements Document

## Introduction

Hệ thống thanh toán khi nhận hàng (COD - Cash on Delivery) cho phép khách hàng đặt hàng mà không cần thanh toán trước, và có thể xác nhận đã nhận hàng để cập nhật trạng thái đơn hàng.

## Glossary

- **COD_System**: Hệ thống thanh toán khi nhận hàng
- **Order_Status**: Trạng thái đơn hàng (PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED)
- **Payment_Method**: Phương thức thanh toán (COD, ONLINE)
- **User**: Người dùng đặt hàng
- **Admin**: Quản trị viên hệ thống

## Requirements

### Requirement 1

**User Story:** Là một khách hàng, tôi muốn chọn thanh toán khi nhận hàng, để tôi có thể đặt hàng mà không cần thanh toán trước.

#### Acceptance Criteria

1. WHEN a user selects COD payment method THEN the COD_System SHALL display "Thanh toán khi nhận hàng" option
2. WHEN a user completes checkout with COD payment THEN the COD_System SHALL create order with PROCESSING status
3. WHEN COD order is created THEN the COD_System SHALL display success message "Đặt hàng thành công! Bạn sẽ thanh toán khi nhận hàng"
4. WHEN COD order is created THEN the COD_System SHALL set payment status to "PENDING"
5. WHEN COD order is created THEN the COD_System SHALL clear user cart

### Requirement 2

**User Story:** Là một khách hàng, tôi muốn xác nhận đã nhận hàng, để cập nhật trạng thái đơn hàng và hoàn tất giao dịch.

#### Acceptance Criteria

1. WHEN a user views their order with SHIPPED status THEN the COD_System SHALL display "Xác nhận đã nhận hàng" button
2. WHEN a user clicks "Xác nhận đã nhận hàng" THEN the COD_System SHALL update order status to DELIVERED
3. WHEN order status changes to DELIVERED THEN the COD_System SHALL update payment status to "COMPLETED"
4. WHEN order is confirmed as delivered THEN the COD_System SHALL record delivery confirmation timestamp
5. WHEN delivery is confirmed THEN the COD_System SHALL prevent further status changes by user

### Requirement 3

**User Story:** Là một quản trị viên, tôi muốn theo dõi các đơn hàng COD, để quản lý quy trình giao hàng và thanh toán.

#### Acceptance Criteria

1. WHEN admin views order list THEN the COD_System SHALL display payment method for each order
2. WHEN admin views COD order details THEN the COD_System SHALL show payment status and delivery confirmation
3. WHEN admin updates order status to SHIPPED THEN the COD_System SHALL enable user confirmation option
4. WHEN admin views dashboard THEN the COD_System SHALL display COD order statistics
5. WHEN admin searches orders THEN the COD_System SHALL allow filtering by payment method

### Requirement 4

**User Story:** Là một khách hàng, tôi muốn xem trạng thái đơn hàng COD, để biết được tiến trình giao hàng và thanh toán.

#### Acceptance Criteria

1. WHEN user views order details THEN the COD_System SHALL display current order status
2. WHEN user views COD order THEN the COD_System SHALL show payment method as "Thanh toán khi nhận hàng"
3. WHEN order status is PROCESSING THEN the COD_System SHALL display "Đơn hàng đang được xử lý"
4. WHEN order status is SHIPPED THEN the COD_System SHALL display "Đơn hàng đang được giao" and confirmation button
5. WHEN order status is DELIVERED THEN the COD_System SHALL display "Đơn hàng đã được giao thành công"

### Requirement 5

**User Story:** Là hệ thống, tôi muốn xử lý quy trình COD một cách nhất quán, để đảm bảo tính toàn vẹn dữ liệu và trải nghiệm người dùng.

#### Acceptance Criteria

1. WHEN COD order is created THEN the COD_System SHALL validate all required customer information
2. WHEN order status changes THEN the COD_System SHALL validate status transition rules
3. WHEN user confirms delivery THEN the COD_System SHALL validate order belongs to user
4. WHEN payment status updates THEN the COD_System SHALL maintain data consistency
5. WHEN system errors occur THEN the COD_System SHALL provide meaningful error messages in Vietnamese