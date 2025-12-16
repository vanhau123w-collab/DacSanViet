# Specialty Food E-commerce Website

Hệ thống website bán hàng đặc sản quê hương được xây dựng với Spring Boot, cung cấp nền tảng thương mại điện tử cho các sản phẩm đặc sản địa phương.

## Công nghệ sử dụng

- **Backend**: Spring Boot 3.2.1, Spring Security, Spring Data JPA
- **Frontend**: Thymeleaf, CSS Framework (Tailwind CSS/Bootstrap)
- **Database**: MySQL (Production), H2 (Testing)
- **Authentication**: JWT + Spring Security
- **Real-time**: WebSocket với STOMP
- **Testing**: JUnit 5, Mockito, jqwik (Property-Based Testing)

## Cấu trúc dự án

```
src/
├── main/
│   ├── java/com/specialtyfood/
│   │   ├── controller/     # REST endpoints và web controllers
│   │   ├── service/        # Business logic
│   │   ├── repository/     # Data access layer
│   │   ├── model/          # JPA entities
│   │   ├── dto/            # Data Transfer Objects
│   │   └── config/         # Spring configuration
│   └── resources/
│       ├── templates/      # Thymeleaf templates
│       ├── static/         # CSS, JS, images
│       └── application.properties
└── test/
    └── java/com/specialtyfood/
        └── ...             # Test classes
```

## Yêu cầu hệ thống

- Java 17 hoặc cao hơn
- Maven 3.6+
- MySQL 8.0+ (cho production)

## Cài đặt và chạy

### 1. Clone repository
```bash
git clone <repository-url>
cd specialty-food-ecommerce
```

### 2. Cấu hình database
Cập nhật thông tin database trong `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/specialty_food_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 3. Chạy ứng dụng
```bash
# Compile và chạy tests
mvn clean test

# Chạy ứng dụng
mvn spring-boot:run
```

Ứng dụng sẽ chạy tại: http://localhost:8080

## Tính năng chính

### Cho khách hàng:
- Duyệt và tìm kiếm sản phẩm đặc sản
- Thêm sản phẩm vào giỏ hàng và thanh toán
- Tạo tài khoản và quản lý thông tin cá nhân
- Theo dõi đơn hàng và lịch sử mua hàng

### Cho admin:
- Quản lý sản phẩm và danh mục
- Quản lý đơn hàng và khách hàng
- Xem báo cáo bán hàng và thống kê
- Quản lý hệ thống thông báo

### Tính năng kỹ thuật:
- Bảo mật với JWT authentication
- Thông báo real-time qua WebSocket
- Giao diện responsive cho mobile
- Property-based testing cho đảm bảo chất lượng

## Testing

### Chạy tests
```bash
# Chạy tất cả tests
mvn test

# Chạy tests với profile cụ thể
mvn test -Dspring.profiles.active=test
```

### Loại tests:
- **Unit Tests**: Test các component riêng lẻ
- **Integration Tests**: Test tương tác giữa các component
- **Property-Based Tests**: Test với jqwik để kiểm tra tính đúng đắn

## Cấu hình môi trường

### Development
- Database: H2 in-memory
- Logging: DEBUG level
- Hot reload: Enabled

### Production
- Database: MySQL cluster
- Logging: INFO level
- Security: Enhanced
- Performance: Optimized

## API Documentation

API endpoints sẽ được documented khi các controller được implement trong các task tiếp theo.

## Đóng góp

1. Fork repository
2. Tạo feature branch
3. Commit changes
4. Push to branch
5. Tạo Pull Request

## License

[Thêm thông tin license nếu cần]