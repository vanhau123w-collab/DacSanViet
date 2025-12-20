package com.dacsanviet.service;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.dacsanviet.dao.OrderDao;
import com.dacsanviet.dto.ConsultationRequest;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

	private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

	@Autowired
	private JavaMailSender mailSender;

	@Value("${app.mail.from}")
	private String fromEmail;

	@Value("${app.mail.to}")
	private String toEmail;

	@Value("${app.frontend.url}")
	private String frontendUrl;

	public void sendConsultationEmail(ConsultationRequest request) throws UnsupportedEncodingException {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			helper.setFrom(fromEmail, "Đặc Sản Việt");
			helper.setTo(toEmail);
			helper.setSubject("Yêu Cầu Tư Vấn Mới - Đặc Sản Việt");

			String htmlContent = buildEmailContent(request);
			helper.setText(htmlContent, true);

			mailSender.send(message);
			logger.info("Email sent successfully to {} for consultation request from {}", toEmail, request.getName());

		} catch (MessagingException e) {
			logger.error("Failed to send consultation email", e);
			throw new RuntimeException("Không thể gửi email. Vui lòng thử lại sau!", e);
		}
	}

	private String getInterestDisplayName(String interestValue) {
		if (interestValue == null || interestValue.isEmpty()) {
			return "Không chọn";
		}
		return switch (interestValue) {
		case "mien-bac" -> "Đặc sản miền Bắc";
		case "mien-trung" -> "Đặc sản miền Trung";
		case "mien-nam" -> "Đặc sản miền Nam";
		case "tet" -> "Sản phẩm Tết";
		case "qua-tang" -> "Quà tặng doanh nghiệp";
		default -> interestValue;
		};
	}

	private String buildEmailContent(ConsultationRequest request) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
		String currentTime = LocalDateTime.now().format(formatter);

		String name = request.getName();
		String phone = request.getPhone();
		String email = request.getEmail() != null ? request.getEmail() : "Không cung cấp";
		String interestDisplay = getInterestDisplayName(request.getInterest());
		String message = request.getMessage() != null && !request.getMessage().isEmpty() ? request.getMessage()
				: "Không có ghi chú";

		return String.format(
				"""
						<!DOCTYPE html>
						<html>
						<head>
						    <meta charset="UTF-8">
						    <meta name="viewport" content="width=device-width, initial-scale=1.0">
						</head>
						<body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif; background-color: #f5f5f5;">
						    <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="background-color: #f5f5f5; padding: 20px 0;">
						        <tr>
						            <td align="center">
						                <table width="600" cellpadding="0" cellspacing="0" border="0" style="background-color: #ffffff; max-width: 600px;">
						                    <tr>
						                        <td style="background: linear-gradient(135deg, #4ec2b6 0%%, #2e857c 100%%); padding: 40px 30px; text-align: center;">
						                            <table width="100%%" cellpadding="0" cellspacing="0" border="0">
						                                <tr>
						                                    <td align="center">
						                                        <img src="https://files.catbox.moe/5uf8r1.png" alt="Đặc Sản Việt" style="max-width: 200px; max-height: 200px; width: auto; height: auto; display: block; margin: 0 auto 20px;">
						                                    </td>
						                                </tr>
						                                <tr>
						                                    <td align="center" style="color: #ffffff; font-size: 26px; font-weight: 700; padding: 10px 0 5px;">
						                                        Đặc Sản Việt
						                                    </td>
						                                </tr>
						                                <tr>
						                                    <td align="center" style="color: #ffffff; font-size: 14px; opacity: 0.95;">
						                                        Gìn giữ hồn quê, lan toả giá trị Việt
						                                    </td>
						                                </tr>
						                            </table>
						                        </td>
						                    </tr>
						                    <tr>
						                        <td style="background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 20px 30px;">
						                            <table width="100%%" cellpadding="0" cellspacing="0" border="0">
						                                <tr>
						                                    <td style="vertical-align: middle;">
						                                        <div style="font-weight: 700; color: #856404; font-size: 17px; margin-bottom: 5px;">
						                                            Yêu Cầu Tư Vấn Mới
						                                        </div>
						                                        <div style="color: #856404; font-size: 14px;">
						                                            Vui lòng liên hệ khách hàng trong vòng 24 giờ
						                                        </div>
						                                    </td>
						                                </tr>
						                            </table>
						                        </td>
						                    </tr>
						                    <tr>
						                        <td style="padding: 35px 30px;">
						                            <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="margin-bottom: 25px;">
						                                <tr>
						                                    <td style="font-size: 18px; font-weight: 700; color: #333333; padding-bottom: 12px; border-bottom: 3px solid #4ec2b6;">
						                                        Thông Tin Khách Hàng
						                                    </td>
						                                </tr>
						                            </table>
						                            <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="background-color: #f8f9fa; border-radius: 8px; overflow: hidden;">
						                                <tr>
						                                    <td style="padding: 15px 20px; border-bottom: 1px solid #e9ecef;">
						                                        <table width="100%%" cellpadding="0" cellspacing="0" border="0">
						                                            <tr>
						                                                <td width="140" style="font-weight: 600; color: #4ec2b6; font-size: 14px; vertical-align: top;">
						                                                    Họ và tên
						                                                </td>
						                                                <td style="color: #000000; font-size: 15px; font-weight: 600;">%s</td>
						                                            </tr>
						                                        </table>
						                                    </td>
						                                </tr>
						                                <tr>
						                                    <td style="padding: 15px 20px; border-bottom: 1px solid #e9ecef;">
						                                        <table width="100%%" cellpadding="0" cellspacing="0" border="0">
						                                            <tr>
						                                                <td width="140" style="font-weight: 600; color: #4ec2b6; font-size: 14px; vertical-align: top;">
						                                                    Số điện thoại
						                                                </td>
						                                                <td style="color: #000000; font-size: 15px; font-weight: 600;">
						                                                    <a href="tel:%s" style="color: #4ec2b6; text-decoration: none; font-weight: 700;">%s</a>
						                                                </td>
						                                            </tr>
						                                        </table>
						                                    </td>
						                                </tr>
						                                <tr>
						                                    <td style="padding: 15px 20px; border-bottom: 1px solid #e9ecef;">
						                                        <table width="100%%" cellpadding="0" cellspacing="0" border="0">
						                                            <tr>
						                                                <td width="140" style="font-weight: 600; color: #4ec2b6; font-size: 14px; vertical-align: top;">
						                                                    Email
						                                                </td>
						                                                <td style="color: #333333; font-size: 14px;">
						                                                    <a href="mailto:%s" style="color: #4ec2b6; text-decoration: none;">%s</a>
						                                                </td>
						                                            </tr>
						                                        </table>
						                                    </td>
						                                </tr>
						                                <tr>
						                                    <td style="padding: 15px 20px; border-bottom: 1px solid #e9ecef;">
						                                        <table width="100%%" cellpadding="0" cellspacing="0" border="0">
						                                            <tr>
						                                                <td width="140" style="font-weight: 600; color: #4ec2b6; font-size: 14px; vertical-align: top;">
						                                                    Quan tâm
						                                                </td>
						                                                <td style="color: #333333; font-size: 14px; font-weight: 600;">%s</td>
						                                            </tr>
						                                        </table>
						                                    </td>
						                                </tr>
						                                <tr>
						                                    <td style="padding: 15px 20px; border-bottom: 1px solid #e9ecef;">
						                                        <table width="100%%" cellpadding="0" cellspacing="0" border="0">
						                                            <tr>
						                                                <td width="140" style="font-weight: 600; color: #4ec2b6; font-size: 14px; vertical-align: top;">
						                                                    Ghi chú
						                                                </td>
						                                                <td style="color: #333333; font-size: 14px; line-height: 1.6;">%s</td>
						                                            </tr>
						                                        </table>
						                                    </td>
						                                </tr>
						                                <tr>
						                                    <td style="padding: 15px 20px;">
						                                        <table width="100%%" cellpadding="0" cellspacing="0" border="0">
						                                            <tr>
						                                                <td width="140" style="font-weight: 600; color: #4ec2b6; font-size: 14px; vertical-align: top;">
						                                                    Thời gian
						                                                </td>
						                                                <td style="color: #666666; font-size: 13px;">%s</td>
						                                            </tr>
						                                        </table>
						                                    </td>
						                                </tr>
						                            </table>
						                            <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="margin-top: 30px;">
						                                <tr>
						                                    <td align="center" style="background-color: #f0f8f7; border-radius: 10px; padding: 25px;">
						                                        <div style="color: #666666; font-size: 14px; margin-bottom: 18px;">
						                                            Hãy liên hệ ngay với khách hàng để tư vấn và chốt đơn hàng
						                                        </div>
						                                        <a href="tel:%s" style="display: inline-block; background: linear-gradient(135deg, #4ec2b6 0%%, #2e857c 100%%); color: #ffffff; text-decoration: none; padding: 14px 35px; border-radius: 8px; font-weight: 700; font-size: 15px; box-shadow: 0 4px 12px rgba(78, 194, 182, 0.3);">
						                                            Gọi Ngay Cho Khách Hàng
						                                        </a>
						                                    </td>
						                                </tr>
						                            </table>
						                        </td>
						                    </tr>
						                    <tr>
						                        <td style="background-color: #f8f9fa; padding: 30px; border-top: 1px solid #e9ecef;">
						                            <table width="100%%" cellpadding="0" cellspacing="0" border="0">
						                                <tr>
						                                    <td align="center" style="padding-bottom: 15px;">
						                                        <div style="font-weight: 600; color: #333333; font-size: 14px; margin-bottom: 10px;">
						                                            Liên Hệ
						                                        </div>
						                                        <div style="color: #666666; font-size: 13px; line-height: 1.8;">
						                                            01 Võ Văn Ngân, Phường Thủ Đức, TP Hồ Chí Minh<br>
						                                            Hotline: 1900-xxxx<br>
						                                            <a href="mailto:dacsanviethotro@gmail.com" style="color: #4ec2b6; text-decoration: none;">dacsanviethotro@gmail.com</a>
						                                        </div>
						                                    </td>
						                                </tr>
						                                <tr>
						                                    <td style="padding: 15px 0;">
						                                        <div style="height: 1px; background-color: #e0e0e0;"></div>
						                                    </td>
						                                </tr>
						                                <tr>
						                                    <td align="center" style="padding-bottom: 15px;">
						                                        <div style="color: #666666; font-size: 13px;">
						                                            <a href="%s" style="color: #4ec2b6; text-decoration: none; font-weight: 500;">Trang chủ</a> •
						                                            <a href="%s/products" style="color: #4ec2b6; text-decoration: none; font-weight: 500;">Sản phẩm</a> •
						                                            <a href="%s/contact" style="color: #4ec2b6; text-decoration: none; font-weight: 500;">Liên hệ</a>
						                                        </div>
						                                    </td>
						                                </tr>
						                                <tr>
						                                    <td align="center" style="color: #999999; font-size: 12px;">
						                                        © 2025 Đặc Sản Việt. All rights reserved.
						                                    </td>
						                                </tr>
						                            </table>
						                        </td>
						                    </tr>
						                </table>
						            </td>
						        </tr>
						    </table>
						</body>
						</html>
						""",
				name, phone, phone, email, email, interestDisplay, message, currentTime, phone, frontendUrl,
				frontendUrl, frontendUrl);
	}

	public void sendPasswordResetEmail(String toEmail, String resetToken) throws UnsupportedEncodingException {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			helper.setFrom(fromEmail, "Đặc Sản Việt");
			helper.setTo(toEmail);
			helper.setSubject("🔐 Đặt Lại Mật Khẩu - Đặc Sản Việt");

			String resetLink = frontendUrl + "/reset-password?token=" + resetToken;
			String htmlContent = buildPasswordResetEmailContent(resetLink);
			helper.setText(htmlContent, true);

			mailSender.send(message);
			logger.info("Password reset email sent successfully to {}", toEmail);

		} catch (MessagingException e) {
			logger.error("Failed to send password reset email to {}", toEmail, e);
			throw new RuntimeException("Không thể gửi email đặt lại mật khẩu. Vui lòng thử lại sau!", e);
		}
	}

	public void sendPasswordResetConfirmationEmail(String toEmail) throws UnsupportedEncodingException {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			helper.setFrom(fromEmail, "Đặc Sản Việt");
			helper.setTo(toEmail);
			helper.setSubject("✅ Mật Khẩu Đã Được Đặt Lại - Đặc Sản Việt");

			String htmlContent = buildPasswordResetConfirmationEmailContent();
			helper.setText(htmlContent, true);

			mailSender.send(message);
			logger.info("Password reset confirmation email sent successfully to {}", toEmail);

		} catch (MessagingException e) {
			logger.error("Failed to send password reset confirmation email to {}", toEmail, e);
		}
	}

	private String buildPasswordResetEmailContent(String resetLink) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
		String currentTime = LocalDateTime.now().format(formatter);

		return """
				<!DOCTYPE html>
				<html>
				<head>
				    <meta charset="UTF-8">
				    <style>
				        body {
				            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
				            line-height: 1.6;
				            color: #333;
				            max-width: 600px;
				            margin: 0 auto;
				            padding: 20px;
				        }
				        .header {
				            background: linear-gradient(135deg, #4ec2b6 0%%, #2e857c 100%%);
				            color: white;
				            padding: 30px;
				            border-radius: 10px 10px 0 0;
				            text-align: center;
				        }
				        .header h1 {
				            margin: 0;
				            font-size: 24px;
				        }
				        .content {
				            background: #f8f9fa;
				            padding: 30px;
				            border-radius: 0 0 10px 10px;
				        }
				        .info-box {
				            background: white;
				            padding: 20px;
				            margin: 15px 0;
				            border-radius: 8px;
				            border-left: 4px solid #4ec2b6;
				        }
				        .button {
				            display: inline-block;
				            padding: 15px 30px;
				            background: #4ec2b6;
				            color: white !important;
				            text-decoration: none;
				            border-radius: 8px;
				            font-weight: bold;
				            margin: 20px 0;
				        }
				        .button:hover {
				            background: #2e857c;
				        }
				        .warning {
				            background: #fff3cd;
				            border-left: 4px solid #ffc107;
				            padding: 15px;
				            margin: 15px 0;
				            border-radius: 5px;
				        }
				        .footer {
				            text-align: center;
				            margin-top: 20px;
				            padding-top: 20px;
				            border-top: 2px solid #e9ecef;
				            color: #6c757d;
				            font-size: 14px;
				        }
				    </style>
				</head>
				<body>
				    <div class="header">
				        <h1>🔐 Đặt Lại Mật Khẩu</h1>
				        <p style="margin: 10px 0 0 0; font-size: 14px;">Đặc Sản Việt</p>
				    </div>

				    <div class="content">
				        <p style="font-size: 16px;">Xin chào,</p>

				        <p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.</p>

				        <div class="info-box">
				            <p style="margin: 0;">Nhấn vào nút bên dưới để đặt lại mật khẩu:</p>
				            <div style="text-align: center;">
				                <a href="%s" class="button">Đặt Lại Mật Khẩu</a>
				            </div>
				            <p style="margin: 10px 0 0 0; font-size: 14px; color: #6c757d;">
				                Hoặc copy link sau vào trình duyệt:<br>
				                <span style="word-break: break-all;">%s</span>
				            </p>
				        </div>

				        <div class="warning">
				            <strong>⚠️ Lưu ý:</strong>
				            <ul style="margin: 10px 0 0 0; padding-left: 20px;">
				                <li>Link này chỉ có hiệu lực trong <strong>1 giờ</strong></li>
				                <li>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này</li>
				                <li>Không chia sẻ link này với bất kỳ ai</li>
				            </ul>
				        </div>

				        <p style="font-size: 14px; color: #6c757d; margin-top: 20px;">
				            Thời gian yêu cầu: %s
				        </p>
				    </div>

				    <div class="footer">
				        <p>Email này được gửi tự động từ hệ thống Đặc Sản Việt</p>
				        <p style="margin: 5px 0;">🌐 <a href="%s" style="color: #4ec2b6;">dacsanviet.com</a></p>
				    </div>
				</body>
				</html>
				""".formatted(resetLink, resetLink, currentTime, frontendUrl);
	}

	private String buildPasswordResetConfirmationEmailContent() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
		String currentTime = LocalDateTime.now().format(formatter);

		return """
				<!DOCTYPE html>
				<html>
				<head>
				    <meta charset="UTF-8">
				    <style>
				        body {
				            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
				            line-height: 1.6;
				            color: #333;
				            max-width: 600px;
				            margin: 0 auto;
				            padding: 20px;
				        }
				        .header {
				            background: linear-gradient(135deg, #4ec2b6 0%%, #2e857c 100%%);
				            color: white;
				            padding: 30px;
				            border-radius: 10px 10px 0 0;
				            text-align: center;
				        }
				        .header h1 {
				            margin: 0;
				            font-size: 24px;
				        }
				        .content {
				            background: #f8f9fa;
				            padding: 30px;
				            border-radius: 0 0 10px 10px;
				        }
				        .success-box {
				            background: #d4edda;
				            border-left: 4px solid #28a745;
				            padding: 20px;
				            margin: 15px 0;
				            border-radius: 8px;
				            text-align: center;
				        }
				        .success-icon {
				            font-size: 48px;
				            margin-bottom: 10px;
				        }
				        .info-box {
				            background: white;
				            padding: 20px;
				            margin: 15px 0;
				            border-radius: 8px;
				            border-left: 4px solid #4ec2b6;
				        }
				        .button {
				            display: inline-block;
				            padding: 15px 30px;
				            background: #4ec2b6;
				            color: white !important;
				            text-decoration: none;
				            border-radius: 8px;
				            font-weight: bold;
				            margin: 20px 0;
				        }
				        .footer {
				            text-align: center;
				            margin-top: 20px;
				            padding-top: 20px;
				            border-top: 2px solid #e9ecef;
				            color: #6c757d;
				            font-size: 14px;
				        }
				    </style>
				</head>
				<body>
				    <div class="header">
				        <h1>✅ Mật Khẩu Đã Được Đặt Lại</h1>
				        <p style="margin: 10px 0 0 0; font-size: 14px;">Đặc Sản Việt</p>
				    </div>

				    <div class="content">
				        <div class="success-box">
				            <div class="success-icon">✅</div>
				            <h2 style="margin: 0; color: #28a745;">Thành Công!</h2>
				            <p style="margin: 10px 0 0 0;">Mật khẩu của bạn đã được đặt lại thành công.</p>
				        </div>

				        <p style="font-size: 16px;">Xin chào,</p>

				        <p>Mật khẩu tài khoản của bạn đã được thay đổi thành công.</p>

				        <div class="info-box">
				            <p><strong>Bạn có thể đăng nhập ngay bây giờ với mật khẩu mới:</strong></p>
				            <div style="text-align: center;">
				                <a href="%s/login" class="button">Đăng Nhập Ngay</a>
				            </div>
				        </div>

				        <div style="background: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 15px 0; border-radius: 5px;">
				            <strong>⚠️ Lưu ý bảo mật:</strong>
				            <ul style="margin: 10px 0 0 0; padding-left: 20px;">
				                <li>Nếu bạn không thực hiện thay đổi này, vui lòng liên hệ ngay với chúng tôi</li>
				                <li>Không chia sẻ mật khẩu với bất kỳ ai</li>
				                <li>Sử dụng mật khẩu mạnh và duy nhất cho tài khoản của bạn</li>
				            </ul>
				        </div>

				        <p style="font-size: 14px; color: #6c757d; margin-top: 20px;">
				            Thời gian thay đổi: %s
				        </p>
				    </div>

				    <div class="footer">
				        <p>Email này được gửi tự động từ hệ thống Đặc Sản Việt</p>
				        <p style="margin: 5px 0;">📧 Hỗ trợ: dacsanviethotro@gmail.com</p>
				        <p style="margin: 5px 0;">🌐 <a href="%s" style="color: #4ec2b6;">dacsanviet.com</a></p>
				    </div>
				</body>
				</html>
				"""
				.formatted(frontendUrl, currentTime, frontendUrl);
	}

	/**
	 * Send order confirmation email to customer
	 */
	public void sendOrderConfirmationEmail(com.dacsanviet.dao.OrderDao order) {
		if (order.getCustomerEmail() == null || order.getCustomerEmail().isEmpty()) {
			logger.warn("Cannot send order confirmation email - no customer email provided for order {}",
					order.getOrderNumber());
			return;
		}

		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			// Try to set from with personal name, fallback to email only if fails
			try {
				helper.setFrom(fromEmail, "Đặc Sản Việt");
			} catch (Exception e) {
				logger.warn("Failed to set personal name, using email only", e);
				helper.setFrom(fromEmail);
			}

			helper.setTo(order.getCustomerEmail());
			helper.setSubject("Xác Nhận Đơn Hàng #" + order.getOrderNumber() + " - Đặc Sản Việt");

			String htmlContent = buildOrderConfirmationEmailContent(order);
			helper.setText(htmlContent, true);

			mailSender.send(message);
			logger.info("Order confirmation email sent successfully to {} for order {}", order.getCustomerEmail(),
					order.getOrderNumber());

		} catch (MessagingException e) {
			logger.error("Failed to send order confirmation email for order {}", order.getOrderNumber(), e);
			// Don't throw exception - order is already created
		} catch (Exception e) {
			logger.error("Unexpected error sending order confirmation email for order {}", order.getOrderNumber(), e);
		}
	}

	private String buildOrderConfirmationEmailContent(com.dacsanviet.dao.OrderDao order) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
		String orderDate = order.getOrderDate() != null ? order.getOrderDate().format(formatter) : "";

		// Build order items HTML
		StringBuilder orderItemsHtml = new StringBuilder();
		if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
			for (com.dacsanviet.dao.OrderItemDao item : order.getOrderItems()) {
				// Use product image from order item snapshot, fallback to default if not
				// available
				String productImageUrl = item.getProductImageUrl();
				if (productImageUrl == null || productImageUrl.isEmpty()) {
					productImageUrl = "https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?w=300&q=80"; // Default
																													// image
				}

				orderItemsHtml.append(String.format(
						"""
								<tr>
									<td style="padding: 15px; border-bottom: 1px solid #e9ecef;">
										<table width="100%%" cellpadding="0" cellspacing="0" border="0">
											<tr>
												<td width="80" style="vertical-align: top;">
													<img src="%s" alt="%s" style="width: 70px; height: 70px; object-fit: cover; border-radius: 8px; border: 1px solid #e9ecef;">
												</td>
												<td style="padding-left: 15px; vertical-align: top;">
													<div style="font-weight: 600; color: #333; margin-bottom: 5px; font-size: 15px;">%s</div>
													<div style="color: #666; font-size: 13px;">Số lượng: %d</div>
													<div style="color: #666; font-size: 13px;">Đơn giá: %s</div>
												</td>
											</tr>
										</table>
									</td>
									<td style="padding: 15px; border-bottom: 1px solid #e9ecef; text-align: right; font-weight: 600; vertical-align: top; white-space: nowrap;">
										%s
									</td>
								</tr>
								""",
						productImageUrl, item.getProductName(), item.getProductName(), item.getQuantity(),
						formatPrice(item.getUnitPrice()),
						formatPrice(item.getUnitPrice().multiply(new java.math.BigDecimal(item.getQuantity())))));
			}
		} else {
			// No items - show message
			orderItemsHtml.append("""
					<tr>
						<td colspan="2" style="padding: 20px; text-align: center; color: #666;">
							Thông tin sản phẩm sẽ được cập nhật sau khi xác nhận đơn hàng
						</td>
					</tr>
					""");
		}

		String paymentMethodText = getPaymentMethodText(order.getPaymentMethod());
		String statusText = getOrderStatusText(order.getStatus());

		return String.format(
				"""
						<!DOCTYPE html>
						<html>
						<head>
							<meta charset="UTF-8">
							<meta name="viewport" content="width=device-width, initial-scale=1.0">
						</head>
						<body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif; background-color: #f5f5f5;">
							<table width="100%%" cellpadding="0" cellspacing="0" border="0" style="background-color: #f5f5f5; padding: 20px 0;">
								<tr>
									<td align="center">
										<table width="600" cellpadding="0" cellspacing="0" border="0" style="background-color: #ffffff; max-width: 600px;">
											<!-- Header -->
											<tr>
												<td style="background: linear-gradient(135deg, #4ec2b6 0%%, #2e857c 100%%); padding: 40px 30px; text-align: center;">
													<img src="https://files.catbox.moe/5uf8r1.png" alt="Đặc Sản Việt" style="max-width: 150px; margin-bottom: 15px;">
													<h1 style="color: #ffffff; font-size: 26px; font-weight: 700; margin: 10px 0;">Đặc Sản Việt</h1>
													<p style="color: #ffffff; font-size: 14px; margin: 0; opacity: 0.95;">Gìn giữ hồn quê, lan toả giá trị Việt</p>
												</td>
											</tr>

											<!-- Success Banner -->
											<tr>
												<td style="background-color: #d4edda; border-left: 4px solid #28a745; padding: 20px 30px;">
													<table width="100%%" cellpadding="0" cellspacing="0" border="0">
														<tr>
															<td style="vertical-align: middle;">
																<div style="display: inline-block; vertical-align: middle;">
																	<div style="font-weight: 700; color: #155724; font-size: 18px; margin-bottom: 5px;">
																		Đặt Hàng Thành Công!
																	</div>
																	<div style="color: #155724; font-size: 14px;">
																		Cảm ơn bạn đã tin tưởng Đặc Sản Việt
																	</div>
																</div>
															</td>
														</tr>
													</table>
												</td>
											</tr>

											<!-- Order Info -->
											<tr>
												<td style="padding: 35px 30px;">
													<h2 style="font-size: 18px; font-weight: 700; color: #333; margin: 0 0 20px; border-bottom: 3px solid #4ec2b6; padding-bottom: 10px;">
														Thông Tin Đơn Hàng
													</h2>

													<table width="100%%" cellpadding="0" cellspacing="0" border="0" style="background-color: #f8f9fa; border-radius: 8px; margin-bottom: 25px;">
														<tr>
															<td style="padding: 15px 20px; border-bottom: 1px solid #e9ecef;">
																<table width="100%%" cellpadding="0" cellspacing="0" border="0">
																	<tr>
																		<td width="140" style="font-weight: 600; color: #4ec2b6; font-size: 14px;">Mã đơn hàng</td>
																		<td style="color: #000; font-size: 16px; font-weight: 700;">#%s</td>
																	</tr>
																</table>
															</td>
														</tr>
														<tr>
															<td style="padding: 15px 20px; border-bottom: 1px solid #e9ecef;">
																<table width="100%%" cellpadding="0" cellspacing="0" border="0">
																	<tr>
																		<td width="140" style="font-weight: 600; color: #4ec2b6; font-size: 14px;">Ngày đặt</td>
																		<td style="color: #333; font-size: 14px;">%s</td>
																	</tr>
																</table>
															</td>
														</tr>
														<tr>
															<td style="padding: 15px 20px; border-bottom: 1px solid #e9ecef;">
																<table width="100%%" cellpadding="0" cellspacing="0" border="0">
																	<tr>
																		<td width="140" style="font-weight: 600; color: #4ec2b6; font-size: 14px;">Trạng thái</td>
																		<td style="color: #333; font-size: 14px; font-weight: 600;">%s</td>
																	</tr>
																</table>
															</td>
														</tr>
														<tr>
															<td style="padding: 15px 20px;">
																<table width="100%%" cellpadding="0" cellspacing="0" border="0">
																	<tr>
																		<td width="140" style="font-weight: 600; color: #4ec2b6; font-size: 14px;">Thanh toán</td>
																		<td style="color: #333; font-size: 14px;">%s</td>
																	</tr>
																</table>
															</td>
														</tr>
													</table>

													<!-- Customer Info -->
													<h2 style="font-size: 18px; font-weight: 700; color: #333; margin: 30px 0 20px; border-bottom: 3px solid #4ec2b6; padding-bottom: 10px;">
														Thông Tin Người Nhận
													</h2>

													<table width="100%%" cellpadding="0" cellspacing="0" border="0" style="background-color: #f8f9fa; border-radius: 8px; margin-bottom: 25px;">
														<tr>
															<td style="padding: 15px 20px; border-bottom: 1px solid #e9ecef;">
																<table width="100%%" cellpadding="0" cellspacing="0" border="0">
																	<tr>
																		<td width="140" style="font-weight: 600; color: #4ec2b6; font-size: 14px;">Họ và tên</td>
																		<td style="color: #333; font-size: 14px; font-weight: 600;">%s</td>
																	</tr>
																</table>
															</td>
														</tr>
														<tr>
															<td style="padding: 15px 20px; border-bottom: 1px solid #e9ecef;">
																<table width="100%%" cellpadding="0" cellspacing="0" border="0">
																	<tr>
																		<td width="140" style="font-weight: 600; color: #4ec2b6; font-size: 14px;">Số điện thoại</td>
																		<td style="color: #333; font-size: 14px; font-weight: 600;">%s</td>
																	</tr>
																</table>
															</td>
														</tr>
														<tr>
															<td style="padding: 15px 20px; border-bottom: 1px solid #e9ecef;">
																<table width="100%%" cellpadding="0" cellspacing="0" border="0">
																	<tr>
																		<td width="140" style="font-weight: 600; color: #4ec2b6; font-size: 14px;">Email</td>
																		<td style="color: #333; font-size: 14px;">%s</td>
																	</tr>
																</table>
															</td>
														</tr>
														<tr>
															<td style="padding: 15px 20px;">
																<table width="100%%" cellpadding="0" cellspacing="0" border="0">
																	<tr>
																		<td width="140" style="font-weight: 600; color: #4ec2b6; font-size: 14px; vertical-align: top;">Địa chỉ</td>
																		<td style="color: #333; font-size: 14px; line-height: 1.6;">%s</td>
																	</tr>
																</table>
															</td>
														</tr>
													</table>

													<!-- Order Items -->
													<h2 style="font-size: 18px; font-weight: 700; color: #333; margin: 30px 0 20px; border-bottom: 3px solid #4ec2b6; padding-bottom: 10px;">
														Chi Tiết Đơn Hàng
													</h2>

													<table width="100%%" cellpadding="0" cellspacing="0" border="0" style="background-color: #fff; border: 1px solid #e9ecef; border-radius: 8px;">
														%s
														<tr>
															<td colspan="2" style="padding: 15px; background-color: #f8f9fa;">
																<table width="100%%" cellpadding="0" cellspacing="0" border="0">
																	<tr>
																		<td style="padding: 5px 0; color: #666; font-size: 14px;">Tạm tính:</td>
																		<td style="padding: 5px 0; text-align: right; font-size: 14px;">%s</td>
																	</tr>
																	<tr>
																		<td style="padding: 5px 0; color: #666; font-size: 14px;">Phí vận chuyển:</td>
																		<td style="padding: 5px 0; text-align: right; font-size: 14px; color: #28a745; font-weight: 600;">%s</td>
																	</tr>
																	<tr>
																		<td style="padding: 15px 0 5px; color: #333; font-size: 16px; font-weight: 700; border-top: 2px solid #dee2e6;">Tổng cộng:</td>
																		<td style="padding: 15px 0 5px; text-align: right; font-size: 20px; font-weight: 700; color: #D2691E; border-top: 2px solid #dee2e6;">%s</td>
																	</tr>
																</table>
															</td>
														</tr>
													</table>

													<!-- Next Steps -->
													<table width="100%%" cellpadding="0" cellspacing="0" border="0" style="margin-top: 30px;">
														<tr>
															<td style="background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 20px; border-radius: 8px;">
																<p style="margin: 0; color: #856404; font-size: 14px; line-height: 1.8;">
																	Chúng tôi sẽ liên hệ với bạn trong vòng 24h để xác nhận đơn hàng. Nếu cần hỗ trợ, vui lòng liên hệ hotline (028) 3896 8641.
																</p>
															</td>
														</tr>
													</table>

													<!-- CTA Button -->
													<table width="100%%" cellpadding="0" cellspacing="0" border="0" style="margin-top: 30px;">
														<tr>
															<td align="center">
																<a href="%s" style="display: inline-block; background: linear-gradient(135deg, #4ec2b6 0%%, #2e857c 100%%); color: #ffffff; text-decoration: none; padding: 15px 40px; border-radius: 8px; font-weight: 700; font-size: 15px;">
																	Tiếp Tục Mua Sắm
																</a>
															</td>
														</tr>
													</table>
												</td>
											</tr>

											<!-- Footer -->
											<tr>
												<td style="background-color: #f8f9fa; padding: 30px; border-top: 1px solid #e9ecef;">
													<table width="100%%" cellpadding="0" cellspacing="0" border="0">
														<tr>
															<td align="center" style="padding-bottom: 15px;">
																<div style="font-weight: 600; color: #333; font-size: 14px; margin-bottom: 10px;">Liên Hệ</div>
																<div style="color: #666; font-size: 13px; line-height: 1.8;">
																	01 Võ Văn Ngân, Phường Thủ Đức, TP Hồ Chí Minh<br>
																	Hotline: (028) 3896 8641<br>
																	<a href="mailto:dacsanviethotro@gmail.com" style="color: #4ec2b6; text-decoration: none;">dacsanviethotro@gmail.com</a>
																</div>
															</td>
														</tr>
														<tr>
															<td align="center" style="padding-top: 15px; border-top: 1px solid #e0e0e0; color: #999; font-size: 12px;">
																© 2025 Đặc Sản Việt. All rights reserved.
															</td>
														</tr>
													</table>
												</td>
											</tr>
										</table>
									</td>
								</tr>
							</table>
						</body>
						</html>
						""",
				order.getOrderNumber(), orderDate, statusText, paymentMethodText, order.getCustomerName(),
				order.getCustomerPhone(), order.getCustomerEmail(), order.getShippingAddressText(),
				orderItemsHtml.toString(), formatPrice(order.getTotalAmount().subtract(order.getShippingFee())),
				order.getShippingFee().compareTo(java.math.BigDecimal.ZERO) == 0 ? "Miễn phí"
						: formatPrice(order.getShippingFee()),
				formatPrice(order.getTotalAmount()), frontendUrl + "/products");
	}

	private String formatPrice(java.math.BigDecimal price) {
		return String.format("%,dđ", price.longValue());
	}

	private String getPaymentMethodText(String method) {
		if (method == null)
			return "Chưa xác định";
		return switch (method) {
		case "COD" -> "Thanh toán khi nhận hàng (COD)";
		case "MOMO" -> "Ví điện tử Momo";
		case "VNPAY" -> "VNPAY";
		case "VIETQR" -> "VietQR";
		case "BANK_TRANSFER" -> "Chuyển khoản ngân hàng";
		default -> method;
		};
	}

	private String getOrderStatusText(com.dacsanviet.model.OrderStatus status) {
		if (status == null)
			return "Chưa xác định";
		return switch (status) {
		case PENDING -> "Chờ xác nhận";
		case CONFIRMED -> "Đã xác nhận";
		case PROCESSING -> "Đang xử lý";
		case SHIPPED -> "Đang giao hàng";
		case DELIVERED -> "Đã giao hàng";
		case CANCELLED -> "Đã hủy";
		};
	}

	/**
	 * Send payment confirmation email
	 */
	public void sendPaymentConfirmationEmail(OrderDao order) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			helper.setFrom(fromEmail);
			helper.setTo(order.getCustomerEmail());
			helper.setSubject("Xác Nhận Thanh Toán - Đơn Hàng " + order.getOrderNumber());

			String htmlContent = buildPaymentConfirmationEmail(order);
			helper.setText(htmlContent, true);

			mailSender.send(message);
			System.out.println("Payment confirmation email sent to: " + order.getCustomerEmail());
		} catch (Exception e) {
			System.err.println("Failed to send payment confirmation email: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private String buildPaymentConfirmationEmail(OrderDao order) {
		StringBuilder html = new StringBuilder();
		html.append("<!DOCTYPE html>");
		html.append("<html><head><meta charset='UTF-8'></head><body style='font-family: Arial, sans-serif;'>");
		html.append("<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>");

		// Header
		html.append(
				"<div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0;'>");
		html.append("<h1 style='margin: 0;'>✅ Thanh Toán Thành Công!</h1>");
		html.append("</div>");

		// Content
		html.append("<div style='background: #f8f9fa; padding: 30px; border-radius: 0 0 10px 10px;'>");
		html.append("<p style='font-size: 16px;'>Xin chào <strong>").append(order.getCustomerName())
				.append("</strong>,</p>");
		html.append("<p>Chúng tôi đã nhận được thanh toán của bạn cho đơn hàng <strong>").append(order.getOrderNumber())
				.append("</strong>.</p>");

		// Payment info
		html.append("<div style='background: white; padding: 20px; border-radius: 8px; margin: 20px 0;'>");
		html.append("<h3 style='color: #28a745; margin-top: 0;'>Thông Tin Thanh Toán</h3>");
		html.append("<table style='width: 100%; border-collapse: collapse;'>");
		html.append("<tr><td style='padding: 8px 0; border-bottom: 1px solid #dee2e6;'><strong>Số tiền:</strong></td>");
		html.append(
				"<td style='padding: 8px 0; border-bottom: 1px solid #dee2e6; text-align: right; color: #28a745; font-size: 18px;'><strong>")
				.append(formatPrice(order.getTotalAmount())).append("</strong></td></tr>");
		html.append(
				"<tr><td style='padding: 8px 0; border-bottom: 1px solid #dee2e6;'><strong>Phương thức:</strong></td>");
		html.append("<td style='padding: 8px 0; border-bottom: 1px solid #dee2e6; text-align: right;'>")
				.append(getPaymentMethodText(order.getPaymentMethod())).append("</td></tr>");
		html.append("<tr><td style='padding: 8px 0;'><strong>Trạng thái:</strong></td>");
		html.append(
				"<td style='padding: 8px 0; text-align: right; color: #28a745;'><strong>Đã thanh toán</strong></td></tr>");
		html.append("</table>");
		html.append("</div>");

		html.append("<p>Đơn hàng của bạn đang được xử lý và sẽ được giao sớm nhất có thể.</p>");
		html.append("<p>Cảm ơn bạn đã mua hàng tại <strong>Đặc Sản Việt</strong>!</p>");

		html.append("</div>");
		html.append("</div>");
		html.append("</body></html>");

		return html.toString();
	}

}
