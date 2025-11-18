package com.kydbm.monarch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT 관련 설정을 application.properties에서 읽어오는 클래스
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}