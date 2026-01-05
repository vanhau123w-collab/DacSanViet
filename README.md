# 🛒 Đặc Sản Việt - E-commerce Platform

<div align="center">

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-brightgreen.svg)
![Java](https://img.shields.io/badge/Java-17-orange.svg)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)
![Status](https://img.shields.io/badge/Status-Production%20Ready-success.svg)

**Nền tảng thương mại điện tử hiện đại cho đặc sản Việt Nam**

[Demo](#demo) • [Tính năng](#tính-năng) • [Cài đặt](#cài-đặt) • [API](#api-documentation) • [Đóng góp](#đóng-góp)

</div>

---

## 📋 Mục lục

- [Giới thiệu](#giới-thiệu)
- [Tính năng](#tính-năng)
- [Công nghệ sử dụng](#công-nghệ-sử-dụng)
- [Cài đặt](#cài-đặt)
- [Cấu hình](#cấu-hình)
- [API Documentation](#api-documentation)
- [Screenshots](#screenshots)
- [Đóng góp](#đóng-góp)
- [License](#license)

## 🌟 Giới thiệu

**Đặc Sản Việt** là một nền tảng thương mại điện tử hiện đại được xây dựng bằng Spring Boot, chuyên về việc bán các sản phẩm đặc sản từ khắp các vùng miền Việt Nam. Dự án tập trung vào trải nghiệm người dùng tuyệt vời với giao diện đẹp mắt, hiệu suất cao và tính năng phong phú.

### 🎯 Mục tiêu dự án

- Tạo ra một nền tảng bán hàng trực tuyến chuyên nghiệp
- Quảng bá các sản phẩm đặc sản Việt Nam
- Cung cấp trải nghiệm mua sắm tuyệt vời cho khách hàng
- Hỗ trợ các nhà bán hàng địa phương

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
- ✅ Quản lý khách hàng và đánh giá
- ✅ Cấu hình hệ thống linh hoạt
- ✅ Quản lý tin tức và nội dung

### 📰 Hệ thống tin tức
- ✅ Quản lý bài viết với WYSIWYG editor
- ✅ Danh mục tin tức đa cấp
- ✅ Tìm kiếm tin tức full-text
- ✅ Hệ thống bình luận với reply
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
- ✅ Live chat với WebSocket
- ✅ Hệ thống thông báo realtime
- ✅ Form liên hệ với validation
- ✅ Trang giới thiệu với video stories
- ✅ Chatbox tự động

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

- **Java 17+** ☕
- **MySQL 8.0+** 🗄️
- **Maven 3.6+** 📦
- **Node.js 16+** (optional, for frontend build) 🟢

### 1. Clone repository

```bash
git clone https://github.com/vanhau123w-collab/DacSanViet.git
cd specialty-food-ecommerce
```

### 2. Cấu hình database

Tạo database MySQL:

```sql
CREATE DATABASE dacsanviet_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'dacsanviet_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON dacsanviet_db.* TO 'dacsanviet_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Cấu hình application.properties

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/dacsanviet_db
spring.datasource.username=dacsanviet_user
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
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
DB_NAME=dacsanviet
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

```http
GET    /cart                      # Trang giỏ hàng
POST   /cart/add                  # Thêm sản phẩm (JSON)
POST   /cart/update               # Cập nhật số lượng (JSON)
POST   /cart/remove               # Xóa sản phẩm (JSON)
POST   /cart/clear                # Xóa toàn bộ giỏ hàng
```

### Order Endpoints

```http
GET    /orders                    # Danh sách đơn hàng
GET    /orders/{id}               # Chi tiết đơn hàng
POST   /checkout                  # Tạo đơn hàng mới
GET    /checkout/retry/{orderNumber} # Thanh toán lại
```

### Admin API Endpoints

```http
PUT    /api/admin/orders/{id}     # Cập nhật đơn hàng (Admin)
GET    /api/admin/orders          # Danh sách đơn hàng (Admin)
POST   /api/admin/categories      # Tạo danh mục (Admin)
PUT    /api/admin/categories/{id} # Cập nhật danh mục (Admin)
```

### News Endpoints

```http
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
docker build -t specialty-food-ecommerce .

# Run container
docker run -p 8080:8080 \
  -e DB_HOST=your-db-host \
  -e DB_USERNAME=your-username \
  -e DB_PASSWORD=your-password \
  specialty-food-ecommerce
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
      - MYSQL_DATABASE=specialty_food_db
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

## 🤝 Đóng góp

Chúng tôi rất hoan nghênh mọi đóng góp! Vui lòng đọc [CONTRIBUTING.md](CONTRIBUTING.md) để biết thêm chi tiết.

### Quy trình đóng góp

1. **Fork** repository
2. **Clone** fork về máy local
3. **Tạo branch** mới cho feature: `git checkout -b feature/amazing-feature`
4. **Commit** changes: `git commit -m 'Add amazing feature'`
5. **Push** lên branch: `git push origin feature/amazing-feature`
6. **Tạo Pull Request**

### Code Style

- Sử dụng **Google Java Style Guide**
- Viết **Javadoc** cho public methods
- **Unit tests** cho logic quan trọng
- **Integration tests** cho API endpoints

## 📄 License

Dự án này được phân phối dưới giấy phép MIT. Xem [LICENSE](LICENSE) để biết thêm chi tiết.

## 👥 Team

- **Lead Developer** - *Full Stack Development*
- **Frontend Developer** - *UI/UX Design & Implementation*
- **Backend Developer** - *API & Database Design*

## 📞 Liên hệ

- **Website**: https://dacsanviet.com
- **Email**: dacsanviethotro@gmail.com
- **Phone**: +84 93 165 2105
- **Address**: 01 Võ Văn Ngân, Phường Thủ Đức, TP.HCM
- **Tee's Info**: [https://github.com/teehihi](https://github.com/teehihi)
- **PVH's Info**: [https://github.com/vanhau123w-collab](https://github.com/vanhau123w-collab)

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