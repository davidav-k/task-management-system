package com.example.taskmanagementsystem.service;

import com.example.taskmanagementsystem.client.rediscache.RedisCacheClient;
import com.example.taskmanagementsystem.entity.RoleType;
import com.example.taskmanagementsystem.entity.User;
import com.example.taskmanagementsystem.exception.PasswordChangeIllegalArgumentException;
import com.example.taskmanagementsystem.repo.UserRepository;
import com.example.taskmanagementsystem.security.AppUserDetails;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles(value = "dev")
public class UserServiceTest {
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    RedisCacheClient redisCacheClient;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserService userService;
    private final User user = User.builder().id(1L).username("user").email("user@mail.com")
            .password("user").roles(Set.of(RoleType.ROLE_ADMIN)).build();

    @Test
    public void testFindByIdSuccess() {

        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        User returnedUser = userService.findById(1L);

        assertThat(returnedUser.getId()).isEqualTo(1L);
        assertThat(returnedUser.getUsername()).isEqualTo("user");
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    public void testFindByIdFail() {

        given(userRepository.findById(1L)).willReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> userService.findById(1L));

        assertThat(thrown).isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User with id 1 not found");
        verify(userRepository, times(1)).findById(1L);

    }

    @Test
    public void testFindAll() {
        User user1 = User.builder().id(2L).username("user1").build();
        List<User> users = List.of(user, user1);
        given(userRepository.findAll()).willReturn(users);

        List<User> returnedUsers = userService.findAll();

        assertThat(returnedUsers.get(0).getId()).isEqualTo(1L);
        assertThat(returnedUsers.get(0).getUsername()).isEqualTo("user");
        assertThat(returnedUsers.get(1).getId()).isEqualTo(2L);
        assertThat(returnedUsers.get(1).getUsername()).isEqualTo("user1");

        verify(userRepository, times(1)).findAll();
    }

    @Test
    public void testCreateUser() {
        given(userRepository.save(user)).willReturn(user);

        User savedUser = userService.create(user);

        assertThat(savedUser.getId()).isEqualTo(1L);
        assertThat(savedUser.getUsername()).isEqualTo("user");
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void testUpdateByAdminSuccess() {
        User update = User.builder().id(1L).username("UserUpdate").email("userUp@mail.com").build();

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userRepository.save(user)).willReturn(update);

        AppUserDetails appUserDetails = new AppUserDetails(user);
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(
                appUserDetails, null, appUserDetails.getAuthorities()));
        SecurityContextHolder.setContext(securityContext);

        User savedUser = userService.update(1L, update);

        assertThat(savedUser.getId()).isEqualTo(1L);
        assertThat(savedUser.getUsername()).isEqualTo("UserUpdate");
        assertThat(savedUser.getEmail()).isEqualTo("userUp@mail.com");
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void testUpdateByUserOnlyUpdateUserNameSuccess() {
        User update = User.builder().id(1L).username("UserUpdate").email("userUp@mail.com").build();
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userRepository.save(user)).willReturn(user);

        user.setRoles(Set.of(RoleType.ROLE_USER));
        AppUserDetails appUserDetails = new AppUserDetails(user);
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(
                appUserDetails, null, appUserDetails.getAuthorities()));
        SecurityContextHolder.setContext(securityContext);

        User savedUser = userService.update(1L, update);

        assertThat(savedUser.getId()).isEqualTo(1L);
        assertThat(savedUser.getUsername()).isEqualTo("UserUpdate");
        assertThat(savedUser.getEmail()).isEqualTo("user@mail.com");
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void testUpdateUserNotFoundFail() {

        User update = User.builder().id(1L).username("UserUpdate").build();
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            userService.update(1L, update);
        });

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void testDeleteByIdSuccess() {

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteById(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteByIdFail() {

        given(userRepository.findById(2L)).willReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.deleteById(2L));

        verify(userRepository, times(1)).findById(2L);
    }

    @Test
    void testChangePasswordSuccess(){

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(anyString(),anyString())).willReturn(true);
        given(passwordEncoder.encode(anyString())).willReturn("1newUser");
        given(userRepository.save(user)).willReturn(user);
        doNothing().when(redisCacheClient).delete(anyString());

        userService.changePassword(1L, "user", "1newUser", "1newUser");

        assertThat(user.getPassword()).isEqualTo("1newUser");
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testChangePasswordOldPasswordIsIncorrect(){
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(anyString(),anyString())).willReturn(false);
        Exception exception = assertThrows(BadCredentialsException.class, () -> {
            userService.changePassword(1L, "bad", "1newUser", "1newUser");
        });

        assertThat(exception).isInstanceOf(BadCredentialsException.class).hasMessage("Old password is incorrect");

          }

    @Test
    void testChangePasswordNewPasswordDoesNotMatchConfirmNewPassword(){

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(anyString(),anyString())).willReturn(true);


        Exception exception = assertThrows(PasswordChangeIllegalArgumentException.class, () -> {
            userService.changePassword(1L, "user", "1newUser", "2newUser");
        });

        assertThat(exception).isInstanceOf(PasswordChangeIllegalArgumentException.class).hasMessage("New password and confirm new password do not match");

    }

    @Test
    void testChangePasswordNewPasswordDoesNotConformToPolicy(){
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(anyString(),anyString())).willReturn(true);


        Exception exception = assertThrows(PasswordChangeIllegalArgumentException.class, () -> {
            userService.changePassword(1L, "user", "newUser", "newUser");
        });

        assertThat(exception).isInstanceOf(PasswordChangeIllegalArgumentException.class).hasMessage("New password does not conform to password policy");

    }

}