package com.example.taskmanagementsystem.controller;

import com.example.taskmanagementsystem.dto.StatusCode;
import com.example.taskmanagementsystem.dto.user.UserRq;
import com.example.taskmanagementsystem.entity.RoleType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.testcontainers.RedisContainer;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles(value = "test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UserControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Value("${api.endpoint.base-url}")
    String baseUrl;
    String tokenAdmin;
    String tokenUser;

    @Container
    @ServiceConnection
    static RedisContainer redisContainer = new RedisContainer(DockerImageName.parse("redis:6.2.6"));

    @BeforeEach
    void setUp() throws Exception {
        ResultActions resultActionsAdmin = mockMvc.perform(post(baseUrl + "/user/login")
                .with(httpBasic("admin", "Password123")));
        MvcResult mvcResultAdmin = resultActionsAdmin.andDo(print()).andReturn();
        String contentAsStringAdmin = mvcResultAdmin.getResponse().getContentAsString();
        JSONObject jsonAdmin = new JSONObject(contentAsStringAdmin);
        tokenAdmin = "Bearer " + jsonAdmin.getJSONObject("data").getString("token");

        ResultActions resultActionsUser = mockMvc.perform(post(baseUrl + "/user/login")
                .with(httpBasic("user1", "Password123")));
        MvcResult mvcResultUser = resultActionsUser.andDo(print()).andReturn();
        String contentAsStringUser = mvcResultUser.getResponse().getContentAsString();
        JSONObject jsonUser = new JSONObject(contentAsStringUser);
        tokenUser = "Bearer " + jsonUser.getJSONObject("data").getString("token");
    }

    @Test
    void testFindByIdByAdminOwnInfoSuccess() throws Exception {
        mockMvc.perform(get(baseUrl + "/user/1")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", tokenAdmin))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Found one success"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.username").value("admin"));
    }

    @Test
    void testFindByIdByAdminAnotherUserInfoSuccess() throws Exception {
        mockMvc.perform(get(baseUrl + "/user/2")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", tokenAdmin))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Found one success"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.username").value("user1"));
    }

    @Test
    void testFindByIdByUserAccessingOwnInfoSuccess() throws Exception {
        mockMvc.perform(get(baseUrl + "/user/2")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", tokenUser))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Found one success"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.username").value("user1"));
    }

    @Test
    void testFindByIdByUserAccessingAnotherUserInfoFail() throws Exception {
        mockMvc.perform(get(baseUrl + "/user/1")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", tokenUser))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission"));
    }

    @Test
    void testFindByIdNotFoundFail() throws Exception {
        mockMvc.perform(get(baseUrl + "/user/5")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", tokenAdmin))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("User with id 5 not found"));
    }

    @Test
    void testCreateByAdminSuccess() throws Exception {
        UserRq rq = new UserRq("user5", "user5@mail.com", "Password123", Set.of(RoleType.ROLE_USER), true);

        mockMvc.perform(post(baseUrl + "/user")
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rq))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", tokenAdmin))
                .andExpect(jsonPath("$.timestamp").value(any(String.class)))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("User created successfully"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.username").value("user5"))
                .andExpect(jsonPath("$.data.email").value("user5@mail.com"))
                .andExpect(jsonPath("$.data.roles").value("ROLE_USER"))
                .andExpect(jsonPath("$.data.password").doesNotExist());
        mockMvc.perform(get(baseUrl + "/user")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", tokenAdmin))
                .andExpect(jsonPath("$.data", Matchers.hasSize(4)));
    }

    @Test
    void testCreateByAdminWrongDataFail() throws Exception {
        UserRq fakeRq = new UserRq("", "use", "", null, true);

        mockMvc.perform(
                        post(baseUrl + "/user")
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(fakeRq))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", tokenAdmin))
                .andExpect(jsonPath("$.timestamp").value(any(String.class)))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("Provided arguments are not valid"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.username").value("Username must be from 3 to 10 symbols"))
                .andExpect(jsonPath("$.data.email").value("The email address must be in the format user@example.com"))
                .andExpect(jsonPath("$.data.password").value("Password must contain at least one digit, one lowercase letter, one uppercase letter, and be at least 8 characters long"))
                .andExpect(jsonPath("$.data.roles").value("RoleType must not be null"));

    }

    @Test
    void testUpdateByAdminOnwInfoSuccess() throws Exception {

        UserRq rq = new UserRq("adminUp", "admin@mail.com", "Password123", Set.of(RoleType.ROLE_ADMIN), true);

        this.mockMvc.perform(put(baseUrl + "/user/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rq))
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", tokenAdmin))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update success"))
                .andExpect(jsonPath("$.data.username").value("adminUp"));
    }

    @Test
    void testUpdateByAdminAnotherUserSuccess() throws Exception {

        UserRq rq = new UserRq("userUp", "userUp@mail.com", "Password123", Set.of(RoleType.ROLE_USER), true);

        this.mockMvc.perform(put(baseUrl + "/user/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rq))
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", tokenAdmin))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update success"))
                .andExpect(jsonPath("$.data.username").value("userUp"));
    }

    @Test
    void testUpdateByUserOwnInfoSuccess() throws Exception {

        UserRq rq = new UserRq("userUp", "userUp@mail.com", "Password123", Set.of(RoleType.ROLE_USER), true);

        this.mockMvc.perform(put(baseUrl + "/user/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rq))
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", tokenUser))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update success"))
                .andExpect(jsonPath("$.data.username").value("userUp"));
    }

    @Test
    void testUpdateByUserAnotherUserInfoFail() throws Exception {
        mockMvc.perform(get(baseUrl + "/user/1")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", tokenUser))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission"));
    }

    @Test
    void testUpdateWrongDataFail() throws Exception {

        UserRq rq = new UserRq("", "", "", Set.of(), true);

        this.mockMvc.perform(put(baseUrl + "/user/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rq))
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", tokenAdmin))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("Provided arguments are not valid"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.username").value("Username must be from 3 to 10 symbols"))
                .andExpect(jsonPath("$.data.password").value("Password must contain at least one digit, one lowercase letter, one uppercase letter, and be at least 8 characters long"))
                .andExpect(jsonPath("$.data.email").value("Email address cannot be empty"))
                .andExpect(jsonPath("$.data.roles").value("RoleType must not be null"));
    }

    @Test
    void testDeleteByIdSuccess() throws Exception {

        this.mockMvc.perform(delete(baseUrl + "/user/2")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", tokenAdmin))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Delete success"));
    }

    @Test
    void testDeleteByIdFail() throws Exception {

        this.mockMvc.perform(delete(baseUrl + "/user/5")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", tokenAdmin))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("User with id 5 not found"));

    }

    @Test
    void testFindAllByAdminSuccess() throws Exception {
        mockMvc.perform(get(baseUrl + "/user")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", tokenAdmin))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Found all"))
                .andExpect(jsonPath("$.data[0].username").value("admin"))
                .andExpect(jsonPath("$.data[1].username").value("user1"))
                .andExpect(jsonPath("$.data", Matchers.hasSize(3)));
    }

    @Test
    void testFindAllByUserFail() throws Exception {
        mockMvc.perform(get(baseUrl + "/user")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", tokenUser))
                .andDo(print())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission"));
    }

    @Test
    void testChangeUserPasswordSuccess() throws Exception {

        Map<String, String> passwordMap = new HashMap<>();
        passwordMap.put("oldPassword", "Password123");
        passwordMap.put("newPassword", "Abc12345");
        passwordMap.put("confirmNewPassword", "Abc12345");

        this.mockMvc.perform(patch(baseUrl + "/user/2/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordMap))
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", tokenAdmin))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Change password success"));
    }

    @Test
    void testChangeUserPasswordWithWrongOldPassword() throws Exception {

        Map<String, String> passwordMap = new HashMap<>();
        passwordMap.put("oldPassword", "wronguser");
        passwordMap.put("newPassword", "Abc12345");
        passwordMap.put("confirmNewPassword", "Abc12345");

        this.mockMvc.perform(patch(baseUrl + "/user/2/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordMap))
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", tokenAdmin))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("username or password is incorrect"))
                .andExpect(jsonPath("$.data").value("Old password is incorrect"));
    }

    @Test
    void testChangeUserPasswordWithNewPasswordNotMatchingConfirmNewPassword() throws Exception {

        Map<String, String> passwordMap = new HashMap<>();
        passwordMap.put("oldPassword", "Password123");
        passwordMap.put("newPassword", "Abc12345");
        passwordMap.put("confirmNewPassword", "AAbc12345");

        this.mockMvc.perform(patch(baseUrl + "/user/2/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordMap))
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", tokenAdmin))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("New password and confirm new password do not match"));
    }

    @Test
    void testChangeUserPasswordWithUserNotFound() throws Exception {

        Map<String, String> passwordMap = new HashMap<>();
        passwordMap.put("oldPassword", "user");
        passwordMap.put("newPassword", "Abc12345");
        passwordMap.put("confirmNewPassword", "AAbc12345");

        this.mockMvc.perform(patch(baseUrl + "/user/5/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordMap))
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", tokenAdmin))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("User with id 5 not found"));
    }

    @Test
    void testChangeUserPasswordNotConfirmingToPasswordPolicy() throws Exception {

        Map<String, String> passwordMap = new HashMap<>();
        passwordMap.put("oldPassword", "user");
        passwordMap.put("newPassword", "Abc");
        passwordMap.put("confirmNewPassword", "Abc");

        this.mockMvc.perform(patch(baseUrl + "/user/2/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordMap))
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", tokenAdmin))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("Provided arguments are not valid"));
    }
}
