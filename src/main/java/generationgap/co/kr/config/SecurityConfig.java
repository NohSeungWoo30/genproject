package generationgap.co.kr.config;

import generationgap.co.kr.service.user.CustomUserDetailsService; // CustomUserDetailsService 임포트 추가
import org.springframework.beans.factory.annotation.Autowired; // Autowired 임포트 추가
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager; // AuthenticationManager 임포트 추가
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder; // AuthenticationManagerBuilder 임포트 추가
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher; // 추가

@Configuration // 이 클래스가 Spring 설정 클래스임을 나타냅니다.
@EnableWebSecurity // Spring Security 활성화 및 웹 보안 지원을 가능하게 합니다
public class SecurityConfig {

    // CustomUserDetailsService를 주입받아 사용합니다.
    @Autowired
    private CustomUserDetailsService customUserDetailsService; // *** 이 부분 추가 ***

    @Bean // 이 메서드가 반환하는 객체를 Spring 빈으로 등록합니다.
    public PasswordEncoder passwordEncoder() {
        // BCrypt 알고리즘을 사용하여 비밀번호를 해싱하는 인코더를 반환합니다.
        // BCrypt는 솔트(salt)를 내부적으로 처리하여 보안성을 높여줍니다.
        return new BCryptPasswordEncoder();
    }

    // *** 이 부분 추가: AuthenticationManager를 빈으로 등록하여 UserDetailsService와 PasswordEncoder를 설정합니다. ***
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
                .userDetailsService(customUserDetailsService) // 사용할 UserDetailsService 지정
                .passwordEncoder(passwordEncoder());          // 사용할 PasswordEncoder 지정
        return authenticationManagerBuilder.build();
    }

    // TODO: 나중에 로그인 및 보안 설정을 추가할 때 여기에 WebSecurityConfigurerAdapter를 구현할 수 있습니다.
    // Spring Security 5.7+ 및 Spring Boot 2.7+ 부터는 SecurityFilterChain 빈을 사용합니다.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()) // CSRF 보호 비활성화 (개발 단계에서만 사용, 실제 서비스에서는 보안 고려하여 활성화)
                                                                // POST 요청 시 403 Forbidden 오류 방지를 위해 임시 비활성화
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                                new AntPathRequestMatcher("/user/signup"), // 회원가입 페이지 접근 허용
                                new AntPathRequestMatcher("/user/login"),   // *** 로그인 페이지 허용
                                new AntPathRequestMatcher("/css/**"),     // CSS 파일 접근 허용 (정적 리소스)
                                new AntPathRequestMatcher("/js/**"),      // JavaScript 파일 접근 허용
                                new AntPathRequestMatcher("/images/**"),  // 이미지 파일 접근 허용
                                //new AntPathRequestMatcher("/**"),          // 모든 요청 일단 허용 (개발 초기 단계)
                                // 나중에 로그인 기능 구현 후 .authenticated()로 변경
                                new AntPathRequestMatcher("/main"),
                                // *** 이 부분 추가: 비밀번호 재설정 관련 URL 허용 ***
                                new AntPathRequestMatcher("/user/find-id"),
                                new AntPathRequestMatcher("/user/forgot-password"),
                                new AntPathRequestMatcher("/user/forgot-password-request"),
                                new AntPathRequestMatcher("/user/reset-password"),
                                new AntPathRequestMatcher("/user/reset-password-process"),
                                new AntPathRequestMatcher("/user/reset_password_error") // 에러 페이지도 허용
                ).permitAll() // 위의 경로들은 인증 없이 접근을 허용합니다.

                // 프로필 관련 페이지는 인증된 사용자만 접근 허용
                .requestMatchers(new AntPathRequestMatcher("/user/profile/**")).authenticated()
                .anyRequest().authenticated() // 그 외의 모든 요청은 인증을 요구합니다. (나중에 사용할 부분)
            )
            .formLogin(formLogin -> formLogin // 폼 로그인 설정
                    .loginPage("/user/login") // 나중에 사용자 정의 로그인 페이지 경로 설정
                    .loginProcessingUrl("/login-process") // 로그인 폼 제출(POST)을 처리할 URL (실제 컨트롤러는 필요 없음)
                    .defaultSuccessUrl("/main", true) // 로그인 성공 시 이동할 기본 페이지
                    .usernameParameter("userId") // 로그인 폼에서 사용자 ID를 받을 input name (기본값: username)
                    .passwordParameter("password") // 로그인 폼에서 비밀번호를 받을 input name (기본값: password)
                    .failureUrl("/user/login?error") // 로그인 실패 시 이동할 페이지
                .permitAll() // 로그인 관련 URL(loginPage, loginProcessingUrl 등)은 모두 허용
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout")) // 로그아웃 URL
                .logoutSuccessUrl("/user/login?logout") // 로그아웃 성공 시 이동할 페이지 (logout 파라미터 추가)
                .invalidateHttpSession(true) // 세션 무효화
                .deleteCookies("JSESSIONID") // 쿠키 삭제 (세션 트래킹용)
                .permitAll() // 로그아웃 관련 URL도 모두 허용
            );

        return http.build();

}
}
