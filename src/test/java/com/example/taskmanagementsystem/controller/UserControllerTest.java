package com.example.taskmanagementsystem.controller;

import com.example.taskmanagementsystem.dto.StatusCode;
import com.example.taskmanagementsystem.dto.user.*;
import com.example.taskmanagementsystem.entity.RoleType;
import com.example.taskmanagementsystem.entity.User;
import com.example.taskmanagementsystem.service.UserService;
import com.example.taskmanagementsystem.util.DBDataInitializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles(value = "test")
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    UserService userService;
    @MockBean
    UserRqToUserConverter userRqToUserConverter;
    @MockBean
    UserToUserRsConverter userToUserRsConverter;
    @MockBean
    Authentication authentication;
    @MockBean
    DBDataInitializer dbDataInitializer;

    @Value("${api.endpoint.base-url}")
    String baseUrl;

    @Test
    void getLoginInfo_ShouldReturnUserInfoAndToken() throws Exception {
        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("userInfo", new UserRs(1L, "testUser", "test@mail.com", Set.of(RoleType.ROLE_USER)));
        loginInfo.put("token", "jwtToken");

        given(authentication.getName()).willReturn("testUser");
        given(userService.createLoginInfo(authentication)).willReturn(loginInfo);

        mockMvc.perform(
                        post(baseUrl + "/user/login")
                                .accept(MediaType.APPLICATION_JSON)
                                .principal(authentication))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("User info and token"))
                .andExpect(jsonPath("$.data.userInfo.username").value("testUser"))
                .andExpect(jsonPath("$.data.userInfo.email").value("test@mail.com"))
                .andExpect(jsonPath("$.data.userInfo.roles[0]").value("ROLE_USER"))
                .andExpect(jsonPath("$.data.token").value("jwtToken"));

        verify(userService, times(1)).createLoginInfo(authentication);
    }

    @Test
    void findById_ShouldReturnUserRs() throws Exception {
        User admin = User.builder().id(1L).username("admin").password("Password123").email("admin@mail.com").roles(Set.of(RoleType.ROLE_ADMIN)).build();
        UserRs adminRs = new UserRs(1L, "admin", "admin@mail.com", Set.of(RoleType.ROLE_ADMIN));
        given(userService.findByIdReturnUserRs(1L)).willReturn(adminRs);
        given(userToUserRsConverter.convert(admin)).willReturn(adminRs);

        mockMvc.perform(get(baseUrl + "/user/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Found one success"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.email").value("admin@mail.com"))
                .andExpect(jsonPath("$.data.roles").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    void findById_ShouldThrowException_WhenUserNotFound() throws Exception {
        given(userService.findByIdReturnUserRs(3L)).willThrow(
                new EntityNotFoundException("User with id 3 not found"));

        mockMvc.perform(get(baseUrl + "/user/3").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("User with id 3 not found"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void findAllSuccess() throws Exception {
        UserRs adminRs = new UserRs(1L, "admin", "admin@mail.com", Set.of(RoleType.ROLE_ADMIN));
        UserRs userRs = new UserRs(1L, "user", "user@mail.com", Set.of(RoleType.ROLE_USER));
        List<UserRs> userRsList = List.of(adminRs, userRs);
        given(userService.findAll()).willReturn(userRsList);
        given(userToUserRsConverter.convert(any(User.class))).willReturn(adminRs);

        mockMvc.perform(get(baseUrl + "/user").accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Found all"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.size()").value(2))
                .andExpect(jsonPath("$.data[0].username").value("admin"))
                .andExpect(jsonPath("$.data[1].username").value("user"));
    }

    @Test
    void create_ShouldSaveUser() throws Exception {
        User admin = User.builder().id(1L).username("admin").password("Password123").email("admin@mail.com").roles(Set.of(RoleType.ROLE_ADMIN)).build();
        UserRs adminRs = new UserRs(1L, "admin", "admin@mail.com", Set.of(RoleType.ROLE_ADMIN));
        UserRq adminRq = new UserRq("admin", "admin@mail.com", "Password123", Set.of(RoleType.ROLE_ADMIN), true);
        given(userRqToUserConverter.convert(adminRq)).willReturn(admin);
        given(userService.create(adminRq)).willReturn(adminRs);
        given(userToUserRsConverter.convert(admin)).willReturn(adminRs);

        mockMvc.perform(
                        post(baseUrl + "/user")
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(adminRq))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("User created successfully"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.email").value("admin@mail.com"))
                .andExpect(jsonPath("$.data.roles").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    void create_NotValidUserDataFail() throws Exception {

        UserRq fakeRq = new UserRq("","admin","", null, true);

        mockMvc.perform(
                        post(baseUrl + "/user")
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(fakeRq))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("Provided arguments are not valid"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.username").value("Username must be from 3 to 10 symbols"))
                .andExpect(jsonPath("$.data.email").value("The email address must be in the format user@example.com"))
                .andExpect(jsonPath("$.data.password").value("Password must contain at least one digit, one lowercase letter, one uppercase letter, and be at least 8 characters long"))
                .andExpect(jsonPath("$.data.roles").value("RoleType must not be null or empty"));
    }

    @Test
    void update_ShouldUpdateUser() throws Exception {
        User user = User.builder().id(2L).username("user").password("Password123").email("user@mail.com").roles(Set.of(RoleType.ROLE_USER)).build();
        UserRs userRs = new UserRs(1L,"userUp", "userUp@mail.com", Set.of(RoleType.ROLE_USER));
        UserRq rq = new UserRq("userUp", "userUp@mail.com","Password123", Set.of(RoleType.ROLE_USER), true);
        given(userRqToUserConverter.convert(rq)).willReturn(user);
        given(userService.update(1L, rq)).willReturn(userRs);
        given(userToUserRsConverter.convert(user)).willReturn(userRs);

        this.mockMvc.perform(put(baseUrl + "/user/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rq))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update success"))
                .andExpect(jsonPath("$.data.username").value("userUp"));
    }

    @Test
    void update_NotValidUserDataFail() throws Exception {

        UserRq rq = new UserRq("", "","", Set.of(), true);

        this.mockMvc.perform(put(baseUrl + "/user/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rq))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("Provided arguments are not valid"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.username").value("Username must be from 3 to 10 symbols"))
                .andExpect(jsonPath("$.data.password").value("Password must contain at least one digit, one lowercase letter, one uppercase letter, and be at least 8 characters long"))
                .andExpect(jsonPath("$.data.email").value("Email address cannot be empty"))
                .andExpect(jsonPath("$.data.roles").value("RoleType must not be null or empty"));
    }

    @Test
    void deleteById_ShouldDeleteUser() throws Exception {

        doNothing().when(userService).deleteById(1L);

        this.mockMvc.perform(delete(baseUrl + "/user/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Delete success"));

    }

    @Test
    void deleteById_ShouldThrowException_WhenUserNotFound() throws Exception {

        doThrow(new EntityNotFoundException("user not found")).when(userService).deleteById(1L);

        this.mockMvc.perform(delete(baseUrl + "/user/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("user not found"));

    }

    @Test
    void changePassword_ShouldUpdatePassword() throws Exception {
        PasswordRq rq = new PasswordRq("Password123",
                "Password12345",
                "Password12345");
        doNothing().when(userService).changePassword(1L, rq);

        mockMvc.perform(
                        patch(baseUrl + "/user/1/password")
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(rq))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Change password success"));
    }
}