package com.example.taskmanagementsystem.security;

import com.example.taskmanagementsystem.client.rediscache.RedisCacheClient;
import com.example.taskmanagementsystem.dto.user.UserRs;
import com.example.taskmanagementsystem.dto.user.UserToUserRsConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtProvider jwtProvider;
    private final UserToUserRsConverter userToUserRsConverter;
    private final RedisCacheClient redisCacheClient;


    public Map<String, Object> createLoginInfo(Authentication authentication) {

        AppUserDetails principal = (AppUserDetails) authentication.getPrincipal();
        UserRs userRs = userToUserRsConverter.convert(principal.getUser());
        String token = jwtProvider.createToken(authentication);

        assert userRs != null;
        redisCacheClient.set("whitelist:" + userRs.id(), token, 2, TimeUnit.HOURS);

        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("userInfo", userRs);
        loginInfo.put("token", token);

        return loginInfo;



    }
}
