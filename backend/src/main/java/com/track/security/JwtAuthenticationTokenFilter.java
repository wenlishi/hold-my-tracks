package com.track.security;

import com.track.util.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT认证令牌过滤器。
 * 继承自OncePerRequestFilter，确保每个请求只经过一次过滤。
 * 负责从请求中提取JWT令牌，验证其有效性，并设置Spring Security的上下文。
 * 依赖于JwtUtils和UserDetailsService来处理JWT令牌和加载用户详情。
 * 当请求携带有效的JWT令牌时，过滤器会从令牌中提取用户名，加载用户详情，并将认证信息存储在SecurityContext中，以便后续的安全检查使用。
 * 如果令牌无效或不存在，过滤器将不会设置认证信息，允许请求继续进行，但可能会被后续的安全机制拒绝访问受保护的资源。
 */
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    /**
     * JwtUtils实例，用于处理JWT令牌的生成和验证。
     */
    @Autowired
    private JwtUtils jwtUtils;

    /**
     * UserDetailsService实例，用于加载用户详情。
     */
    @Autowired
    private UserDetailsService userDetailsService;

    /**
     * 日志记录器，用于记录JWT认证相关的日志信息。
     * 使用SLF4J的LoggerFactory创建。
     */
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationTokenFilter.class);

    /**
     * 过滤请求，提取并验证JWT令牌。
     * 如果令牌有效，设置Spring Security的上下文。
     * @param request  HTTP请求
     * @param response HTTP响应
     * @param filterChain 过滤器链
     * @throws ServletException 如果发生Servlet异常
     * @throws IOException 如果发生IO异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求中解析JWT令牌。
     * @param request HTTP请求
     * @return JWT令牌字符串，如果不存在则返回null
     */
    private String parseJwt(HttpServletRequest request) {
        // 1. 获取请求头
        String headerAuth = request.getHeader("Authorization");
        // 2. 检查请求头是否包含Bearer令牌
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
}