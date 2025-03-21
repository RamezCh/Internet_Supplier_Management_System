package com.github.ramezch.backend.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getMe() throws Exception {
        mockMvc.perform(get("/api/auth/me").with(oidcLogin().userInfoToken(token -> token.claim("login", "test-user")
                        .claim("avatarUrl", "www.image.com"))))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                {
                  "username": "test-user",
                  "avatarUrl": "www.image.com",
                  "customerIds": [],
                  "role": "USER"
                }
                """));
    }
}