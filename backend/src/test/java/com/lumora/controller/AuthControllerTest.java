package com.lumora.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lumora.dto.LoginRequest;
import com.lumora.dto.RegisterRequest;
import com.lumora.repository.RefreshTokenRepository;
import com.lumora.repository.UserProfileRepository;
import com.lumora.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM refresh_tokens");
        jdbcTemplate.execute("DELETE FROM user_profiles");
        jdbcTemplate.execute("DELETE FROM user_roles");
        jdbcTemplate.execute("DELETE FROM document_chunks");
        jdbcTemplate.execute("DELETE FROM documents");
        jdbcTemplate.execute("DELETE FROM workspaces");
        jdbcTemplate.execute("DELETE FROM users");
    }

    @Test
    void testRegisterUserAndLoginSuccess() throws Exception {
        RegisterRequest regDto = RegisterRequest.builder()
                .username("authuser")
                .email("authuser@example.com")
                .password("securepassword")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("User registered successfully")));

        LoginRequest loginDto = LoginRequest.builder()
                .username("authuser")
                .password("securepassword")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.username", is("authuser")));
    }

    @Test
    void testRegisterDuplicateUsernameReturnsBadRequest() throws Exception {
        RegisterRequest regDto = RegisterRequest.builder()
                .username("authuser")
                .email("authuser@example.com")
                .password("securepassword")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regDto)))
                .andExpect(status().isOk());

        // Attempt duplicate
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regDto)))
                .andExpect(status().isBadRequest());
    }
}
