package com.example.taskmanagementsystem.dto.user;


import com.example.taskmanagementsystem.entity.User;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UserToUserRsConverter implements Converter<User, UserRs> {

    @Override
    public UserRs convert(User user) {
        return new UserRs(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoles());
    }
}
