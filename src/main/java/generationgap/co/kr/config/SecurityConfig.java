package generationgap.co.kr.config;

import generationgap.co.kr.mapper.user.UserMapper;
import generationgap.co.kr.service.user.CustomOAuth2UserService;
import generationgap.co.kr.service.user.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService; // OAuth2UserService 임포트
import org.springframework.security.oauth2.core.user.OAuth2User; // OAuth2User 임포트
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // CustomUserDetailsService는 @Autowired 대신 생성자 주입을 권장합니다.
    // 하지만 현재 코드 구조를 유지하며 수정하겠습니다.
    // private final CustomUserDetailsService customUserDetailsService;
    // public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
    //     this.customUserDetailsService = customUserDetailsService;
    // }

    // CustomUserDetailsService를 필드로 주입받습니다. (현재 코드를 유지하는 경우)
    // Spring Security 6+에서는 AuthenticationManagerBuilder를 SecurityFilterChain 내에서 직접 사용하지 않고,
    // DaoAuthenticationProvider 등을 빈으로 등록하는 방식이 권장됩니다.
    // 여기서는 기존 AuthenticationManager 설정 방식을 최대한 유지하며 수정합니다.
    private final CustomUserDetailsService customUserDetailsService; // final로 변경하여 RequiredArgsConstructor 사용 권장
    private final UserMapper userMapper; // CustomOAuth2UserService 생성을 위해 주입

    // 생성자 주입으로 CustomUserDetailsService와 UserMapper를 주입받습니다.
    // @Autowired 대신 이 방식을 사용하는 것이 Spring에서 권장하는 DI 방식입니다.
    public SecurityConfig(CustomUserDetailsService customUserDetailsService, UserMapper userMapper) {
        this.customUserDetailsService = customUserDetailsService;
        this.userMapper = userMapper;
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationManager를 빈으로 등록
    // Spring Security 6.x에서는 이 방식이 Deprecated될 수 있으나,
    // HttpSecurity를 통한 build는 아직 사용 가능합니다.
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
                .userDetailsService(customUserDetailsService)
                .passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }


    // SecurityFilterChain 빈 등록
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            // CustomOAuth2UserService 빈을 직접 주입받습니다.
            // Spring이 이미 생성한 CustomOAuth2UserService 빈을 여기에 주입해 줍니다.
            OAuth2UserService<org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest, OAuth2User> customOAuth2UserService)
            throws Exception {

        http.csrf(csrf -> csrf.disable()) // CSRF 보호 비활성화 (개발 단계에서만 사용)
                .authorizeHttpRequests(authorize -> authorize
                        // permitAll() 경로들은 가장 먼저 선언하는 것이 좋습니다.
                        .requestMatchers(
                                // 정적 리소스 및 기본 공개 경로
                                new AntPathRequestMatcher("/"), // 루트 경로
                                new AntPathRequestMatcher("/css/**"),
                                new AntPathRequestMatcher("/js/**"),
                                new AntPathRequestMatcher("/images/**"),
                                new AntPathRequestMatcher("/error"), // 에러 페이지
                                new AntPathRequestMatcher("/favicon.ico"),
                                new AntPathRequestMatcher("/**"),          // 모든 요청 일단 허용 (개발 초기
                                // 사용자 관련 공개 경로 (회원가입, 로그인, 중복 확인, 비밀번호 재설정)
                                new AntPathRequestMatcher("/user/signup"),
                                new AntPathRequestMatcher("/user/check/**"), // 중복 확인 API (구글 간편로그인 전에 실행될 수 있음)
                                new AntPathRequestMatcher("/user/login"),
                                new AntPathRequestMatcher("/user/find-id"),
                                new AntPathRequestMatcher("/user/forgot-password"),
                                new AntPathRequestMatcher("/user/forgot-password-request"),
                                new AntPathRequestMatcher("/user/reset-password"),
                                new AntPathRequestMatcher("/user/reset-password-process"),
                                new AntPathRequestMatcher("/user/reset_password_error"),
                                new AntPathRequestMatcher("/main") // 메인 페이지 (로그인 없이 접근 가능)
                        ).permitAll() // 위의 경로들은 인증 없이 접근을 허용합니다.

                        // 나머지 인증 필요한 경로들
                        .anyRequest().authenticated() // 위의 permitAll() 경로를 제외한 모든 요청은 인증을 요구합니다.
                )
                .formLogin(formLogin -> formLogin // 폼 로그인 설정
                        .loginPage("/user/login")
                        .loginProcessingUrl("/login-process")
                        .defaultSuccessUrl("/main", true)
                        .usernameParameter("userId")
                        .passwordParameter("password")
                        .failureUrl("/user/login?error")
                        .permitAll()
                )
                .oauth2Login(oauth2Login -> oauth2Login
                        .loginPage("/user/login") // OAuth2 로그인 시작 페이지를 사용자 정의 로그인 페이지로 지정
                        .defaultSuccessUrl("/main", true) // OAuth2 로그인 성공 후 리디렉션
                        // .failureUrl("/user/login?oauth2error") // OAuth2 로그인 실패 시 (필요하다면)
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService) // 주입받은 CustomOAuth2UserService 빈 사용
                        )
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/user/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        return http.build();
    }

    // CustomOAuth2UserService 빈 등록
    // 이 메서드에서는 필드로 주입받은 userMapper를 사용합니다.
    @Bean
    public CustomOAuth2UserService customOAuth2UserService() { // 인자 제거
        // 생성자 주입된 userMapper를 사용하여 인스턴스 생성
        return new CustomOAuth2UserService(userMapper);
    }
}