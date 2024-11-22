package com.example.taskmanagementsystem;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.liquibase.enabled=false"
})
class TaskManagementSystemApplicationTests {

    @Test
    void contextLoads() {
    }

}
