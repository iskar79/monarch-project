package com.kydbm.monarch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * `application.properties` 파일의 JWT 관련 설정값을 자바 객체로 바인딩합니다.
 */
@Configuration
/**
 * `application.properties` 파일에서 'jwt'로 시작하는 속성들을 이 클래스의 필드에 매핑합니다.
 * 예: jwt.secret=your-key -> secret 필드에 바인딩
 */
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * JWT 토큰 서명 및 검증에 사용될 비밀 키.
     */
    private String secret;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
