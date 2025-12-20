package com.dacsanviet.controller;

import com.dacsanviet.service.OrderService;
import com.dacsanviet.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private OrderService orderService;

    @Value("${vietqr.bank-id}")
    private String vietqrBankId;

    @Value("${vietqr.account-no}")
    private String vietqrAccountNo;

    @Value("${vietqr.account-name}")
    private String vietqrAccountName;

    @Value("${vietqr.template}")
    private String vietqrTemplate;

    @Value("${momo.qr-image}")
    private String momoQrImage;

    /**
     * Create VNPAY payment
     */
    @GetMapping("/vnpay/create")
    public String createVNPayPayment(
            @RequestParam Long orderId,
            @RequestParam Long amount,
            @RequestParam String orderInfo,
            HttpServletRequest request) {
        
        String ipAddress = getClientIp(request);
        String paymentUrl = vnPayService.createPaymentUrl(
                amount,
                orderInfo,
                String.valueOf(orderId),
                ipAddress
        );
        
        return "redirect:" + paymentUrl;
    }

    /**
     * VNPAY return URL
     */
    @GetMapping("/vnpay-return")
    public String vnpayReturn(@RequestParam Map<String, String> params, Model model) {
        boolean isValid = vnPayService.verifyPaymentResponse(params);
        
        String responseCode = params.get("vnp_ResponseCode");
        String txnRef = params.get("vnp_TxnRef");
        String amount = params.get("vnp_Amount");
        
        if (isValid && "00".equals(responseCode)) {
            // Payment successful
            model.addAttribute("success", true);
            model.addAttribute("message", "Thanh toán thành công!");
            model.addAttribute("orderId", txnRef);
            model.addAttribute("amount", Long.parseLong(amount) / 100);
            
            // Update order status to PAID
            try {
                Long orderIdLong = Long.parseLong(txnRef);
                // You can add a method in OrderService to update payment status
                // orderService.updatePaymentStatus(orderIdLong, "PAID", "VNPAY");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Payment failed
            model.addAttribute("success", false);
            model.addAttribute("message", "Thanh toán thất bại!");
            model.addAttribute("orderId", txnRef);
        }
        
        return "payment/result";
    }

    /**
     * Show VietQR payment page
     */
    @GetMapping("/vietqr")
    public String showVietQR(
            @RequestParam Long orderId,
            @RequestParam Long amount,
            @RequestParam String orderInfo,
            Model model) {
        
        // Get order to retrieve orderNumber
        try {
            var order = orderService.getOrderById(orderId);
            String orderNumber = order.getOrderNumber();
            
            // Generate VietQR URL
            String description = "DH" + orderId;
            String vietqrUrl = String.format(
                    "https://img.vietqr.io/image/%s-%s-%s.jpg?amount=%d&addInfo=%s&accountName=%s",
                    vietqrBankId,
                    vietqrAccountNo,
                    vietqrTemplate,
                    amount,
                    description,
                    vietqrAccountName
            );
            
            model.addAttribute("qrUrl", vietqrUrl);
            model.addAttribute("orderId", orderId);
            model.addAttribute("orderNumber", orderNumber);
            model.addAttribute("amount", amount);
            model.addAttribute("orderInfo", orderInfo);
            model.addAttribute("bankName", "Vietcombank");
            model.addAttribute("accountNo", vietqrAccountNo);
            model.addAttribute("accountName", vietqrAccountName);
            model.addAttribute("description", description);
            
            return "payment/vietqr";
        } catch (Exception e) {
            model.addAttribute("error", "Không tìm thấy đơn hàng");
            return "redirect:/";
        }
    }

    /**
     * Show Momo payment page
     */
    @GetMapping("/momo")
    public String showMomo(
            @RequestParam Long orderId,
            @RequestParam Long amount,
            @RequestParam String orderInfo,
            Model model) {
        
        // Get order to retrieve orderNumber
        try {
            var order = orderService.getOrderById(orderId);
            String orderNumber = order.getOrderNumber();
            
            String description = "DH" + orderId;
            
            model.addAttribute("qrImage", momoQrImage);
            model.addAttribute("orderId", orderId);
            model.addAttribute("orderNumber", orderNumber);
            model.addAttribute("amount", amount);
            model.addAttribute("orderInfo", orderInfo);
            model.addAttribute("description", description);
            
            return "payment/momo";
        } catch (Exception e) {
            model.addAttribute("error", "Không tìm thấy đơn hàng");
            return "redirect:/";
        }
    }

    /**
     * Get client IP address
     */
    private String getClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
}
