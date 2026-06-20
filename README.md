<div align="center">

# 🛒 Đặc Sản Việt - E-Commerce Platform

![Spring Boot](https://img.shields.io/badge/SPRING_BOOT-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Java 17](https://img.shields.io/badge/JAVA_17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![MySQL](https://img.shields.io/badge/MYSQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/THYMELEAF-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white)
![Bootstrap](https://img.shields.io/badge/BOOTSTRAP-7952B3?style=for-the-badge&logo=bootstrap&logoColor=white)
![JavaScript](https://img.shields.io/badge/JAVASCRIPT-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black)
![E-Commerce](https://img.shields.io/badge/E--COMMERCE-FF6A00?style=for-the-badge)
![License MIT](https://img.shields.io/badge/LICENSE-MIT-green?style=for-the-badge)

> 🎁 Nền tảng thương mại điện tử hiện đại chuyên phân phối các sản phẩm đặc sản vùng miền Việt Nam, mang hương vị quê hương đến mọi miền tổ quốc.

</div>

---

## 📌 Giới thiệu dự án

**Đặc Sản Việt** là hệ thống ứng dụng công nghệ web hiện đại kết hợp **Spring Boot** và **Thymeleaf** nhằm tạo ra một kênh phân phối đặc sản Việt Nam uy tín và tiện lợi.

Hệ thống được định hướng trở thành một nền tảng bán hàng trực tuyến toàn diện, có khả năng quản lý kho hàng, xử lý đơn hàng, tích hợp thanh toán đa phương thức và cung cấp trải nghiệm mua sắm tuyệt vời cho người dùng.

Các chức năng chính:

- 🔍 Tìm kiếm và lọc sản phẩm thông minh theo danh mục, mức giá
- 🛒 Giỏ hàng tối ưu hóa bằng localStorage, đồng bộ dữ liệu người dùng
- ⚡ Checkout 1 trang (1-page checkout) nhanh chóng và tích hợp mã QR thanh toán
- 🧩 Dashboard quản trị trực quan với các biểu đồ thống kê doanh thu, đơn hàng
- 💬 Hỗ trợ hệ thống bình luận, đánh giá sản phẩm realtime bằng WebSocket
- 🔔 Gửi email thông báo tự động cho từng trạng thái của đơn hàng
- 🧠 Quản lý phân quyền chặt chẽ (Admin, Customer, Staff) bằng Spring Security
- 📊 Hỗ trợ hệ thống tin tức, blog chuẩn SEO để quảng bá sản phẩm

---

## ✨ Tính năng

### 🔐 Xác thực & Phân quyền
- ✅ Đăng ký/Đăng nhập với Spring Security
- ✅ Quên mật khẩu qua email với template đẹp
- ✅ Phân quyền người dùng (Admin, Staff, Customer)
- ✅ Session management an toàn
- ✅ CSRF protection

### 🛍️ Quản lý sản phẩm
- ✅ Danh mục sản phẩm theo vùng miền (Bắc, Trung, Nam)
- ✅ Tìm kiếm thông minh với autocomplete
- ✅ Lọc và sắp xếp sản phẩm đa tiêu chí
- ✅ Hình ảnh sản phẩm với lazy loading
- ✅ Sản phẩm nổi bật và khuyến mãi
- ✅ Quản lý kho hàng realtime

### 🛒 Giỏ hàng & Thanh toán
- ✅ Giỏ hàng localStorage (YAME behavior)
- ✅ Đồng bộ giỏ hàng cho cả guest và user
- ✅ Nhiều phương thức thanh toán (COD, VietQR, MoMo, VNPay)
- ✅ Tính phí vận chuyển theo khu vực
- ✅ Mã giảm giá và khuyến mãi
- ✅ Checkout 1-page đơn giản

### 📦 Quản lý đơn hàng
- ✅ Theo dõi đơn hàng với trạng thái realtime
- ✅ Lịch sử mua hàng chi tiết
- ✅ Hệ thống email thông báo đẹp mắt
- ✅ Quản lý vận chuyển (Giao Hàng Nhanh, DacSanVietShip)
- ✅ Cập nhật trạng thái tự động
- ✅ Xuất hóa đơn PDF

### 📧 Hệ thống Email
- ✅ Email xác nhận đặt hàng
- ✅ Email xác nhận thanh toán
- ✅ Email thông báo giao hàng
- ✅ Email hoàn tất đơn hàng
- ✅ Email thanh toán thất bại
- ✅ Template email responsive đẹp mắt
- ✅ Email tư vấn khách hàng

### 👨‍💼 Quản trị hệ thống
- ✅ Dashboard thống kê đẹp mắt với charts
- ✅ Quản lý sản phẩm, danh mục đa cấp
- ✅ Quản lý đơn hàng với bulk actions
- ✅ Báo cáo doanh thu chi tiết
- ✅ Quản lý khách hàng
- ✅ Cấu hình hệ thống linh hoạt
- ✅ Quản lý tin tức và nội dung

### 📰 Hệ thống tin tức
- ✅ Quản lý bài viết
- ✅ Danh mục tin tức đa cấp
- ✅ Tìm kiếm tin tức full-text
- ✅ SEO-friendly URLs
- ✅ Social sharing buttons

### 🎨 Giao diện & UX
- ✅ Responsive design (Mobile-first)
- ✅ Modern UI với Bootstrap 5.3
- ✅ Animations mượt mà với AOS
- ✅ Header/Footer fragments đồng bộ
- ✅ Theme màu teal chuyên nghiệp
- ✅ Loading states và error handling
- ✅ Accessibility support

### 💬 Tương tác khách hàng
- ✅ Live comment với WebSocket
- ✅ Hệ thống thông báo realtime
- ✅ Form liên hệ với validation
- ✅ Trang giới thiệu với video stories

### 🚀 Hiệu suất & Bảo mật
- ✅ Caching với EhCache
- ✅ Database optimization
- ✅ Image optimization và CDN
- ✅ SQL injection protection
- ✅ XSS protection
- ✅ Rate limiting
- ✅ Error handling toàn diện

## 🛠️ Công nghệ sử dụng

### Backend
- **Spring Boot 3.2.1** - Framework chính
- **Spring Security 6** - Bảo mật và xác thực
- **Spring Data JPA** - ORM và database access
- **MySQL 8.0** - Cơ sở dữ liệu chính
- **Redis** - Caching và session storage
- **JWT** - Token-based authentication
- **WebSocket** - Real-time notifications

### Frontend
- **Thymeleaf** - Template engine với fragments
- **Bootstrap 5.3** - CSS framework hiện đại
- **JavaScript ES6+** - Client-side logic
- **Font Awesome 6** - Icon library
- **SweetAlert2** - Beautiful alerts
- **AOS** - Scroll animations
- **Chart.js** - Data visualization
- **WebSocket** - Real-time communication

### DevOps & Tools
- **Maven** - Build tool
- **SockJS + STOMP** - WebSocket support
- **Swagger** - API documentation
- **MySQL Workbench** - Database management

## 🚀 Cài đặt

### Yêu cầu hệ thống
- ☕ **Java 17+**
- 🗄️ **MySQL 8.0+**
- 📦 **Maven 3.6+**
- 🟢 **Node.js 16+** (optional, for frontend build)

### 1. Clone repository
```bash
git clone https://github.com/vanhau123w-collab/DacSanViet.git
cd DacSanViet
```

### 2. Cấu hình database
Tạo database MySQL:
```sql
CREATE DATABASE DacSanViet CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'dacsanviet_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON DacSanViet.* TO 'dacsanviet_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Cấu hình application.properties
```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/DacSanViet
spring.datasource.username=dacsanviet_user
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.properties.hibernate.format_sql=true

# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=dacsanviethotro@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# File Upload Configuration
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# Application Configuration
app.frontend.url=http://localhost:8082
app.mail.from=dacsanviethotro@gmail.com
app.mail.to=dacsanviethotro@gmail.com

# Caching Configuration
spring.cache.type=ehcache
spring.cache.ehcache.config=classpath:ehcache.xml
```

### 4. Chạy ứng dụng
```bash
# Development mode
./mvnw spring-boot:run

# Production mode
./mvnw clean package
java -jar target/dacsanviet-0.0.1-SNAPSHOT.jar
```

### 5. Truy cập ứng dụng
- **Website**: http://localhost:8082
- **Admin Panel**: http://localhost:8082/admin
- **API Docs**: http://localhost:8082/swagger-ui.html

## ⚙️ Cấu hình

### Tài khoản mặc định
```
Admin Account:
- Username: admin
- Password: admin123
- Email: admin@dacsanviet.com

Test Customer:
- Username: user
- Password: user123
- Email: customer@dacsanviet.com
```

### Tính năng đặc biệt

#### YAME Behavior (Giỏ hàng)
- Sử dụng localStorage cho tất cả người dùng
- Không lưu giỏ hàng vào database
- Đồng bộ giỏ hàng giữa guest và authenticated user

#### Email Templates
- Template responsive đẹp mắt
- Hỗ trợ đa ngôn ngữ (Tiếng Việt)
- Tự động gửi email theo trạng thái đơn hàng

#### Vận chuyển thông minh
- Tự động chọn DacSanVietShip cho giao hàng nhanh 5H
- Hỗ trợ nhiều đơn vị vận chuyển
- Tracking number tự động

### Biến môi trường
```bash
# Database
DB_HOST=localhost
DB_PORT=3306
DB_NAME=DacSanViet
DB_USERNAME=root
DB_PASSWORD=your_password

# Email
MAIL_HOST=smtp.gmail.com
MAIL_USERNAME=dacsanviethotro@gmail.com
MAIL_PASSWORD=your-app-password

# Application
APP_FRONTEND_URL=http://localhost:8082
APP_MAIL_FROM=dacsanviethotro@gmail.com
APP_MAIL_TO=dacsanviethotro@gmail.com

# File Upload
UPLOAD_PATH=/uploads
MAX_FILE_SIZE=10MB
```

## 📚 API Documentation

### Cart Endpoints
```
GET    /cart                      # Trang giỏ hàng
POST   /cart/add                  # Thêm sản phẩm (JSON)
POST   /cart/update               # Cập nhật số lượng (JSON)
POST   /cart/remove               # Xóa sản phẩm (JSON)
POST   /cart/clear                # Xóa toàn bộ giỏ hàng
```

### Order Endpoints
```
GET    /orders                    # Danh sách đơn hàng
GET    /orders/{id}               # Chi tiết đơn hàng
POST   /checkout                  # Tạo đơn hàng mới
GET    /checkout/retry/{orderNumber} # Thanh toán lại
```

### Admin API Endpoints
```
PUT    /api/admin/orders/{id}     # Cập nhật đơn hàng (Admin)
GET    /api/admin/orders          # Danh sách đơn hàng (Admin)
POST   /api/admin/categories      # Tạo danh mục (Admin)
PUT    /api/admin/categories/{id} # Cập nhật danh mục (Admin)
```

### News Endpoints
```
GET    /news                      # Danh sách tin tức
GET    /news/{slug}               # Chi tiết bài viết
GET    /news/category/{slug}      # Tin tức theo danh mục
GET    /news/search               # Tìm kiếm tin tức
POST   /news/comments/submit      # Gửi bình luận
```

### Response Format
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": {
    // Response data here
  },
  "timestamp": "2024-01-01T00:00:00Z"
}
```

## 🧪 Testing

### Chạy tests
```bash
# Unit tests
./mvnw test

# Integration tests
./mvnw test -Dtest=**/*IntegrationTest

# Coverage report
./mvnw jacoco:report
```

### Test data
```bash
# Load sample data
./mvnw spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=test-data
```

## 🚀 Deployment

### Docker
```bash
# Build image
docker build -t DacSanViet .

# Run container
docker run -p 8080:8080 \
  -e DB_HOST=your-db-host \
  -e DB_USERNAME=your-username \
  -e DB_PASSWORD=your-password \
  DacSanViet
```

### Docker Compose
```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - DB_HOST=mysql
      - DB_USERNAME=root
      - DB_PASSWORD=password
    depends_on:
      - mysql
      - redis
  
  mysql:
    image: mysql:8.0
    environment:
      - MYSQL_ROOT_PASSWORD=password
      - MYSQL_DATABASE=DacSanViet
    volumes:
      - mysql_data:/var/lib/mysql
  
  redis:
    image: redis:7-alpine
    volumes:
      - redis_data:/data

volumes:
  mysql_data:
  redis_data:
```

------

## 💃🏻 Demo Giao Diện

### 🏡 Trang chủ

<table>
  <tr>
    <td><img src="screenshots/home-1.png" width="300"></td>
    <td><img src="screenshots/home-2.png" width="300"></td>
    <td><img src="screenshots/home-3.png" width="300"></td>
  </tr>
  <tr>
    <td><img src="screenshots/home-4.png" width="300"></td>
    <td><img src="screenshots/home-5.png" width="300"></td>
    <td><img src="screenshots/home-6.png" width="300"></td>
  </tr>
</table>

*Giao diện trang chủ trực quan sinh động*

### 🕺🏻 Trang Danh Sách Sản Phẩm

![Giao diện trang sản phẩm](screenshots/category-1.png)

*Giao diện trang danh sách sản phẩm trực quan sinh động, hỗ trợ filter theo danh mục, giá,..*

![Giao diện filter](screenshots/category-2.png)

*Filter hỗ trợ khoảng giá theo nhu cầu người dùng*

![Đánh giá khách hàng](screenshots/category-3.png)
*Các đánh giá của khách hàng (Mock Data)*

### 🙆🏻‍♀️ Trang Chi Tiết Sản Phẩm

![Giao diện chi tiết sản phẩm](screenshots/product-detail-1.png)
*Giao diện khá trực quan hỗ trợ zoom to hover giúp khách hàng xem rõ chi tiết của sản phẩm*

![Mô tả sản phẩm](screenshots/product-detail-2.png)
*Bao gồm các thông tin chi tiết của sản phẩm bao gồm nguồn gốc, xuất xứ, cân nặng,...*

![Đánh giá sản phẩm](screenshots/product-detail-3.png)
*Gồm các đánh giá của khách hàng (Mock Data)*

![Thảo luận real-time và FAQ](screenshots/product-detail-4.png)

### 🌱 Trang Giới Thiệu - About DacSanViet

<table>
  <tr>
    <td><img src="screenshots/about-1.png" width="350"></td>
    <td><img src="screenshots/about-2.png" width="350"></td>
  </tr>
</table>

*Những thông tin tổng quan về DacSanViet*

![Khám phá đặc sản theo tỉnh thành](screenshots/about-3.png)
*Khám phá đặc sản theo tỉnh thành góp phần quảng bá đặc sản địa phương*

![Tổng quan về đội ngủ](screenshots/about-4.png)
*Giới thiệu tổng quan về các thành viên DacSanViet*

![Video giới thiệu về DacSanViet](screenshots/about-5.png)

### 😴 Trang Giỏ Hàng

![Giỏ hàng](screenshots/cart.png)
*Giỏ hàng được lưu trữ local trên trình duyệt giúp giảm dung lượng cho DB*

### 🚚 Quy Trình Thanh Toán 1 page - Tạo ra sự tiện lợi cho khách hàng

![Quy trình thanh toán đơn giản](screenshots/checkout-1.png)
*Quy trình thanh toán đơn giản tạo sự tiện lợi cho khách hàng*

![Thanh toán](screenshots/checkout-2.png)

*Mã QR động hỗ trợ thanh toán tiện lợi*

![Email thông báo](screenshots/checkout-3.png)
*Hệ thống email thông báo tình trạng đơn hàng bao gồm:*
- Thanh toán thành công
- Thanh toán thất bại
- Đơn hàng đang được giao
- Giao hàng thành công,...

### 📱 Trang Tin Tức
![Tin Tức](screenshots/news.png)
*Trang tin tức - nơi người dùng cập nhật những thông tin mới nhất từ DacSanViet*

### 🗣️ Trang Liên Hệ
![Liên hệ](screenshots/contact.png)
*Trang liên hệ với các thông tin cơ bản của DacSanViet*

## Giao Diện ADMIN

### 🌝 Dashboard Tổng Quan
![Trang Dashboard](screenshots/admin-1.png)
*Dashboard tổng quan hiển thị những thông tin cơ bản về doanh thu, sản phẩm bán chạy,...*

| Quản lý Đơn Hàng | Quản lý Sản Phẩm | Quản lý Danh Mục |
|------|------|------|
| ![](screenshots/admin-2.png) | ![](screenshots/admin-3.png) | ![](screenshots/admin-4.png) |
| Quản lý Người Dùng | Quản lý Khuyến Mãi | Quản lý Tin Tức |
| ![](screenshots/admin-5.png) | ![](screenshots/admin-6.png) | ![](screenshots/admin-7.png) |

### 🤟 Thao Tác Với Đơn Hàng

| Xem Thông Tin Đơn Hàng | Cập Nhật Đơn Hàng 
|------|------|
| ![](screenshots/order-1.png) | ![](screenshots/order-2.png) | 

### 🫴 Thao Tác Với Sản Phẩm
![Sửa thông tin sản phẩm](screenshots/edit-product-1.png)
*Cập nhật thông tin sản phẩm, mô tả sản phẩm đẹp hơn với CKEditor - các phần khác cũng tương tự*


## 🤝 Đóng góp

Chúng tôi rất hoan nghênh mọi đóng góp! Vui lòng đọc thông tin bên dưới để biết thêm chi tiết.

### Quy trình đóng góp
1. Fork repository
2. Clone fork về máy local
3. Tạo branch mới cho feature: `git checkout -b feature/amazing-feature`
4. Commit changes: `git commit -m 'Add amazing feature'`
5. Push lên branch: `git push origin feature/amazing-feature`
6. Tạo Pull Request

### Code Style
- Sử dụng Google Java Style Guide
- Viết Javadoc cho public methods
- Unit tests cho logic quan trọng
- Integration tests cho API endpoints

## 📄 License

Dự án này được phân phối dưới giấy phép MIT. Xem [LICENSE](LICENSE) để biết thêm chi tiết.

## 👥 Team

- **Tee** - Lead Developer - Full Stack Development - [GitHub](https://github.com/teehihi)
- **PVH** - Backend Developer - API & Database Design - [GitHub](https://github.com/vanhau123w-collab)

## 📞 Liên hệ

- **Website**: https://dacsanviet.com
- **Email**: dacsanviethotro@gmail.com
- **Phone**: +84 93 165 2105
- **Address**: 01 Võ Văn Ngân, Phường Thủ Đức, TP.HCM

## 🙏 Acknowledgments

- [Spring Boot](https://spring.io/projects/spring-boot) - Framework tuyệt vời
- [Bootstrap](https://getbootstrap.com/) - CSS framework
- [Thymeleaf](https://www.thymeleaf.org/) - Template engine
- [Font Awesome](https://fontawesome.com/) - Icon library

---

<div align="center">

**⭐ Nếu bạn thích dự án này, hãy cho chúng tôi một star! ⭐**

Made with ❤️ by Đặc Sản Việt Team

</div>