package com.example.taskmanagementsystem.service;

import com.example.taskmanagementsystem.client.rediscache.RedisCacheClient;
import com.example.taskmanagementsystem.dto.user.*;
import com.example.taskmanagementsystem.entity.User;
import com.example.taskmanagementsystem.exception.EmailAlreadyInUseException;
import com.example.taskmanagementsystem.exception.PasswordChangeIllegalArgumentException;
import com.example.taskmanagementsystem.exception.UsernameAlreadyTakenException;
import com.example.taskmanagementsystem.repo.UserRepository;
import com.example.taskmanagementsystem.security.AppUserDetails;
import com.example.taskmanagementsystem.security.JwtProvider;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisCacheClient redisCacheClient;
    private final JwtProvider jwtProvider;
    private final UserToUserRsConverter userToUserRsConverter;
    private final UserRqToUserConverter userRqToUserConverter;

    public Map<String, Object> createLoginInfo(@NotNull Authentication authentication) {
        AppUserDetails principal = (AppUserDetails) authentication.getPrincipal();
        UserRs userRs = Optional.ofNullable(userToUserRsConverter.convert(principal.getUser()))
                .orElseThrow(() -> new IllegalArgumentException("Conversion failed"));
        String token = jwtProvider.createToken(authentication);
        redisCacheClient.set("whitelist:" + userRs.id(), token, 2, TimeUnit.HOURS);
        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("userInfo", userRs);
        loginInfo.put("token", token);
        return loginInfo;
    }

    public UserRs findById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(
                MessageFormatter.format("User with id {} not found", id).getMessage()));
        return userToUserRsConverter.convert(user);
    }

    public List<UserRs> findAll() {
        List<User> users = userRepository.findAll();
        return users.stream().map(userToUserRsConverter::convert).toList();
    }

    @Transactional
    public UserRs create(UserRq rq) {
        validateUniqueFields(rq);
        User newUser = Optional.ofNullable(userRqToUserConverter.convert(rq))
                .orElseThrow(() -> new IllegalArgumentException(
                        MessageFormatter.format("Conversion failed user {}", rq.username()).getMessage()));
        User savedUser = userRepository.save(newUser);
        return userToUserRsConverter.convert(savedUser);
    }

    private void validateUniqueFields(@NotNull UserRq rq) {
        if (userRepository.existsByUsername(rq.username())) {
            throw new UsernameAlreadyTakenException(
                    MessageFormatter.format("Username {} is already taken", rq.username()).getMessage());
        }
        if (userRepository.existsByEmail(rq.email())) {
            throw new EmailAlreadyInUseException(
                    MessageFormatter.format("Email {} is already in use", rq.email()).getMessage());
        }
    }

    @Transactional
    public UserRs update(Long userId, UserRq rq) {
        User updateUser = Optional.ofNullable(userRqToUserConverter.convert(rq))
                .orElseThrow(() -> new IllegalArgumentException(
                        MessageFormatter.format("Conversion failed user {}", rq.username()).getMessage()));
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        MessageFormatter.format("User with id {} not found", userId).getMessage()));

        validateUsernameAndEmailForUpdate(existingUser, updateUser);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication.getAuthorities()
                .stream()
                .noneMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_admin"))) {
            existingUser.setUsername(updateUser.getUsername());
        } else {
            existingUser.setUsername(updateUser.getUsername());
            existingUser.setEmail(updateUser.getEmail());
            existingUser.setEnabled(updateUser.isEnabled());
            existingUser.setRoles(updateUser.getRoles());
        }

        User savedUser = userRepository.save(existingUser);
        return userToUserRsConverter.convert(savedUser);
    }

    private void validateUsernameAndEmailForUpdate(@NotNull User existingUser, @NotNull User updateUser) {
        if (!existingUser.getUsername().equals(updateUser.getUsername()) &&
                userRepository.existsByUsername(updateUser.getUsername())) {
            throw new UsernameAlreadyTakenException(
                    MessageFormatter.format("Username {} is already taken", updateUser.getUsername()).getMessage());
        }
        if (!existingUser.getEmail().equals(updateUser.getEmail()) &&
                userRepository.findByEmail(updateUser.getEmail()).isPresent()) {
            throw new EmailAlreadyInUseException(
                    MessageFormatter.format("Email {} is already in use", updateUser.getEmail()).getMessage());
        }
    }

    @Transactional
    public void deleteById(Long id) {
        userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(
                MessageFormatter.format("User with id {} not found", id).getMessage()));
        userRepository.deleteById(id);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .map(AppUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException(
                        MessageFormatter.format("User with userName {} not found", username).getMessage()
                ));
    }

    @Transactional
    public void changePassword(Long userId, @NotNull PasswordRq rq) {

        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException(
                MessageFormatter.format("User with id {} not found", userId).getMessage()));

        if (!passwordEncoder.matches(rq.getOldPassword(), user.getPassword())) {
            throw new BadCredentialsException("Old password is incorrect");
        }

        if (!rq.getNewPassword().equals(rq.getConfirmNewPassword())) {
            throw new PasswordChangeIllegalArgumentException("New password and confirm new password do not match");
        }

        //The new password must contain at least one digit, one lowercase letter, one uppercase letter, and be at least 8 characters long.
        String passwordPolicy = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$";
        if (!rq.getNewPassword().matches(passwordPolicy)) {
            throw new PasswordChangeIllegalArgumentException("New password does not conform to password policy");
        }

        user.setPassword(passwordEncoder.encode(rq.getNewPassword()));

        redisCacheClient.delete("whitelist:" + userId);

        userRepository.save(user);

    }
}
