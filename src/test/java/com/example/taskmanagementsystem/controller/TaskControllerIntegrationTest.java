package com.example.taskmanagementsystem.controller;

import com.example.taskmanagementsystem.dto.StatusCode;
import com.example.taskmanagementsystem.dto.task.TaskRq;
import com.example.taskmanagementsystem.entity.Priority;
import com.example.taskmanagementsystem.entity.Status;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles(value = "test")
public class TaskControllerIntegrationTest {
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
                .with(httpBasic("admin", "admin")));
        MvcResult mvcResultAdmin = resultActionsAdmin.andDo(print()).andReturn();
        String contentAsStringAdmin = mvcResultAdmin.getResponse().getContentAsString();
        JSONObject jsonAdmin = new JSONObject(contentAsStringAdmin);
        tokenAdmin = "Bearer " + jsonAdmin.getJSONObject("data").getString("token");

        ResultActions resultActionsUser = mockMvc.perform(post(baseUrl + "/user/login")
                .with(httpBasic("user", "user")));
        MvcResult mvcResultUser = resultActionsUser.andDo(print()).andReturn();
        String contentAsStringUser = mvcResultUser.getResponse().getContentAsString();
        JSONObject jsonUser = new JSONObject(contentAsStringUser);
        tokenUser = "Bearer " + jsonUser.getJSONObject("data").getString("token");
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testFindByIdSuccess() throws Exception {

        this.mockMvc.perform(get(baseUrl + "/task/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Found one"))
                .andExpect(jsonPath("$.data.title").value("Task1"))
                .andExpect(jsonPath("$.data.commentsRs[0].comment").value("Comment1"))
                .andExpect(jsonPath("$.data.commentsRs[1].comment").value("Comment2"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testFindByIdFail() throws Exception {

        this.mockMvc.perform(get(baseUrl + "/comment/14")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", tokenAdmin))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Comment with id 14 not found"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testFindAllSuccess() throws Exception {

        mockMvc.perform(get(baseUrl + "/task")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Found all"))
                .andExpect(jsonPath("$.data.content").exists())
                .andExpect(jsonPath("$.data.content[0].title").value("Task1"))
                .andExpect(jsonPath("$.data.content[0].commentsRs[0].comment").value("Comment1"))
                .andExpect(jsonPath("$.data.content[0].commentsRs[1].comment").value("Comment2"))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(2)));
    }

    @Test
    public void testCreateByAdminSuccess() throws Exception {

        TaskRq rq = new TaskRq("Task", "Create Task", Status.WAITING, Priority.LOW, 1L, 1L);

        this.mockMvc.perform(post(baseUrl + "/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rq))
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", tokenAdmin))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Task created"))
                .andExpect(jsonPath("$.data").exists());
        mockMvc.perform(get(baseUrl + "/task")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(3)));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void testCreateByUserSuccess() throws Exception {

        TaskRq rq = new TaskRq("Task", "Create Task", Status.WAITING, Priority.LOW, 1L, 1L);

        this.mockMvc.perform(post(baseUrl + "/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rq))
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", tokenUser))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Task created"))
                .andExpect(jsonPath("$.data").exists());
        mockMvc.perform(get(baseUrl + "/task")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(3)));
    }

    @Test
    void testCreateTaskFail() throws Exception {
        TaskRq rq = new TaskRq("", "", null, null, null, null);

        this.mockMvc.perform(post(baseUrl + "/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rq))
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", tokenAdmin))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("Provided arguments are not valid"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.title").value("Title must be from 3 to 20"))
                .andExpect(jsonPath("$.data.description").value("Description must be from 5 to 50"))
                .andExpect(jsonPath("$.data.status").value("Status must not be null"))
                .andExpect(jsonPath("$.data.priority").value("Priority must not be null"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testUpdateTaskSuccess() throws Exception {
        TaskRq rq = new TaskRq("TaskUP", "Update Task", Status.WAITING, Priority.LOW, 1L, 1L);

        this.mockMvc.perform(put(baseUrl + "/task/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rq))
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", tokenAdmin))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update success"))
                .andExpect(jsonPath("$.data.title").value("TaskUP"))
                .andExpect(jsonPath("$.data.description").value("Update Task"));
    }

    @Test
    void testDeleteByIdSuccess() throws Exception {

        this.mockMvc.perform(delete(baseUrl + "/task/1")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", tokenAdmin))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Delete success"))
                .andExpect(jsonPath("$.data").isEmpty());
        mockMvc.perform(get(baseUrl + "/task")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(1)));

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testDeleteByIdFail() throws Exception {

        this.mockMvc.perform(delete(baseUrl + "/task/3")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", tokenAdmin))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("task not found"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testFilterTasksByAuthorSuccess() throws Exception {

        this.mockMvc.perform(get(baseUrl + "/task/filter?pageNumber=0&pageSize=10&authorId=1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", tokenAdmin))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Filtered tasks"))
                .andExpect(jsonPath("$.data.content").exists())
                .andExpect(jsonPath("$.data.content[0].title").value("Task1"))
                .andExpect(jsonPath("$.data.content[0].commentsRs[0].comment").value("Comment1"))
                .andExpect(jsonPath("$.data.content[0].commentsRs[1].comment").value("Comment2"))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(1)));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testFilterTasksByAssigneeSuccess() throws Exception {

        this.mockMvc.perform(get(baseUrl + "/task/filter?pageNumber=0&pageSize=10&assigneeId=2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", tokenAdmin))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Filtered tasks"))
                .andExpect(jsonPath("$.data.content").exists())
                .andExpect(jsonPath("$.data.content[0].title").value("Task1"))
                .andExpect(jsonPath("$.data.content[0].commentsRs[0].comment").value("Comment1"))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(1)));
    }


    @Test
    void testSearchTasksByDescription() throws Exception {
        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put("description", "descr");
        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("page","0");
        requestParams.add("size","2");
        requestParams.add("sort","title,asc");


        mockMvc.perform(post(baseUrl + "/task/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchCriteria))
                        .params(requestParams)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Search result"))
                .andExpect(jsonPath("$.data.content").exists())
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(2)));
    }

    @Test
    void testSearchTasksByDescriptionAndTitle() throws Exception {
        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put("title", "Task1");
        searchCriteria.put("description", "descr");
        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("page","0");
        requestParams.add("size","2");
        requestParams.add("sort","title,asc");


        mockMvc.perform(post(baseUrl + "/task/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchCriteria))
                        .params(requestParams)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Search result"))
                .andExpect(jsonPath("$.data.content").exists())
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(1)));
    }
}
