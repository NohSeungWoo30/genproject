package generationgap.co.kr.controller;

import generationgap.co.kr.dto.ConfirmRequest;
import generationgap.co.kr.service.payment.PaymentService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import generationgap.co.kr.security.CustomUserDetails;
import java.math.BigDecimal;

@Controller
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/payment/result")
    public String paymentSuccess(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam String amount,
            @RequestParam(required = false) String orderName, // <<-- orderName 파라미터를 추가로 받습니다.
            Model model,
            Authentication authentication) {

        try {
            Long userIdx = null;
            if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                userIdx = userDetails.getUserIdx();
            }

            if (userIdx == null) {
                model.addAttribute("message", "결제 처리 중 사용자 정보를 찾을 수 없습니다. 다시 로그인 해주세요.");
                model.addAttribute("errorMessage", "USER_NOT_FOUND");
                model.addAttribute("orderId", orderId);
                return "payment_fail";
            }

            ConfirmRequest confirmRequest = new ConfirmRequest();
            confirmRequest.setPaymentKey(paymentKey);
            confirmRequest.setOrderId(orderId);
            confirmRequest.setAmount(new BigDecimal(amount));

            // 프론트에서 전달받은 orderName (상품 이름)을 ConfirmRequest에 설정
            confirmRequest.setProductName(orderName != null ? orderName : "알 수 없는 상품");

            paymentService.confirmPayment(confirmRequest, userIdx);

            System.out.println("결제 성공!");
            System.out.println("paymentKey: " + paymentKey);
            System.out.println("orderId: " + orderId);
            System.out.println("amount: " + amount);
            System.out.println("productName (저장될 이름): " + confirmRequest.getProductName());

            model.addAttribute("message", "결제가 성공적으로 완료되었습니다!");
            model.addAttribute("paymentKey", paymentKey);
            model.addAttribute("orderId", orderId);
            model.addAttribute("amount", amount);
            model.addAttribute("productName", confirmRequest.getProductName());
            return "payment_success";

        } catch (NumberFormatException e) {
            System.err.println("결제 금액(amount) 파싱 오류: " + e.getMessage());
            model.addAttribute("message", "결제 금액 형식이 올바르지 않습니다.");
            model.addAttribute("errorMessage", "INVALID_AMOUNT_FORMAT");
            model.addAttribute("orderId", orderId);
            return "payment_fail";
        } catch (RuntimeException e) {
            System.err.println("결제 승인 서비스 오류: " + e.getMessage());
            model.addAttribute("message", "결제 승인 중 오류가 발생했습니다.");
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("orderId", orderId);
            return "payment_fail";
        } catch (Exception e) {
            System.err.println("예상치 못한 결제 처리 오류: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("message", "결제 처리 중 알 수 없는 오류가 발생했습니다.");
            model.addAttribute("errorMessage", "UNKNOWN_ERROR");
            model.addAttribute("orderId", orderId);
            return "payment_fail";
        }
    }

    @GetMapping("/payment/fail")
    public String paymentFail(
            @RequestParam String code,
            @RequestParam String message,
            @RequestParam String orderId,
            @RequestParam(required = false) String orderName, // 실패 시에도 상품 이름 받을 수 있도록 추가 (옵션)
            Model model) {
        System.out.println("결제 실패!");
        System.out.println("code: " + code);
        System.out.println("message: " + message);
        System.out.println("orderId: " + orderId);
        System.out.println("productName (실패 시): " + (orderName != null ? orderName : "N/A")); // 로그 추가

        model.addAttribute("message", "결제가 실패했습니다.");
        model.addAttribute("code", code);
        model.addAttribute("errorMessage", message);
        model.addAttribute("orderId", orderId);
        // 실패 페이지에 상품 이름도 전달 (원한다면)
        model.addAttribute("productName", orderName != null ? orderName : "알 수 없는 상품");
        return "payment_fail";
    }
}
