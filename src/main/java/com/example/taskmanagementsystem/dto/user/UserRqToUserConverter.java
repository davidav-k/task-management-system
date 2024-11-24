package com.example.taskmanagementsystem.dto.user;

import com.example.taskmanagementsystem.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserRqToUserConverter implements Converter<UserRq, User> {

    private final PasswordEncoder passwordEncoder;

    public User convert(@Valid UserRq rq) {

        return User.builder()
                .username(rq.username())
                .password(passwordEncoder.encode(rq.password()))
                .email(rq.email())
                .roles(rq.roles())
                .enabled(true)
                .build();

    }
}
