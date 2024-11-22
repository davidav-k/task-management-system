package com.example.taskmanagementsystem.controller;

import com.example.taskmanagementsystem.dto.StatusCode;
import com.example.taskmanagementsystem.dto.user.UserRq;
import com.example.taskmanagementsystem.dto.user.UserRqToUserConverter;
import com.example.taskmanagementsystem.dto.user.UserRs;
import com.example.taskmanagementsystem.dto.user.UserToUserRsConverter;
import com.example.taskmanagementsystem.entity.RoleType;
import com.example.taskmanagementsystem.entity.User;
import com.example.taskmanagementsystem.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
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

    @Value("${api.endpoint.base-url}")
    String baseUrl;

    private User admin;
    private User user;
    private UserRq rq;
    private UserRs rs;

    @BeforeEach
    void setUp() {
        admin = User.builder()
                .id(1L)
                .username("admin")
                .password("admin")
                .email("admin@admin.com")
                .roles(Set.of(RoleType.ROLE_ADMIN))
                .build();
        user = User.builder()
                .id(2L)
                .username("user")
                .password("user")
                .email("user@admin.com")
                .roles(Set.of(RoleType.ROLE_USER))
                .build();
        rs = new UserRs(1L,
                "admin",
                "admin@admin.com",
                Set.of(RoleType.ROLE_ADMIN));
        rq = new UserRq("admin",
                "admin@admin.com",
                "admin",
                Set.of(RoleType.ROLE_ADMIN));
    }

    @AfterEach
    void tearDown() {
    }


    @Test
    void findByIdSuccess() throws Exception {

        given(userService.findById(1L)).willReturn(admin);
        given(userToUserRsConverter.convert(admin)).willReturn(rs);

        mockMvc.perform(get(baseUrl + "/user/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").value(any(String.class)))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Found one"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.email").value("admin@admin.com"))
                .andExpect(jsonPath("$.data.roles").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    void findByIdFail() throws Exception {
        given(userService.findById(3L)).willThrow(new EntityNotFoundException("User with id 3 not found"));

        mockMvc.perform(get(baseUrl + "/user/3").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("User with id 3 not found"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void findAllSuccess() throws Exception {

        List<User> users = List.of(admin, user);
        given(userService.findAll()).willReturn(users);
        given(userToUserRsConverter.convert(ArgumentMatchers.any(User.class))).willReturn(rs);

        mockMvc.perform(get(baseUrl + "/user").accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Found all"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.size()").value(2))
                .andExpect(jsonPath("$.data[0].username").value("admin"))
                .andExpect(jsonPath("$.data[1].username").value("admin"));
    }

    @Test
    void testCreateSuccess() throws Exception {

        given(userRqToUserConverter.convert(rq)).willReturn(admin);
        given(userService.create(admin)).willReturn(admin);
        given(userToUserRsConverter.convert(admin)).willReturn(rs);

        mockMvc.perform(
                        post(baseUrl + "/user")
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(rq))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").value(any(String.class)))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("User created"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.email").value("admin@admin.com"))
                .andExpect(jsonPath("$.data.roles").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    void testCreateFail() throws Exception {

        UserRq fakeRq = new UserRq("","admin","", null);

        mockMvc.perform(
                        post(baseUrl + "/user")
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(fakeRq))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").value(any(String.class)))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("Provided arguments are not valid"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.username").value("Username must be from 3 to 10 symbols"))
                .andExpect(jsonPath("$.data.email").value("The email address must be in the format user@example.com"))
                .andExpect(jsonPath("$.data.password").value("The password length must be from 4 no more than 255 characters."))
                .andExpect(jsonPath("$.data.roles").value("RoleType must not be null"));
    }

    @Test
    void testUpdateSuccess() throws Exception {
        UserRs userRs = new UserRs(1L,"userUp", "userUp@mail.com", Set.of(RoleType.ROLE_USER));
        UserRq rq = new UserRq("userUp", "userUp@mail.com","userUp", Set.of(RoleType.ROLE_USER));
        given(userRqToUserConverter.convert(rq)).willReturn(user);
        given(userService.update(1L, user)).willReturn(user);
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
    void testUpdateFail() throws Exception {

        UserRq rq = new UserRq("", "","", Set.of());

        this.mockMvc.perform(put(baseUrl + "/user/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rq))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("Provided arguments are not valid"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.username").value("Username must be from 3 to 10 symbols"))
                .andExpect(jsonPath("$.data.password").value("The password length must be from 4 no more than 255 characters."))
                .andExpect(jsonPath("$.data.email").value("Email address cannot be empty"))
                .andExpect(jsonPath("$.data.roles").value("RoleType must not be null"));
    }

    @Test
    void testDeleteByIdSuccess() throws Exception {

        doNothing().when(userService).deleteById(1L);

        this.mockMvc.perform(delete(baseUrl + "/user/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Delete success"))
                .andExpect(jsonPath("$.data").isEmpty());

    }

    @Test
    void testDeleteByIdFail() throws Exception {

        doThrow(new EntityNotFoundException("user not found")).when(userService).deleteById(1L);

        this.mockMvc.perform(delete(baseUrl + "/user/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("user not found"))
                .andExpect(jsonPath("$.data").isEmpty());

    }
}