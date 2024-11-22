package com.example.taskmanagementsystem.service;

import com.example.taskmanagementsystem.client.rediscache.RedisCacheClient;
import com.example.taskmanagementsystem.entity.User;
import com.example.taskmanagementsystem.exception.PasswordChangeIllegalArgumentException;
import com.example.taskmanagementsystem.repo.UserRepository;
import com.example.taskmanagementsystem.security.AppUserDetails;
import com.example.taskmanagementsystem.util.AppHelperUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisCacheClient redisCacheClient;


    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(
                MessageFormatter.format("User with id {} not found", id).getMessage()));
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    public User create(User user) {

        return userRepository.save(user);
    }

    @Transactional
    public User update(Long id, User update) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findById(id)
                .map(existedUser -> {
                    //  If the user id not admin, then user can only update own username
                    if (authentication.getAuthorities().stream()
                            .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))) {
                        redisCacheClient.delete("whitelist:" + id);
                    } else {
                        update.setEmail(null);
                        update.setRoles(null);
                        update.setPassword(null);
                    }

                    AppHelperUtils.copyNonNullProperties(update, existedUser);
                    return userRepository.save(existedUser);
                })
                .orElseThrow(() -> new EntityNotFoundException("user not found"));
    }
    @Transactional
    public void deleteById(Long id) {
        userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("user not found"));

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
    public void changePassword(Long userId, String oldPassword, String newPassword, String confirmNewPassword) {

        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException(
                MessageFormatter.format("User with id {} not found", userId).getMessage()));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadCredentialsException("Old password is incorrect");
        }

        if (!newPassword.equals(confirmNewPassword)) {
            throw new PasswordChangeIllegalArgumentException("New password and confirm new password do not match");
        }

        //The new password must contain at least one digit, one lowercase letter, one uppercase letter, and be at least 8 characters long.
        String passwordPolicy = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$";
        if (!newPassword.matches(passwordPolicy)) {
            throw new PasswordChangeIllegalArgumentException("New password does not conform to password policy");
        }

        user.setPassword(passwordEncoder.encode(newPassword));

        redisCacheClient.delete("whitelist:" + userId);

        userRepository.save(user);

    }
}
