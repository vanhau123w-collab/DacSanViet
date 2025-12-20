# Hướng Dẫn Tích Hợp Thanh Toán

## Tổng Quan

Hệ thống thanh toán đã được tích hợp với 4 phương thức:

1. **COD (Cash on Delivery)** - Thanh toán khi nhận hàng
2. **VNPAY** - Cổng thanh toán tự động (Sandbox)
3. **VietQR** - Chuyển khoản ngân hàng qua QR động
4. **Momo** - Ví điện tử Momo (QR tĩnh, xác nhận thủ công)

## 1. VNPAY (Tự Động)

### Cấu Hình
File: `src/main/resources/application.properties`

```properties
vnpay.tmn-code=CGWDEMO1
vnpay.hash-secret=RAOEXHYVSDDIIENYWSLDIIZTANXUXZFJ
vnpay.url=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
vnpay.return-url=http://localhost:8082/payment/vnpay-return
```

### Thông Tin Sandbox
- **TMN Code**: CGWDEMO1
- **Hash Secret**: RAOEXHYVSDDIIENYWSLDIIZTANXUXZFJ
- **URL**: https://sandbox.vnpayment.vn/paymentv2/vpcpay.html

### Test Cards (Sandbox)
- **Ngân hàng**: NCB
- **Số thẻ**: 9704198526191432198
- **Tên chủ thẻ**: NGUYEN VAN A
- **Ngày phát hành**: 07/15
- **Mật khẩu OTP**: 123456

### Luồng Thanh Toán
1. Khách hàng chọn VNPAY tại checkout
2. Hệ thống tạo đơn hàng và redirect đến VNPAY
3. Khách hàng nhập thông tin thẻ tại VNPAY
4. VNPAY xử lý và redirect về `/payment/vnpay-return`
5. Hệ thống verify signature và cập nhật trạng thái đơn hàng

### Chuyển Sang Production
1. Đăng ký tài khoản VNPAY tại: https://vnpay.vn
2. Lấy TMN Code và Hash Secret thật
3. Cập nhật trong `application.properties`:
   ```properties
   vnpay.tmn-code=YOUR_TMN_CODE
   vnpay.hash-secret=YOUR_HASH_SECRET
   vnpay.url=https://pay.vnpay.vn/vpcpay.html
   vnpay.return-url=https://yourdomain.com/payment/vnpay-return
   ```

## 2. VietQR (Tự Động Tạo QR)

### Cấu Hình
```properties
vietqr.bank-id=970436
vietqr.account-no=1040489156
vietqr.account-name=NGUYEN NHAT THIEN
vietqr.template=compact2
```

### Thông Tin Ngân Hàng
- **Ngân hàng**: Vietcombank (970436)
- **Số tài khoản**: 1040489156
- **Chủ tài khoản**: NGUYEN NHAT THIEN

### Luồng Thanh Toán
1. Khách hàng chọn VietQR tại checkout
2. Hệ thống tạo đơn hàng và redirect đến trang VietQR
3. Trang hiển thị mã QR động với:
   - Số tiền đã điền sẵn
   - Nội dung: DH{orderId}
   - Thông tin tài khoản
4. Khách hàng quét QR bằng app ngân hàng
5. Thanh toán được xác nhận thủ công bởi admin

### API VietQR
Sử dụng API miễn phí từ: https://vietqr.io

Format URL:
```
https://img.vietqr.io/image/{BANK_ID}-{ACCOUNT_NO}-{TEMPLATE}.jpg?amount={AMOUNT}&addInfo={DESCRIPTION}&accountName={ACCOUNT_NAME}
```

### Xác Thực Tự Động

**Chỉ có VNPAY hỗ trợ xác thực tự động an toàn.**

Các phương thức khác (VietQR, Momo) cần admin xác nhận thủ công vì:
- Casso/PayOS yêu cầu thông tin đăng nhập internet banking (không an toàn)
- API ngân hàng trực tiếp yêu cầu hợp đồng doanh nghiệp

**Khuyến nghị:**
- Dùng VNPAY cho thanh toán tự động
- VietQR/Momo cho khách hàng không có thẻ ngân hàng
- Admin kiểm tra sao kê ngân hàng và xác nhận thủ công

## 3. Momo (QR Tĩnh - Thủ Công)

### Cấu Hình
```properties
momo.qr-image=/images/payments/momoQR.webp
```

### Chuẩn Bị
1. Mở app Momo
2. Chọn "Nhận tiền" → "Mã QR nhận tiền"
3. Chụp màn hình hoặc tải xuống QR
4. Lưu file vào: `src/main/resources/static/images/payments/momoQR.webp`

### Luồng Thanh Toán
1. Khách hàng chọn Momo tại checkout
2. Hệ thống tạo đơn hàng và hiển thị QR tĩnh
3. Khách hàng quét QR và nhập nội dung: DH{orderId}
4. Admin kiểm tra giao dịch trong app Momo
5. Admin xác nhận đơn hàng thủ công

### Xác Thực Tự Động Momo
Momo không cung cấp API miễn phí cho cá nhân. Để tự động hóa:
1. Đăng ký Momo Business (có phí)
2. Tích hợp Momo Payment API
3. Hoặc sử dụng dịch vụ trung gian như Casso

## 4. COD (Thanh Toán Khi Nhận Hàng)

Không cần cấu hình. Đơn hàng được tạo và xử lý bình thường.

## Cấu Trúc Code

### Services
- `VNPayService.java` - Xử lý VNPAY payment
- `OrderService.java` - Tạo và quản lý đơn hàng

### Controllers
- `PaymentController.java` - Xử lý các request thanh toán
- `CheckoutController.java` - Xử lý checkout

### Templates
- `payment/result.html` - Kết quả thanh toán VNPAY
- `payment/vietqr.html` - Trang thanh toán VietQR
- `payment/momo.html` - Trang thanh toán Momo
- `checkout/simple-checkout.html` - Trang checkout

## Testing

### Test VNPAY (Sandbox)
1. Thêm sản phẩm vào giỏ
2. Checkout và chọn VNPAY
3. Sử dụng thẻ test:
   - Số thẻ: 9704198526191432198
   - Ngày: 07/15
   - OTP: 123456
4. Xác nhận thanh toán
5. Kiểm tra redirect về trang kết quả

### Test VietQR
1. Checkout và chọn VietQR
2. Kiểm tra QR code hiển thị đúng
3. Quét bằng app ngân hàng (test thật)
4. Kiểm tra thông tin tự động điền

### Test Momo
1. Thêm file `momoQR.webp` vào thư mục
2. Checkout và chọn Momo
3. Kiểm tra QR hiển thị
4. Quét bằng app Momo (test thật)

## Bảo Mật

### VNPAY
- Hash Secret được mã hóa trong config
- Verify signature cho mọi response
- Timeout 15 phút cho mỗi giao dịch

### VietQR & Momo
- Nội dung chuyển khoản unique (DH{orderId})
- Admin verify thủ công
- Không lưu thông tin nhạy cảm

## Monitoring

### Logs
Kiểm tra logs tại:
```bash
tail -f logs/application.log | grep -i payment
```

### Database
Kiểm tra orders table:
```sql
SELECT * FROM orders WHERE payment_method IN ('VNPAY', 'VIETQR', 'MOMO');
```

## Troubleshooting

### VNPAY không redirect về
- Kiểm tra `vnpay.return-url` trong config
- Đảm bảo URL accessible từ internet (nếu production)
- Kiểm tra firewall/security group

### VietQR không hiển thị
- Kiểm tra internet connection
- API vietqr.io có thể bị rate limit
- Thử refresh lại trang

### Momo QR không hiển thị
- Kiểm tra file `momoQR.webp` tồn tại
- Kiểm tra path trong config
- Kiểm tra file permissions

## Support

Nếu cần hỗ trợ:
1. Kiểm tra logs
2. Kiểm tra config
3. Test với sandbox/test environment trước
4. Liên hệ support của payment gateway

## Roadmap

### Tính Năng Tương Lai
- [ ] Admin dashboard để xác nhận thanh toán VietQR/Momo
- [ ] Thêm ZaloPay
- [ ] Thêm ShopeePay  
- [ ] Email notification khi có thanh toán mới (cần check sao kê thủ công)
- [ ] Refund functionality
- [ ] Tích hợp API ngân hàng trực tiếp (khi có hợp đồng doanh nghiệp)
