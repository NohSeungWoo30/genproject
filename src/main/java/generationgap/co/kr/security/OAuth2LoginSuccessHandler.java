package generationgap.co.kr.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        log.info("로그인 성공! 🎉");

        // 인증 객체에서 CustomUserDetails 꺼내기
        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails userDetails) {
            log.info("OAuth2 로그인 사용자 ID: {}", userDetails.getUsername());

            // 여기서 추가 작업 가능! (ex. 세션 저장, 쿠키 추가, 리다이렉트 등)
            request.getSession().setAttribute("user", userDetails.getUserDTO());
        } else {
            log.warn("인증 객체가 CustomUserDetails가 아님: {}", principal);
        }

        // 로그인 후 메인 페이지로 리다이렉트
        response.sendRedirect("/main");
    }
}
