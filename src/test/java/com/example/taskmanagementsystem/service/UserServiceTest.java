package com.example.taskmanagementsystem.service;

import com.example.taskmanagementsystem.client.rediscache.RedisCacheClient;
import com.example.taskmanagementsystem.dto.user.*;
import com.example.taskmanagementsystem.entity.RoleType;
import com.example.taskmanagementsystem.entity.User;
import com.example.taskmanagementsystem.exception.PasswordChangeIllegalArgumentException;
import com.example.taskmanagementsystem.exception.UsernameAlreadyTakenException;
import com.example.taskmanagementsystem.repo.UserRepository;
import com.example.taskmanagementsystem.security.AppUserDetails;
import com.example.taskmanagementsystem.security.JwtProvider;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles(value = "test")
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RedisCacheClient redisCacheClient;
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private UserToUserRsConverter userToUserRsConverter;
    @Mock
    private UserRqToUserConverter userRqToUserConverter;
    @Mock
    Authentication authentication;
    @Mock
    AppUserDetails appUserDetails;
    @InjectMocks
    private UserService userService;

    @Test
    void createLoginInfo_ShouldReturnLoginInfo() {
        User user = new User();
        UserRs userRs = new UserRs(1L,
                "admin",
                "admin@mail.com",
                Set.of(RoleType.ROLE_ADMIN));
        String token = "mockToken";

        Mockito.when(authentication.getPrincipal()).thenReturn(appUserDetails);
        Mockito.when(appUserDetails.getUser()).thenReturn(user);
        Mockito.when(userToUserRsConverter.convert(user)).thenReturn(userRs);
        Mockito.when(jwtProvider.createToken(authentication)).thenReturn(token);

        Map<String, Object> result = userService.createLoginInfo(authentication);

        assertNotNull(result);
        assertEquals(userRs, result.get("userInfo"));
        assertEquals(token, result.get("token"));
        Mockito.verify(redisCacheClient).set(Mockito.eq("whitelist:" + userRs.id()), Mockito.eq(token), Mockito.eq(2L), Mockito.eq(TimeUnit.HOURS));
    }

    @Test
    void findById_ShouldReturnUser() {
        Long id = 1L;
        User user = new User();
        UserRs userRs = new UserRs(1L,
                "admin",
                "admin@mail.com",
                Set.of(RoleType.ROLE_ADMIN));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        User result = userService.findById(id);

        assertEquals(user, result);
    }

    @Test
    void findById_ShouldThrowException_WhenUserNotFound() {
        Long id = 1L;
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> userService.findById(id));
        assertEquals("User with id 1 not found", exception.getMessage());
    }

    @Test
    void create_ShouldSaveUser() {
        UserRq rq = new UserRq("testUser",
                "testEmail",
                "Password123",
                Set.of(RoleType.ROLE_ADMIN),
                true);
        UserRs rs = new UserRs(1L,
                "admin",
                "admin@mail.com",
                Set.of(RoleType.ROLE_ADMIN));
        User user = new User();
        User savedUser = new User();

        when(userRqToUserConverter.convert(rq)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userToUserRsConverter.convert(savedUser)).thenReturn(rs);

        UserRs result = userService.create(rq);

        assertEquals(rs, result);
        verify(userRepository).save(user);
    }

    @Test
    void update_ShouldUpdateUser_WhenUserIsAdmin() {

        Long userId = 1L;
        UserRq rq = new UserRq("testUser",
                "testEmail",
                "Password123",
                Set.of(RoleType.ROLE_ADMIN),
                true);
        User updateUser = User.builder()
                .username("newUsername")
                .email("newEmail@mail.com")
                .roles(Set.of(RoleType.ROLE_ADMIN))
                .enabled(true)
                .build();
        User existingUser = User.builder()
                .username("oldUsername")
                .email("oldEmail@example.com")
                .build();
        User updatedUser = new User();
        UserRs rs = new UserRs(1L,
                "admin",
                "admin@mail.com",
                Set.of(RoleType.ROLE_ADMIN));

        when(userRqToUserConverter.convert(rq)).thenReturn(updateUser);
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(updatedUser);
        when(userToUserRsConverter.convert(updatedUser)).thenReturn(rs);
        when(authentication.getAuthorities())
                .thenReturn((Collection) Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserRs result = userService.update(userId, rq);

        assertEquals(rs, result);
        assertEquals("newUsername", existingUser.getUsername());
        assertEquals("newEmail@mail.com", existingUser.getEmail());
        assertEquals(Set.of(RoleType.ROLE_ADMIN), existingUser.getRoles());
    }

    @Test
    void update_ShouldThrowException_WhenUserNotFound() {

        Long userId = 1L;
        UserRq rq = new UserRq("testUser",
                "testEmail",
                "Password123",
                Set.of(RoleType.ROLE_ADMIN),
                true);
        when(userRqToUserConverter.convert(rq)).thenReturn(new User());
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userService.update(userId, rq));
        assertEquals("User with id 1 not found", exception.getMessage());
    }

    @Test
    void update_ShouldThrowException_WhenConversionFails() {

        Long userId = 1L;
        UserRq rq = new UserRq("testUser",
                "testEmail",
                "Password123",
                Set.of(RoleType.ROLE_ADMIN),
                true);

        Mockito.when(userRqToUserConverter.convert(rq)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.update(userId, rq));
        assertEquals("Conversion failed user testUser", exception.getMessage());
    }

    @Test
    void update_ShouldThrowException_WhenUsernameIsTaken() {

        Long userId = 1L;
        UserRq rq = new UserRq("testUser",
                "testEmail",
                "Password123",
                Set.of(RoleType.ROLE_ADMIN),
                true);
        User updateUser = new User();
        updateUser.setUsername("takenUsername");

        User existingUser = new User();
        existingUser.setUsername("oldUsername");

        Mockito.when(userRqToUserConverter.convert(rq)).thenReturn(updateUser);
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        Mockito.when(userRepository.existsByUsername("takenUsername")).thenReturn(true);

        UsernameAlreadyTakenException exception = assertThrows(UsernameAlreadyTakenException.class,
                () -> userService.update(userId, rq));
        assertEquals("Username takenUsername is already taken", exception.getMessage());
    }

    @Test
    void update_ShouldUpdateUsername_WhenUserIsNotAdmin() {

        Long userId = 1L;
        UserRq rq = new UserRq("testUser",
                "testEmail",
                "Password123",
                Set.of(RoleType.ROLE_ADMIN),
                true);
        User updateUser = new User();
        updateUser.setUsername("newUsername");

        User existingUser = new User();
        existingUser.setUsername("oldUsername");
        existingUser.setEmail("oldEmail@example.com");
        User updatedUser = new User();
        updatedUser.setUsername("newUsername");
        UserRs rs = new UserRs(1L,
                "admin",
                "admin@mail.com",
                Set.of(RoleType.ROLE_ADMIN));

        Mockito.when(userRqToUserConverter.convert(rq)).thenReturn(updateUser);
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        Mockito.when(userRepository.save(existingUser)).thenReturn(updatedUser);
        Mockito.when(userToUserRsConverter.convert(updatedUser)).thenReturn(rs);
        when(authentication.getAuthorities())
                .thenReturn((Collection) Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserRs result = userService.update(userId, rq);

        assertEquals(rs, result);
        assertEquals("newUsername", existingUser.getUsername());
    }

    @Test
    void deleteById_ShouldDeleteUser() {
        Long userId = 1L;
        User user = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.deleteById(userId);

        verify(userRepository).deleteById(userId);
    }

    @Test
    void changePassword_ShouldChangePasswordSuccess() {

        Long userId = 1L;
        PasswordRq passwordRq = new PasswordRq("oldPassword123", "NewPassword123", "NewPassword123");
        User user = User.builder().password("encodedOldPassword").build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword123", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("NewPassword123")).thenReturn("encodedNewPassword");

        userService.changePassword(userId, passwordRq);

        verify(userRepository).save(user);
        verify(redisCacheClient).delete("whitelist:" + userId);
        assertEquals("encodedNewPassword", user.getPassword());
    }

    @Test
    void changePassword_ShouldThrowException_WhenOldPasswordDoesNotMatch() {
        Long userId = 1L;
        PasswordRq passwordRq = new PasswordRq("wrongOldPassword", "NewPassword123", "NewPassword123");
        User user = User.builder().password("encodedOldPassword").build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongOldPassword", "encodedOldPassword")).thenReturn(false);

        BadCredentialsException exception = assertThrows(BadCredentialsException.class,
                () -> userService.changePassword(userId, passwordRq));
        assertEquals("Old password is incorrect", exception.getMessage());
    }

    @Test
    void changePassword_ShouldThrowException_WhenNewPasswordAndConfirmationDoNotMatch() {
        Long userId = 1L;
        PasswordRq passwordRq = new PasswordRq("oldPassword123", "NewPassword123", "MismatchPassword");
        User user = User.builder().password("encodedOldPassword").build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword123", "encodedOldPassword")).thenReturn(true);

        PasswordChangeIllegalArgumentException exception = assertThrows(PasswordChangeIllegalArgumentException.class,
                () -> userService.changePassword(userId, passwordRq));
        assertEquals("New password and confirm new password do not match", exception.getMessage());
    }

    @Test
    void changePassword_ShouldThrowException_WhenNewPasswordViolatesPolicy() {
        Long userId = 1L;
        PasswordRq passwordRq = new PasswordRq("oldPassword123", "weakpass", "weakpass");
        User user = User.builder().password("encodedOldPassword").build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword123", "encodedOldPassword")).thenReturn(true);

        PasswordChangeIllegalArgumentException exception = assertThrows(PasswordChangeIllegalArgumentException.class,
                () -> userService.changePassword(userId, passwordRq));
        assertEquals("New password does not conform to password policy", exception.getMessage());
    }

    @Test
    void changePassword_ShouldThrowException_WhenUserNotFound() {
        Long userId = 1L;
        PasswordRq passwordRq = new PasswordRq("oldPassword123", "NewPassword123", "NewPassword123");
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userService.changePassword(userId, passwordRq));
        assertEquals("User with id 1 not found", exception.getMessage());
    }

}