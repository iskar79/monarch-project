package com.kydbm.monarch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CORS 설정
                .cors(cors -> {})
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
                        .loginProcessingUrl("/api/login") // 로그인 처리 URL
                        .successHandler((request, response, authentication) -> response.setStatus(HttpStatus.OK.value()))
                        .failureHandler((request, response, exception) -> response.setStatus(HttpStatus.UNAUTHORIZED.value()))
                )
                .logout(logout -> logout
                        .logoutUrl("/api/logout") // 로그아웃 처리 URL
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK))
                )
                // 인증 예외 처리
                .exceptionHandling(e -> e
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                new AntPathRequestMatcher("/api/**")
                        )
                );
        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        // BCryptPasswordEncoder Bean 등록
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(BCryptPasswordEncoder passwordEncoder) {
        // 인메모리 사용자 생성
        UserDetails user = User.builder()
                .username("user")
                .password(passwordEncoder.encode("password")) // 비밀번호를 암호화하여 저장
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }
}