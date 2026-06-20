package com.dacsanviet.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        String requestUri = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        
        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            model.addAttribute("status", statusCode);
            
            // Nếu lỗi 403 Forbidden tại trang đăng nhập (thường do timeout CSRF)
            if (statusCode == HttpStatus.FORBIDDEN.value()) {
                if ("/login".equals(requestUri) || requestUri == null) {
                    return "redirect:/login?error=timeout";
                }
                // Nếu lỗi 403 khi đang logout (CSRF timeout), cứ đẩy về trang chủ hoặc trang đăng nhập
                if ("/logout".equals(requestUri)) {
                    return "redirect:/login";
                }
                model.addAttribute("errorTitle", "403 - Không Có Quyền Truy Cập");
                model.addAttribute("errorMessage", "Xin lỗi, bạn không có quyền truy cập vào trang này hoặc phiên đăng nhập của bạn đã hết hạn.");
            }
            // Xử lý các lỗi khác (404, 500)
            else if (statusCode == HttpStatus.NOT_FOUND.value()) {
                model.addAttribute("errorTitle", "404 - Không Tìm Thấy Trang");
                model.addAttribute("errorMessage", "Xin lỗi, trang bạn đang tìm kiếm không tồn tại hoặc đã bị di dời.");
            }
            else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                model.addAttribute("errorTitle", "500 - Lỗi Máy Chủ");
                model.addAttribute("errorMessage", "Đã xảy ra lỗi hệ thống. Chúng tôi đang nỗ lực khắc phục sự cố này.");
            }
            else {
                model.addAttribute("errorTitle", "Lỗi " + statusCode);
                model.addAttribute("errorMessage", "Đã xảy ra một lỗi không xác định. Vui lòng thử lại sau.");
            }
        } else {
            model.addAttribute("errorTitle", "Lỗi Hệ Thống");
            model.addAttribute("errorMessage", "Đã xảy ra một lỗi. Vui lòng liên hệ quản trị viên.");
        }
        
        return "error/404"; // Render trang lỗi tùy chỉnh nếu có, hiện tại cứ map tới error/404 hoặc tuỳ chọn
    }
}
