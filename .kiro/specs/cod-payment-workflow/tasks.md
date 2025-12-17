# Implementation Plan

- [x] 1. Cập nhật cấu trúc database và model





  - Thêm enum PaymentStatus vào model
  - Thêm trường delivery_confirmed_at và payment_status vào Order model
  - Cập nhật OrderDao với các trường mới
  - _Requirements: 5.4_

- [ ] 2. Triển khai backend logic cho COD





- [x] 2.1 Tạo PaymentStatus enum

  - Tạo enum với các giá trị PENDING, COMPLETED, FAILED
  - _Requirements: 1.4, 2.3_

- [x] 2.2 Cập nhật Order model và OrderDao

  - Thêm deliveryConfirmedAt và paymentStatus fields
  - Cập nhật constructors và getters/setters
  - _Requirements: 2.4, 5.4_

- [x] 2.3 Viết property test cho COD order creation state management


  - **Property 1: COD Order Creation State Management**
  - **Validates: Requirements 1.2, 1.4, 1.5**

- [x] 2.4 Cập nhật OrderService cho COD workflow


  - Sửa createOrderFromCart để xử lý COD payment method
  - Thêm method confirmDelivery cho user xác nhận nhận hàng
  - Thêm validation cho COD orders
  - _Requirements: 1.2, 1.4, 1.5, 2.2, 2.3, 2.4, 2.5_

- [x] 2.5 Viết property test cho delivery confirmation workflow


  - **Property 2: Delivery Confirmation Workflow**
  - **Validates: Requirements 2.2, 2.3, 2.4, 2.5**

- [x] 2.6 Viết property test cho COD order validation


  - **Property 4: COD Order Validation**
  - **Validates: Requirements 5.1**

- [x] 2.7 Viết property test cho order status transition validation


  - **Property 5: Order Status Transition Validation**


  - **Validates: Requirements 5.2**





- [ ] 2.8 Viết property test cho delivery confirmation authorization
  - **Property 6: Delivery Confirmation Authorization**
  - **Validates: Requirements 5.3**

- [ ] 3. Cập nhật CheckoutController cho COD
- [x] 3.1 Sửa processCheckout method

  - Thêm xử lý đặc biệt cho COD payment method
  - Hiển thị thông báo thành công phù hợp cho COD
  - _Requirements: 1.2, 1.3_

- [ ] 3.2 Viết unit test cho COD checkout process
  - Test COD payment method selection
  - Test success message display
  - _Requirements: 1.1, 1.3_

- [ ] 4. Triển khai OrderController cho user confirmation
- [ ] 4.1 Thêm endpoint confirmDelivery
  - Tạo POST /orders/{id}/confirm-delivery endpoint
  - Thêm validation và authorization
  - _Requirements: 2.2, 5.3_

- [ ] 4.2 Cập nhật order details view
  - Thêm logic hiển thị nút "Xác nhận đã nhận hàng" cho SHIPPED orders
  - Cập nhật hiển thị trạng thái đơn hàng
  - _Requirements: 2.1, 4.1, 4.2, 4.3, 4.4, 4.5_

- [ ] 4.3 Viết unit test cho order confirmation UI
  - Test confirmation button display logic
  - Test order status display
  - _Requirements: 2.1, 4.1, 4.2, 4.3, 4.4, 4.5_

- [ ] 5. Checkpoint - Đảm bảo tất cả tests đều pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 6. Cập nhật Admin interface
- [ ] 6.1 Cập nhật AdminController và order management
  - Thêm hiển thị payment method trong order list
  - Thêm filter theo payment method
  - Cập nhật order details view cho admin
  - _Requirements: 3.1, 3.2, 3.5_

- [ ] 6.2 Viết property test cho payment method filtering
  - **Property 3: Payment Method Filtering**
  - **Validates: Requirements 3.5**

- [ ] 6.3 Cập nhật dashboard với COD statistics
  - Thêm thống kê COD orders vào AdminService
  - Cập nhật dashboard template
  - _Requirements: 3.4_

- [ ] 6.4 Viết unit test cho admin COD features
  - Test payment method display
  - Test COD statistics
  - _Requirements: 3.1, 3.2, 3.4_

- [ ] 7. Cập nhật templates và UI
- [ ] 7.1 Cập nhật checkout template
  - Thêm COD option vào payment method selection
  - Cập nhật success message cho COD
  - _Requirements: 1.1, 1.3_

- [ ] 7.2 Cập nhật order details template
  - Thêm nút "Xác nhận đã nhận hàng" cho SHIPPED orders
  - Cập nhật hiển thị payment method và status
  - _Requirements: 2.1, 4.1, 4.2, 4.3, 4.4, 4.5_

- [ ] 7.3 Cập nhật admin templates
  - Thêm payment method column vào order list
  - Thêm payment method filter
  - Cập nhật dashboard với COD stats
  - _Requirements: 3.1, 3.2, 3.4, 3.5_

- [ ] 8. Triển khai error handling và validation
- [ ] 8.1 Thêm COD-specific error handling
  - Tạo custom exceptions cho COD workflow
  - Thêm Vietnamese error messages
  - _Requirements: 5.5_

- [ ] 8.2 Viết property test cho Vietnamese error messages
  - **Property 8: Vietnamese Error Messages**
  - **Validates: Requirements 5.5**

- [ ] 8.3 Viết property test cho payment status data consistency
  - **Property 7: Payment Status Data Consistency**
  - **Validates: Requirements 5.4**

- [ ] 9. Final Checkpoint - Kiểm tra toàn bộ hệ thống
  - Ensure all tests pass, ask the user if questions arise.
  - Test complete COD workflow từ checkout đến delivery confirmation
  - Verify admin dashboard updates correctly