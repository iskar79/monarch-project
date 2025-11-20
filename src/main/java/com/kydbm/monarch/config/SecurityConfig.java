package com.kydbm.monarch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import org.springframework.security.crypto.password.PasswordEncoder;

/** 
 * Spring Security 설정을 담당하는 클래스.
 * 웹 애플리케이션의 인증(Authentication)과 인가(Authorization) 규칙을 정의합니다.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        http
                // 1. 사용할 인증 관리자(AuthenticationManager)를 명시적으로 설정합니다.
                .authenticationManager(authenticationManager)
                // 2. CORS(Cross-Origin Resource Sharing) 설정을 적용합니다. React 앱からの API 요청을 허용하기 위해 필요합니다.
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 3. CSRF(Cross-Site Request Forgery) 보호 기능을 비활성화합니다. REST API 서버에서는 보통 불필요합니다.
                .csrf(csrf -> csrf.disable())
                // 4. HTTP 요청에 대한 접근 권한을 설정합니다.
                .authorizeHttpRequests(auth -> auth
                        // "/api/login" 등 특정 경로는 인증 없이 누구나 접근할 수 있도록 허용합니다.
                        .requestMatchers("/api/login", "/api/logout", "/api/hello", "/").permitAll()
                        // 그 외 "/api/**"로 시작하는 모든 경로는 반드시 인증(로그인)된 사용자만 접근 가능하도록 설정합니다.
                        .requestMatchers("/api/**").authenticated()
                        // 위에서 지정하지 않은 나머지 모든 요청(예: React 정적 파일)은 허용합니다.
                        .anyRequest().permitAll()
                )
                // 5. 폼 기반 로그인 설정을 정의합니다.
                .formLogin(form -> form
                        // 프론트엔드에서 로그인 데이터를 전송할 URL을 지정합니다. 이 요청은 Spring Security가 가로채서 처리합니다.
                        .loginProcessingUrl("/api/login")
                        // 로그인 성공 시, 별도 페이지 이동 없이 HTTP 200(OK) 상태 코드만 응답합니다.
                        .successHandler((request, response, authentication) -> response.setStatus(HttpStatus.OK.value()))
                        // 로그인 실패 시, 별도 페이지 이동 없이 HTTP 401(Unauthorized) 상태 코드만 응답합니다.
                        .failureHandler((request, response, exception) -> response.setStatus(HttpStatus.UNAUTHORIZED.value()))
                )
                // 6. 로그아웃 설정을 정의합니다.
                .logout(logout -> logout
                        .logoutUrl("/api/logout") // 로그아웃 처리 URL
                        // 로그아웃 성공 시, HTTP 200(OK) 상태 코드를 응답합니다.
                        .logoutSuccessHandler((request, response, authentication) -> response.setStatus(HttpStatus.OK.value()))
                )
                // 7. 인증 예외 처리를 정의합니다.
                .exceptionHandling(e -> e
                        // 인증되지 않은 사용자가 보호된 리소스에 접근하면, HTTP 401(Unauthorized) 상태 코드를 응답합니다.
                        .authenticationEntryPoint((request, response, authException) -> response.setStatus(HttpStatus.UNAUTHORIZED.value()))
                );
        return http.build();
    }

    /**
     * 실제 인증을 처리하는 `AuthenticationManager`를 생성하여 Spring 컨테이너에 Bean으로 등록합니다.
     * 여러 인증 로직(Provider)을 관리할 수 있습니다.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        // 1. 'khma' 사용자를 위한 임시 인증 공급자
        TemporaryAuthenticationProvider temporaryProvider = new TemporaryAuthenticationProvider(userDetailsService);
        
        // 2. DB 기반의 표준 인증 공급자
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        
        // ProviderManager는 등록된 순서대로 인증을 시도합니다. (temporaryProvider -> daoAuthenticationProvider)
        return new ProviderManager(temporaryProvider, daoAuthenticationProvider);
    }

    /**
     * 비밀번호를 안전하게 암호화하고 검증하기 위한 `PasswordEncoder`를 Bean으로 등록합니다.
     * BCrypt는 현재 널리 사용되는 강력한 해시 알고리즘입니다.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }

    /**
     * CORS(Cross-Origin Resource Sharing) 정책을 상세하게 설정합니다.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://127.0.0.1:5173", "http://localhost:5173")); // React 개발 서버의 주소를 허용합니다.
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}