package generationgap.co.kr.config;

import generationgap.co.kr.security.OAuth2LoginSuccessHandler;
import generationgap.co.kr.service.oauth.CustomOAuth2UserService;
import generationgap.co.kr.service.user.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomUserDetailsService customUserDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;


    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
                .userDetailsService(customUserDetailsService);
        // .passwordEncoder(passwordEncoder()); // 비밀번호 인코더 설정이 필요하면 주석 해제하세요.
        return authenticationManagerBuilder.build();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http)
            throws Exception {

        http.csrf(csrf -> csrf.disable()) // CSRF 보호 비활성화 (개발 단계에서만 사용 권장)
                .authorizeHttpRequests(authorize -> authorize
                        // 정적 리소스 및 기본 공개 경로
                        .requestMatchers(
                                new AntPathRequestMatcher("/"),
                                new AntPathRequestMatcher("/css/**"),
                                new AntPathRequestMatcher("/js/**"),
                                new AntPathRequestMatcher("/images/**"),
                                new AntPathRequestMatcher("/error"),
                                new AntPathRequestMatcher("/favicon.ico"),
                                new AntPathRequestMatcher("/main"),
                                // 사용자 관련 공개 경로
                                new AntPathRequestMatcher("/user/profile"),
                                new AntPathRequestMatcher("/user/signup"),
                                new AntPathRequestMatcher("/user/check/**"),
                                new AntPathRequestMatcher("/user/login"),
                                new AntPathRequestMatcher("/user/find-id"),
                                new AntPathRequestMatcher("/user/forgot-password"),
                                new AntPathRequestMatcher("/user/forgot-password-request"),
                                new AntPathRequestMatcher("/user/reset-password"),
                                new AntPathRequestMatcher("/user/reset-password-process"),
                                new AntPathRequestMatcher("/user/reset_password_error")
                        ).permitAll() // 위의 경로들은 인증 없이 접근을 허용합니다.

                        // ⭐ OAuth2 로그인 관련 경로를 인증 없이 허용합니다.
                        // 일반적으로 '/oauth2/authorization/*' 경로와 같은 OAuth2 콜백 URL이 여기에 포함됩니다.
                        // Spring Security가 자동으로 처리하는 경로를 명시적으로 허용하여 간섭을 줄입니다.
                        .requestMatchers(new AntPathRequestMatcher("/oauth2/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/login/oauth2/code/**")).permitAll()


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
                .oauth2Login(oauth2Login -> {
                    oauth2Login
                            .loginPage("/user/login")
                            .userInfoEndpoint(userInfo -> {
                                userInfo.userService(customOAuth2UserService);
                            })
                            .successHandler(oAuth2LoginSuccessHandler);
                })
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/user/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .userDetailsService(customUserDetailsService);

        return http.build();
    }

}