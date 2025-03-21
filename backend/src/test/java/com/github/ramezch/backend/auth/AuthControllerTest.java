package com.github.ramezch.backend.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ramezch.backend.appuser.AppUser;
import com.github.ramezch.backend.appuser.AppUserRoles;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getMe() throws Exception {
        AppUser testUser = AppUser.builder()
                .id("1234")
                .username("test-user")
                .avatarUrl("www.image.com")
                .role(AppUserRoles.USER)
                .customerIds(List.of())
                .build();

        String appUserJson = objectMapper.writeValueAsString(testUser);

        TestingAuthenticationToken authentication = new TestingAuthenticationToken(testUser, null, "ROLE_USER");

        mockMvc.perform(get("/api/auth/me")
                        .with(oidcLogin().userInfoToken(token -> token
                                .claim("login", "test-user")
                                .claim("id", "1234")
                                .claim("avatar_url", "www.image.com")
                        ))
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(content().json(appUserJson));
    }

}
