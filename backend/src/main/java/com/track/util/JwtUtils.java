package com.track.util;

import com.track.security.UserPrincipal;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * 工具类，用于生成和验证JWT令牌。
 * 包含生成令牌、提取用户名和验证令牌的方法。
 * 依赖于io.jsonwebtoken库。Jwts为其中的主要类，用于创建和解析JWT。
 */
@Component
public class JwtUtils {
    /**
     * 日志记录器，用于记录JWT相关的日志信息。
     * 使用SLF4J的LoggerFactory创建。
     */
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    /**
     * JWT密钥，从应用程序配置中注入。
     */
    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * JWT过期时间（以秒为单位），从应用程序配置中注入。
     */
    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    /**
     * 获取用于签名JWT的密钥。
     * @return 用于签名的SecretKey
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * 生成JWT令牌。当前端用户发来请求时,过滤器JwtAuthenticationTokenFilter会调用此方法生成JWT令牌。
     * @param authentication 认证信息
     * @return 生成的JWT令牌
     */
    public String generateJwtToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject((userPrincipal.getUsername()))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs * 1000L))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    /**
     * 从JWT令牌中提取用户名。
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * 验证JWT令牌的有效性。
     * @param authToken JWT令牌
     * @return 如果令牌有效则返回true，否则返回false
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (SecurityException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }
}