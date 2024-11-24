package com.example.taskmanagementsystem.security;

import com.example.taskmanagementsystem.client.rediscache.RedisCacheClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final RedisCacheClient redisCacheClient;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")){
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Jwt jwt = (Jwt)authentication.getPrincipal();
            String userId = jwt.getClaim("userId").toString();

            if (!redisCacheClient.isUserTokenInWhiteList(userId, jwt.getTokenValue())) {
                throw new BadCredentialsException("invalid token");
            }
        }

        return true;
    }
}
