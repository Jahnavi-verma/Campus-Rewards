package com.campusrecycle.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEndpoint_returns200() throws Exception {
        mockMvc.perform(get("/healthz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(jsonPath("$.service").value("campus-recycle-backend"));
    }

    @Test
    void recyclingItemsEndpoint_isPublic() throws Exception {
        mockMvc.perform(get("/recycling/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.welcomeBonus").value(20));
    }

    @Test
    void protectedEndpoint_without_jwt_returns401or302() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 401 || status == 302,
                        "Expected 401 or 302 but got " + status);
                });
    }
}
