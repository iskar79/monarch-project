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

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        http
                .authenticationManager(authenticationManager) // AuthenticationManager를 명시적으로 설정
                // CORS 설정 적용
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // CSRF 보호 비활성화
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 인증 없이 접근 가능한 경로 설정
                        .requestMatchers("/api/login", "/api/logout", "/api/hello", "/").permitAll()
                        // 그 외 /api/** 경로는 인증된 사용자만 접근 가능
                        .requestMatchers("/api/**").authenticated()
                        // 그 외 모든 요청은 허용 (React 앱 리소스 등)
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginProcessingUrl("/api/login")
                        .successHandler((request, response, authentication) -> response.setStatus(HttpStatus.OK.value()))
                        .failureHandler((request, response, exception) -> response.setStatus(HttpStatus.UNAUTHORIZED.value()))
                )
                .logout(logout -> logout
                        .logoutUrl("/api/logout") // 로그아웃 처리 URL
                        .logoutSuccessHandler((request, response, authentication) -> response.setStatus(HttpStatus.OK.value()))
                )
                // 인증 예외 처리
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((request, response, authException) -> response.setStatus(HttpStatus.UNAUTHORIZED.value()))
                );
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        // 1. 'khma' 사용자를 위한 임시 인증 공급자
        TemporaryAuthenticationProvider temporaryProvider = new TemporaryAuthenticationProvider(userDetailsService);
        
        // 2. 다른 모든 사용자를 위한 표준 인증 공급자
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        
        // ProviderManager는 등록된 순서대로 인증을 시도합니다.
        return new ProviderManager(temporaryProvider, daoAuthenticationProvider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCryptPasswordEncoder Bean 등록
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://127.0.0.1:5173", "http://localhost:5173")); // React 앱 주소
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}