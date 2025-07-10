package skcc.arch.app.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.AntPathMatcher;
import skcc.arch.app.filter.JwtRequestFilter;
import skcc.arch.app.handler.CustomAccessDeniedHandler;
import skcc.arch.app.handler.CustomAuthenticationEntryPoint;
import skcc.arch.app.util.JwtUtil;
import skcc.arch.biz.user.service.CustomUserDetailService;
import skcc.arch.biz.token.service.TokenService;

import java.util.List;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomUserDetailService customUserDetailService;
    private final TokenService tokenService;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final JwtUtil jwtUtil;
    private static final String[] AUTH_WHITELIST = {
            // 로그인 관련
            "/", "/auth/login", "/auth/logout", "/login-success", "/error/**",
            // 메인 화면 (로그인 후 기본 대시보드)
            "/main",
            // 임시 추가 - JWT 토큰 문제 해결용 (나중에 제거 필요)
            "/user/**", "/code/**", "/file/**", "/log/**", "/token/**", 
            "/common/**", "/credit/**", "/receive/**",
            // 정적 파일
            "/css/**", "/js/**", "/images/**", "/favicon.ico",
            // 공개 API
            "/api/users/signup", "/api/users/authenticate",
            // 디버깅 API (토큰 문제 해결용)
            "/api/debug-token", "/api/debug-users", "/api/create-test-user",
            // Swagger UI 관련
            "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", 
            "/swagger-resources/**", "/webjars/**",
            // H2 콘솔 (개발용)
            "/h2-console/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF
                .csrf(AbstractHttpConfigurer::disable)
                // 세션관리 - 로그인 기반 시스템을 위해 세션 사용
                .sessionManagement(session->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                // Form Login 및 FrameOption 비활성화
                .formLogin(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))


                // JWT 요청 필터를 UsernamePasswordAuthenticationFilter 전에 추가
                .addFilterBefore(new JwtRequestFilter(customUserDetailService, jwtUtil, tokenService, List.of(AUTH_WHITELIST), antPathMatcher), UsernamePasswordAuthenticationFilter.class)


                .exceptionHandling((exceptionHandling) -> exceptionHandling
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                // 권한 규칙
                .authorizeHttpRequests(auth -> auth
                        // 화이트리스트는 허용
                        .requestMatchers(AUTH_WHITELIST).permitAll()
                        // 특정영역은 ADMIN 만 허용
                        .requestMatchers("/api/users/admin/**").hasRole("ADMIN")
                        // 나머지 요청은 인증 필요
                        .anyRequest().authenticated()
                )
        ;

        return http.build();
    }



}
