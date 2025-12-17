# 🛒 Đặc Sản Quê Hương - E-commerce Platform

<div align="center">

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-brightgreen.svg)
![Java](https://img.shields.io/badge/Java-17-orange.svg)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)

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

**Đặc Sản Quê Hương** là một nền tảng thương mại điện tử hiện đại được xây dựng bằng Spring Boot, chuyên về việc bán các sản phẩm đặc sản từ khắp các vùng miền Việt Nam. Dự án tập trung vào trải nghiệm người dùng tuyệt vời với giao diện đẹp mắt, hiệu suất cao và tính năng phong phú.

### 🎯 Mục tiêu dự án

- Tạo ra một nền tảng bán hàng trực tuyến chuyên nghiệp
- Quảng bá các sản phẩm đặc sản Việt Nam
- Cung cấp trải nghiệm mua sắm tuyệt vời cho khách hàng
- Hỗ trợ các nhà bán hàng địa phương

## ✨ Tính năng

### 🔐 Xác thực & Phân quyền
- ✅ Đăng ký/Đăng nhập với JWT
- ✅ Xác thực 2 lớp (2FA)
- ✅ Quên mật khẩu qua email
- ✅ Phân quyền người dùng (Admin, Customer)
- ✅ Đăng nhập mạng xã hội (Google, Facebook)

### 🛍️ Quản lý sản phẩm
- ✅ Danh mục sản phẩm theo vùng miền
- ✅ Tìm kiếm thông minh với gợi ý
- ✅ Lọc và sắp xếp sản phẩm
- ✅ Đánh giá và nhận xét
- ✅ Sản phẩm yêu thích
- ✅ So sánh sản phẩm

### 🛒 Giỏ hàng & Thanh toán
- ✅ Giỏ hàng thời gian thực
- ✅ Nhiều phương thức thanh toán
- ✅ Mã giảm giá và khuyến mãi
- ✅ Tính phí vận chuyển tự động
- ✅ Lưu giỏ hàng cho lần sau

### 📦 Quản lý đơn hàng
- ✅ Theo dõi đơn hàng realtime
- ✅ Lịch sử mua hàng
- ✅ Hủy/Đổi trả đơn hàng
- ✅ Thông báo trạng thái đơn hàng
- ✅ In hóa đơn PDF

### 👨‍💼 Quản trị hệ thống
- ✅ Dashboard thống kê đẹp mắt
- ✅ Quản lý sản phẩm, danh mục
- ✅ Quản lý đơn hàng, khách hàng
- ✅ Báo cáo doanh thu chi tiết
- ✅ Quản lý kho hàng
- ✅ Cấu hình hệ thống

### 🎨 Giao diện & UX
- ✅ Responsive design (Mobile-first)
- ✅ Dark/Light mode
- ✅ Animations mượt mà
- ✅ PWA support
- ✅ Offline functionality
- ✅ Accessibility (WCAG 2.1)

### 🚀 Hiệu suất & Bảo mật
- ✅ Caching với Redis/EhCache
- ✅ CDN integration
- ✅ Image optimization
- ✅ Rate limiting
- ✅ SQL injection protection
- ✅ XSS protection

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
- **Thymeleaf** - Template engine
- **Bootstrap 5.3** - CSS framework
- **JavaScript ES6+** - Client-side logic
- **Chart.js** - Data visualization
- **SweetAlert2** - Beautiful alerts
- **AOS** - Scroll animations

### DevOps & Tools
- **Maven** - Build tool
- **Docker** - Containerization
- **GitHub Actions** - CI/CD
- **SonarQube** - Code quality
- **Swagger** - API documentation

## 🚀 Cài đặt

### Yêu cầu hệ thống

- **Java 17+** ☕
- **MySQL 8.0+** 🗄️
- **Maven 3.6+** 📦
- **Node.js 16+** (optional, for frontend build) 🟢

### 1. Clone repository

```bash
git clone https://github.com/yourusername/specialty-food-ecommerce.git
cd specialty-food-ecommerce
```

### 2. Cấu hình database

Tạo database MySQL:

```sql
CREATE DATABASE specialty_food_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'specialty_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON specialty_food_db.* TO 'specialty_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Cấu hình application.properties

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/specialty_food_db
spring.datasource.username=specialty_user
spring.datasource.password=your_password

# JWT Configuration
app.jwt.secret=your-256-bit-secret-key-here
app.jwt.expiration=86400000

# Email Configuration (for production)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

### 4. Chạy ứng dụng

```bash
# Development mode
./mvnw spring-boot:run

# Production mode
./mvnw clean package
java -jar target/specialty-food-ecommerce-0.0.1-SNAPSHOT.jar
```

### 5. Truy cập ứng dụng

- **Website**: http://localhost:8080
- **Admin Panel**: http://localhost:8080/admin
- **API Docs**: http://localhost:8080/swagger-ui.html

## ⚙️ Cấu hình

### Tài khoản mặc định

```
Admin Account:
- Username: admin
- Password: admin123
- Email: admin@dacsanquenhuong.vn

Test Customer:
- Username: customer
- Password: customer123
- Email: customer@example.com
```

### Biến môi trường

```bash
# Database
DB_HOST=localhost
DB_PORT=3306
DB_NAME=specialty_food_db
DB_USERNAME=specialty_user
DB_PASSWORD=your_password

# JWT
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000

# Email
MAIL_HOST=smtp.gmail.com
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# File Upload
UPLOAD_PATH=/uploads
MAX_FILE_SIZE=10MB
```

## 📚 API Documentation

### Authentication Endpoints

```http
POST /api/auth/register
POST /api/auth/login
POST /api/auth/refresh
POST /api/auth/logout
POST /api/auth/forgot-password
POST /api/auth/reset-password
```

### Product Endpoints

```http
GET    /api/products              # Lấy danh sách sản phẩm
GET    /api/products/{id}         # Lấy chi tiết sản phẩm
POST   /api/products              # Tạo sản phẩm mới (Admin)
PUT    /api/products/{id}         # Cập nhật sản phẩm (Admin)
DELETE /api/products/{id}         # Xóa sản phẩm (Admin)
GET    /api/products/search       # Tìm kiếm sản phẩm
GET    /api/products/featured     # Sản phẩm nổi bật
```

### Cart Endpoints

```http
GET    /api/cart                  # Lấy giỏ hàng
POST   /api/cart/add              # Thêm sản phẩm vào giỏ
PUT    /api/cart/update           # Cập nhật số lượng
DELETE /api/cart/remove           # Xóa sản phẩm khỏi giỏ
DELETE /api/cart/clear            # Xóa toàn bộ giỏ hàng
```

### Order Endpoints

```http
GET    /api/orders                # Lấy danh sách đơn hàng
GET    /api/orders/{id}           # Chi tiết đơn hàng
POST   /api/orders                # Tạo đơn hàng mới
PUT    /api/orders/{id}/status    # Cập nhật trạng thái (Admin)
DELETE /api/orders/{id}           # Hủy đơn hàng
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

- **Website**: https://dacsanquenhuong.vn
- **Email**: contact@dacsanquenhuong.vn
- **Phone**: +84 123 456 789
- **Address**: 123 Đường ABC, Quận XYZ, TP.HCM

## 🙏 Acknowledgments

- [Spring Boot](https://spring.io/projects/spring-boot) - Framework tuyệt vời
- [Bootstrap](https://getbootstrap.com/) - CSS framework
- [Thymeleaf](https://www.thymeleaf.org/) - Template engine
- [Unsplash](https://unsplash.com/) - Hình ảnh miễn phí
- [Icons8](https://icons8.com/) - Icons đẹp

---

<div align="center">

**⭐ Nếu bạn thích dự án này, hãy cho chúng tôi một star! ⭐**

Made with ❤️ by Đặc Sản Quê Hương Team

</div>