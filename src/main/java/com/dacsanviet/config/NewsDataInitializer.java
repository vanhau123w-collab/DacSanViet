package com.dacsanviet.config;

import com.dacsanviet.model.*;
import com.dacsanviet.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Enhanced News Data Initializer for comprehensive sample data
 * Creates realistic Vietnamese news content for testing and demonstration
 */
@Component
@Order(2) // Run after main DataLoader
@RequiredArgsConstructor
@Slf4j
public class NewsDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final NewsCategoryRepository newsCategoryRepository;
    private final NewsArticleRepository newsArticleRepository;
    private final NewsCommentRepository newsCommentRepository;
    
    private final Random random = new Random();

    @Override
    public void run(String... args) throws Exception {
        if (newsArticleRepository.count() > 5) {
            log.info("News data already initialized, skipping enhanced initialization");
            return;
        }
        
        log.info("Initializing enhanced news data...");
        
        // Get admin user
        User admin = userRepository.findByUsername("admin").orElse(null);
        User testUser = userRepository.findByUsername("user").orElse(null);
        
        if (admin == null) {
            log.warn("Admin user not found, skipping news data initialization");
            return;
        }
        
        // Create additional news categories if needed
        createAdditionalCategories();
        
        // Create comprehensive news articles
        createComprehensiveNewsArticles(admin);
        
        // Create sample comments if test user exists
        if (testUser != null) {
            createSampleComments(testUser);
        }
        
        log.info("Enhanced news data initialization completed successfully!");
    }
    
    private void createAdditionalCategories() {
        List<String[]> additionalCategories = Arrays.asList(
            new String[]{"Công Thức Nấu Ăn", "cong-thuc-nau-an", "Hướng dẫn nấu các món đặc sản truyền thống"},
            new String[]{"Văn Hóa Ẩm Thực", "van-hoa-am-thuc", "Khám phá văn hóa ẩm thực Việt Nam"},
            new String[]{"Mẹo Vặt Bếp Núc", "meo-vat-bep-nuc", "Những mẹo hay trong nấu nướng"},
            new String[]{"Nguyên Liệu Đặc Sản", "nguyen-lieu-dac-san", "Tìm hiểu về các nguyên liệu đặc sản"}
        );
        
        for (String[] categoryData : additionalCategories) {
            createNewsCategoryIfNotExists(categoryData[0], categoryData[1], categoryData[2]);
        }
    }
    
    private void createComprehensiveNewsArticles(User admin) {
        List<NewsCategory> categories = newsCategoryRepository.findAll();
        
        // Comprehensive article data
        List<ArticleData> articles = Arrays.asList(
            new ArticleData(
                "Bí Quyết Làm Bánh Chưng Xanh Ngon Như Người Miền Bắc",
                "Bánh chưng là món ăn truyền thống không thể thiếu trong dịp Tết Nguyên Đán. Cùng khám phá bí quyết làm bánh chưng xanh thơm ngon, đậm đà hương vị truyền thống.",
                generateComprehensiveContent("bánh chưng", "Tết Nguyên Đán", "truyền thống"),
                "Bánh chưng là món ăn truyền thống không thể thiếu trong dịp Tết Nguyên Đán của người Việt.",
                "https://images.unsplash.com/photo-1586190848861-99aa4a171e90?w=800&q=80",
                "https://images.unsplash.com/photo-1586190848861-99aa4a171e90?w=400&q=80",
                true, NewsStatus.PUBLISHED, "Công Thức Nấu Ăn"
            ),
            new ArticleData(
                "Khám Phá Ẩm Thực Đường Phố Sài Gòn - Hương Vị Không Thể Quên",
                "Sài Gòn nổi tiếng với nền ẩm thực đường phố phong phú và đa dạng. Từ bánh mì, phở, đến chè và các món ăn vặt, mỗi món đều mang một câu chuyện riêng.",
                generateComprehensiveContent("ẩm thực đường phố", "Sài Gòn", "bánh mì phở"),
                "Khám phá những món ăn đường phố đặc trưng của Sài Gòn với hương vị khó quên.",
                "https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?w=800&q=80",
                "https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?w=400&q=80",
                true, NewsStatus.PUBLISHED, "Văn Hóa Ẩm Thực"
            ),
            new ArticleData(
                "Cách Chọn và Bảo Quản Đặc Sản Miền Trung Tươi Ngon",
                "Miền Trung Việt Nam có nhiều đặc sản nổi tiếng như bánh tráng, mắm ruốc, bánh bèo. Việc chọn lựa và bảo quản đúng cách sẽ giúp giữ được hương vị tươi ngon nhất.",
                generateComprehensiveContent("đặc sản miền Trung", "bảo quản", "bánh tráng"),
                "Hướng dẫn cách chọn và bảo quản các đặc sản miền Trung để giữ được hương vị tươi ngon.",
                "https://images.unsplash.com/photo-1559847844-d721426d6edc?w=800&q=80",
                "https://images.unsplash.com/photo-1559847844-d721426d6edc?w=400&q=80",
                false, NewsStatus.PUBLISHED, "Mẹo Vặt Bếp Núc"
            ),
            new ArticleData(
                "Lịch Sử Và Nguồn Gốc Của Phở Việt Nam",
                "Phở là món ăn đại diện cho nền ẩm thực Việt Nam. Tìm hiểu về lịch sử hình thành và phát triển của món phở từ những ngày đầu đến nay.",
                generateComprehensiveContent("phở Việt Nam", "lịch sử", "ẩm thực"),
                "Khám phá lịch sử và nguồn gốc của món phở - biểu tượng ẩm thực Việt Nam.",
                "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=800&q=80",
                "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=400&q=80",
                true, NewsStatus.PUBLISHED, "Văn Hóa Ẩm Thực"
            ),
            new ArticleData(
                "Top 10 Đặc Sản Miền Bắc Phải Thử Một Lần Trong Đời",
                "Miền Bắc Việt Nam có rất nhiều đặc sản ngon và nổi tiếng. Cùng khám phá top 10 món ăn đặc sản miền Bắc mà bạn không thể bỏ qua.",
                generateComprehensiveContent("đặc sản miền Bắc", "top 10", "món ngon"),
                "Danh sách 10 đặc sản miền Bắc ngon nhất mà bạn nên thử ít nhất một lần trong đời.",
                "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=800&q=80",
                "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=400&q=80",
                false, NewsStatus.PUBLISHED, "Đặc Sản"
            ),
            new ArticleData(
                "Mùa Durian Đến Rồi - Cách Chọn Sầu Riêng Ngon Và An Toàn",
                "Sầu riêng là vua của các loại trái cây nhiệt đới. Trong mùa sầu riêng, việc biết cách chọn quả ngon và an toàn là rất quan trọng.",
                generateComprehensiveContent("sầu riêng", "cách chọn", "trái cây"),
                "Hướng dẫn cách chọn sầu riêng ngon, chín vừa và an toàn cho sức khỏe.",
                "https://images.unsplash.com/photo-1551024506-0bccd828d307?w=800&q=80",
                "https://images.unsplash.com/photo-1551024506-0bccd828d307?w=400&q=80",
                false, NewsStatus.PUBLISHED, "Nguyên Liệu Đặc Sản"
            ),
            new ArticleData(
                "Chương Trình Khuyến Mãi Đặc Biệt Tháng 1/2025",
                "Chào mừng năm mới 2025, chúng tôi triển khai chương trình khuyến mãi đặc biệt với nhiều ưu đãi hấp dẫn cho tất cả khách hàng.",
                generatePromotionContent(),
                "Chương trình khuyến mãi đặc biệt chào mừng năm mới 2025 với nhiều ưu đãi hấp dẫn.",
                "https://images.unsplash.com/photo-1607083206869-4c7672e72a8a?w=800&q=80",
                "https://images.unsplash.com/photo-1607083206869-4c7672e72a8a?w=400&q=80",
                true, NewsStatus.PUBLISHED, "Khuyến Mãi"
            ),
            new ArticleData(
                "Festival Ẩm Thực Việt Nam 2025 - Sự Kiện Không Thể Bỏ Lỡ",
                "Festival Ẩm Thực Việt Nam 2025 sẽ diễn ra vào tháng 3 tại Hà Nội, quy tụ các đầu bếp nổi tiếng và những món ăn đặc sắc từ khắp ba miền.",
                generateEventContent(),
                "Festival Ẩm Thực Việt Nam 2025 - sự kiện ẩm thực lớn nhất năm với sự tham gia của các đầu bếp nổi tiếng.",
                "https://images.unsplash.com/photo-1565299507177-b0ac66763828?w=800&q=80",
                "https://images.unsplash.com/photo-1565299507177-b0ac66763828?w=400&q=80",
                true, NewsStatus.PUBLISHED, "Sự Kiện"
            ),
            new ArticleData(
                "Xu Hướng Ẩm Thực Healthy 2025 - Ăn Ngon Mà Vẫn Khỏe",
                "Năm 2025 đánh dấu sự phát triển mạnh mẽ của xu hướng ẩm thực healthy. Cùng tìm hiểu cách kết hợp đặc sản truyền thống với lối sống lành mạnh.",
                generateHealthyFoodContent(),
                "Khám phá xu hướng ẩm thực healthy 2025 và cách kết hợp đặc sản truyền thống với lối sống lành mạnh.",
                "https://images.unsplash.com/photo-1563379091339-03246963d96c?w=800&q=80",
                "https://images.unsplash.com/photo-1563379091339-03246963d96c?w=400&q=80",
                false, NewsStatus.PUBLISHED, "Tin Tức Chung"
            ),
            new ArticleData(
                "Bí Quyết Kinh Doanh Đặc Sản Online Thành Công",
                "Trong thời đại số, việc kinh doanh đặc sản online đang trở thành xu hướng. Cùng tìm hiểu những bí quyết để thành công trong lĩnh vực này.",
                generateBusinessContent(),
                "Những bí quyết và kinh nghiệm quý báu để kinh doanh đặc sản online thành công.",
                "https://images.unsplash.com/photo-1565299624946-b28f40a0ca4b?w=800&q=80",
                "https://images.unsplash.com/photo-1565299624946-b28f40a0ca4b?w=400&q=80",
                false, NewsStatus.DRAFT, "Tin Tức Chung"
            )
        );
        
        for (ArticleData articleData : articles) {
            createNewsArticleIfNotExists(articleData, admin, categories);
        }
    }
    
    private void createSampleComments(User testUser) {
        List<NewsArticle> publishedArticles = newsArticleRepository.findAll().stream()
            .filter(article -> article.getStatus() == NewsStatus.PUBLISHED)
            .limit(5)
            .toList();
        
        String[] sampleComments = {
            "Bài viết rất hay và bổ ích! Cảm ơn tác giả đã chia sẻ.",
            "Thông tin rất chi tiết và dễ hiểu. Tôi sẽ thử làm theo hướng dẫn này.",
            "Đây chính là những gì tôi đang tìm kiếm. Cảm ơn rất nhiều!",
            "Bài viết chất lượng cao, nội dung phong phú và hấp dẫn.",
            "Rất hữu ích cho những ai yêu thích ẩm thực Việt Nam như tôi.",
            "Cảm ơn tác giả đã chia sẻ những kinh nghiệm quý báu này.",
            "Tôi đã học được rất nhiều điều mới từ bài viết này.",
            "Nội dung được trình bày một cách sinh động và dễ hiểu."
        };
        
        String[] guestNames = {
            "Nguyễn Văn An", "Trần Thị Bình", "Lê Hoàng Cường", "Phạm Thị Dung",
            "Hoàng Văn Em", "Vũ Thị Phương", "Đặng Minh Giang", "Bùi Thị Hoa"
        };
        
        String[] guestEmails = {
            "an.nguyen@email.com", "binh.tran@email.com", "cuong.le@email.com", "dung.pham@email.com",
            "em.hoang@email.com", "phuong.vu@email.com", "giang.dang@email.com", "hoa.bui@email.com"
        };
        
        for (NewsArticle article : publishedArticles) {
            // Create 2-4 comments per article
            int commentCount = random.nextInt(3) + 2;
            
            for (int i = 0; i < commentCount; i++) {
                NewsComment comment = new NewsComment();
                comment.setArticle(article);
                comment.setContent(sampleComments[random.nextInt(sampleComments.length)]);
                comment.setStatus(CommentStatus.APPROVED);
                
                // Mix of user and guest comments
                if (random.nextBoolean()) {
                    comment.setUser(testUser);
                } else {
                    comment.setGuestName(guestNames[random.nextInt(guestNames.length)]);
                    comment.setGuestEmail(guestEmails[random.nextInt(guestEmails.length)]);
                }
                
                newsCommentRepository.save(comment);
            }
        }
        
        log.info("Created sample comments for published articles");
    }
    
    private String generateComprehensiveContent(String mainTopic, String secondaryTopic, String keywords) {
        return String.format("""
            <h2>Giới Thiệu Về %s</h2>
            <p>%s là một phần không thể thiếu trong nền ẩm thực Việt Nam. Với hương vị đặc trưng và cách chế biến truyền thống, 
            %s đã trở thành món ăn được yêu thích bởi nhiều thế hệ người Việt.</p>
            
            <h2>Lịch Sử Và Nguồn Gốc</h2>
            <p>Theo các tài liệu lịch sử, %s có nguồn gốc từ %s và đã được truyền lại qua nhiều thế hệ. 
            Món ăn này không chỉ mang giá trị dinh dưỡng cao mà còn chứa đựng nhiều ý nghĩa văn hóa sâu sắc.</p>
            
            <h2>Nguyên Liệu Và Cách Chế Biến</h2>
            <p>Để có được một món %s ngon và đúng vị, việc lựa chọn nguyên liệu là vô cùng quan trọng. 
            Các nguyên liệu chính bao gồm %s và các gia vị truyền thống khác.</p>
            
            <h3>Quy Trình Chế Biến Chi Tiết</h3>
            <ol>
                <li>Chuẩn bị nguyên liệu tươi ngon và sạch sẽ</li>
                <li>Sơ chế nguyên liệu theo đúng quy trình truyền thống</li>
                <li>Chế biến theo các bước được truyền lại từ đời này sang đời khác</li>
                <li>Trang trí và thưởng thức khi còn nóng</li>
            </ol>
            
            <h2>Giá Trị Dinh Dưỡng</h2>
            <p>%s không chỉ ngon miệng mà còn cung cấp nhiều chất dinh dưỡng cần thiết cho cơ thể. 
            Món ăn này giàu protein, vitamin và các khoáng chất quan trọng.</p>
            
            <h2>Mẹo Hay Khi Thưởng Thức</h2>
            <p>Để thưởng thức %s một cách trọn vẹn nhất, bạn nên ăn khi món ăn còn nóng và kết hợp với 
            các loại rau sống tươi ngon. Điều này sẽ giúp tăng hương vị và giá trị dinh dưỡng của món ăn.</p>
            
            <h2>Kết Luận</h2>
            <p>%s là một món ăn truyền thống tuyệt vời của người Việt. Việc gìn giữ và phát huy những 
            giá trị ẩm thực này không chỉ giúp chúng ta kết nối với văn hóa dân tộc mà còn góp phần 
            quảng bá nền ẩm thực Việt Nam ra thế giới.</p>
            """, 
            mainTopic, mainTopic, mainTopic, mainTopic, secondaryTopic, 
            mainTopic, keywords, mainTopic, mainTopic, mainTopic);
    }
    
    private String generatePromotionContent() {
        return """
            <h2>Chương Trình Khuyến Mãi Đặc Biệt</h2>
            <p>Nhân dịp chào mừng năm mới 2025, chúng tôi triển khai chương trình khuyến mãi đặc biệt 
            với nhiều ưu đãi hấp dẫn dành cho tất cả khách hàng yêu mến đặc sản Việt Nam.</p>
            
            <h3>Các Ưu Đãi Nổi Bật</h3>
            <ul>
                <li><strong>Giảm giá 25%</strong> cho tất cả sản phẩm đặc sản Tết</li>
                <li><strong>Miễn phí vận chuyển</strong> cho đơn hàng từ 500.000đ</li>
                <li><strong>Tặng quà</strong> cho 100 khách hàng đầu tiên</li>
                <li><strong>Combo đặc biệt</strong> với giá ưu đãi chỉ 299.000đ</li>
            </ul>
            
            <h3>Thời Gian Áp Dụng</h3>
            <p>Chương trình diễn ra từ ngày 1/1/2025 đến hết ngày 31/1/2025. 
            Số lượng có hạn, áp dụng theo nguyên tắc "ai trước người đó".</p>
            
            <h3>Điều Kiện Tham Gia</h3>
            <p>Chương trình áp dụng cho tất cả khách hàng, không giới hạn số lần mua. 
            Không áp dụng đồng thời với các chương trình khuyến mãi khác.</p>
            """;
    }
    
    private String generateEventContent() {
        return """
            <h2>Festival Ẩm Thực Việt Nam 2025</h2>
            <p>Festival Ẩm Thực Việt Nam 2025 là sự kiện ẩm thực lớn nhất trong năm, 
            quy tụ các đầu bếp nổi tiếng và những món ăn đặc sắc từ khắp ba miền Bắc - Trung - Nam.</p>
            
            <h3>Thông Tin Sự Kiện</h3>
            <ul>
                <li><strong>Thời gian:</strong> 15-17/3/2025</li>
                <li><strong>Địa điểm:</strong> Trung tâm Hội nghị Quốc gia, Hà Nội</li>
                <li><strong>Quy mô:</strong> Hơn 200 gian hàng từ 63 tỉnh thành</li>
                <li><strong>Khách mời:</strong> 50+ đầu bếp nổi tiếng trong nước và quốc tế</li>
            </ul>
            
            <h3>Hoạt Động Nổi Bật</h3>
            <ul>
                <li>Trình diễn nấu ăn từ các đầu bếp nổi tiếng</li>
                <li>Thi đấu ẩm thực giữa các vùng miền</li>
                <li>Triển lãm ảnh về ẩm thực Việt Nam</li>
                <li>Hội thảo về xu hướng ẩm thực hiện đại</li>
                <li>Khu vực thưởng thức và mua sắm đặc sản</li>
            </ul>
            
            <h3>Đăng Ký Tham Gia</h3>
            <p>Sự kiện miễn phí cho tất cả du khách. Đăng ký trước để nhận được những ưu đãi đặc biệt 
            và tránh tình trạng quá tải.</p>
            """;
    }
    
    private String generateHealthyFoodContent() {
        return """
            <h2>Xu Hướng Ẩm Thực Healthy 2025</h2>
            <p>Năm 2025 đánh dấu sự phát triển mạnh mẽ của xu hướng ẩm thực healthy. 
            Người tiêu dùng ngày càng quan tâm đến việc ăn uống lành mạnh mà vẫn giữ được hương vị truyền thống.</p>
            
            <h3>Các Xu Hướng Nổi Bật</h3>
            <ul>
                <li><strong>Plant-based:</strong> Tăng cường thực phẩm từ thực vật</li>
                <li><strong>Organic:</strong> Ưu tiên nguyên liệu hữu cơ, sạch</li>
                <li><strong>Low-carb:</strong> Giảm tinh bột, tăng protein và chất xơ</li>
                <li><strong>Fermented foods:</strong> Thực phẩm lên men tốt cho tiêu hóa</li>
            </ul>
            
            <h3>Kết Hợp Đặc Sản Truyền Thống</h3>
            <p>Các đầu bếp hiện đại đang tìm cách kết hợp đặc sản truyền thống với xu hướng healthy, 
            tạo ra những món ăn vừa ngon vừa bổ dưỡng.</p>
            
            <h3>Lời Khuyên Cho Người Tiêu Dùng</h3>
            <ul>
                <li>Chọn nguyên liệu tươi ngon, có nguồn gốc rõ ràng</li>
                <li>Cân bằng dinh dưỡng trong mỗi bữa ăn</li>
                <li>Hạn chế đường và muối</li>
                <li>Tăng cường rau xanh và trái cây</li>
            </ul>
            """;
    }
    
    private String generateBusinessContent() {
        return """
            <h2>Kinh Doanh Đặc Sản Online - Cơ Hội Và Thách Thức</h2>
            <p>Trong thời đại số hóa, việc kinh doanh đặc sản online đang trở thành xu hướng mới. 
            Đây là cơ hội tuyệt vời để đưa những món ăn truyền thống đến gần hơn với người tiêu dùng.</p>
            
            <h3>Những Lợi Thế Của Kinh Doanh Online</h3>
            <ul>
                <li>Tiếp cận khách hàng rộng khắp cả nước</li>
                <li>Chi phí vận hành thấp hơn cửa hàng truyền thống</li>
                <li>Dễ dàng quản lý và theo dõi đơn hàng</li>
                <li>Có thể bán hàng 24/7</li>
            </ul>
            
            <h3>Bí Quyết Thành Công</h3>
            <ol>
                <li><strong>Chất lượng sản phẩm:</strong> Luôn đảm bảo chất lượng tốt nhất</li>
                <li><strong>Đóng gói chuyên nghiệp:</strong> Bảo quản và vận chuyển an toàn</li>
                <li><strong>Dịch vụ khách hàng:</strong> Tư vấn nhiệt tình, giải đáp nhanh chóng</li>
                <li><strong>Marketing hiệu quả:</strong> Sử dụng mạng xã hội và SEO</li>
                <li><strong>Xây dựng thương hiệu:</strong> Tạo dựng uy tín và niềm tin</li>
            </ol>
            
            <h3>Những Thách Thức Cần Vượt Qua</h3>
            <ul>
                <li>Cạnh tranh gay gắt trên thị trường online</li>
                <li>Vấn đề bảo quản và vận chuyển</li>
                <li>Xây dựng niềm tin với khách hàng</li>
                <li>Quản lý kho bãi và logistics</li>
            </ul>
            """;
    }
    
    private NewsCategory createNewsCategoryIfNotExists(String name, String slug, String description) {
        return newsCategoryRepository.findByName(name).orElseGet(() -> {
            NewsCategory category = new NewsCategory();
            category.setName(name);
            category.setSlug(slug);
            category.setDescription(description);
            category.setIsActive(true);
            category.setSortOrder(0);
            return newsCategoryRepository.save(category);
        });
    }
    
    private NewsArticle createNewsArticleIfNotExists(ArticleData data, User author, List<NewsCategory> categories) {
        return newsArticleRepository.findBySlug(data.slug).orElseGet(() -> {
            NewsCategory category = categories.stream()
                .filter(c -> c.getName().equals(data.categoryName))
                .findFirst()
                .orElse(categories.get(0)); // Fallback to first category
            
            NewsArticle article = new NewsArticle();
            article.setTitle(data.title);
            article.setSlug(data.slug);
            article.setExcerpt(data.excerpt);
            article.setContent(data.content);
            article.setMetaDescription(data.metaDescription);
            article.setFeaturedImage(data.featuredImage);
            article.setThumbnailImage(data.thumbnailImage);
            article.setCategory(category);
            article.setAuthor(author);
            article.setIsFeatured(data.isFeatured);
            article.setStatus(data.status);
            article.setViewCount((long) random.nextInt(1000) + 100); // Random view count 100-1099
            
            if (data.status == NewsStatus.PUBLISHED) {
                article.setPublishedAt(LocalDateTime.now().minusDays(random.nextInt(30))); // Random publish date within last 30 days
            }
            
            return newsArticleRepository.save(article);
        });
    }
    
    // Helper class for article data
    private static class ArticleData {
        final String title;
        final String slug;
        final String excerpt;
        final String content;
        final String metaDescription;
        final String featuredImage;
        final String thumbnailImage;
        final boolean isFeatured;
        final NewsStatus status;
        final String categoryName;
        
        ArticleData(String title, String excerpt, String content, String metaDescription,
                   String featuredImage, String thumbnailImage, boolean isFeatured, 
                   NewsStatus status, String categoryName) {
            this.title = title;
            this.slug = createSlugFromTitle(title);
            this.excerpt = excerpt;
            this.content = content;
            this.metaDescription = metaDescription;
            this.featuredImage = featuredImage;
            this.thumbnailImage = thumbnailImage;
            this.isFeatured = isFeatured;
            this.status = status;
            this.categoryName = categoryName;
        }
        
        private static String createSlugFromTitle(String title) {
            return title.toLowerCase()
                .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                .replaceAll("[ìíịỉĩ]", "i")
                .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                .replaceAll("[ùúụủũưừứựửữ]", "u")
                .replaceAll("[ỳýỵỷỹ]", "y")
                .replaceAll("[đ]", "d")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        }
    }
}