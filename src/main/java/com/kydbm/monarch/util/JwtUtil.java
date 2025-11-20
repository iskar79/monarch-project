package com.kydbm.monarch.util;

import com.kydbm.monarch.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT(JSON Web Token) 생성, 검증 등 JWT 관련 핵심 기능을 제공하는 유틸리티 클래스.
 */
@Component
public class JwtUtil {

    private final JwtProperties jwtProperties;

    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    /**
     * JWT 토큰에서 사용자 아이디(username)를 추출합니다.
     * @param token JWT 토큰
     * @return 사용자 아이디
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * JWT 토큰에서 만료일자를 추출합니다.
     * @param token JWT 토큰
     * @return 만료일자
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * UserDetails를 기반으로 JWT 토큰을 생성합니다.
     * @param userDetails 사용자 정보
     * @return 생성된 JWT 토큰
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * 추가적인 클레임(extraClaims)과 UserDetails를 기반으로 JWT 토큰을 생성합니다.
     * @param extraClaims 토큰에 추가할 정보
     * @param userDetails 사용자 정보
     * @return 생성된 JWT 토큰
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims().add(extraClaims).and()
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 24시간 유효
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * JWT 토큰이 유효한지 검증합니다.
     * @param token JWT 토큰
     * @param userDetails 사용자 정보
     * @return 유효하면 true, 아니면 false
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * JWT 토큰에서 특정 클레임(claim)을 추출하는 범용 메소드.
     * @param token JWT 토큰
     * @param claimsResolver 클레임을 추출하는 함수
     * @return 추출된 클레임 값
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * JWT 서명에 사용할 SecretKey를 생성합니다.
     * @return SecretKey 객체
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
