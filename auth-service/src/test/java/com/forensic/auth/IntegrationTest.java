package com.forensic.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forensic.auth.dto.LoginRequest;
import com.forensic.auth.dto.RegisterRequest;
import com.forensic.auth.entity.ERole;
import com.forensic.auth.entity.Role;
import com.forensic.auth.entity.User;
import com.forensic.auth.entity.UserStatus;
import com.forensic.auth.repository.RoleRepository;
import com.forensic.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class IntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Create test roles
        if (!roleRepository.findByName(ERole.ROLE_USER).isPresent()) {
            Role userRole = new Role(ERole.ROLE_USER);
            roleRepository.save(userRole);
        }
        
        if (!roleRepository.findByName(ERole.ROLE_ADMIN).isPresent()) {
            Role adminRole = new Role(ERole.ROLE_ADMIN);
            roleRepository.save(adminRole);
        }
    }

    @Test
    void testCompleteRegistrationAndLoginFlow() throws Exception {
        // Step 1: Register a new user
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("integrationtestuser");
        registerRequest.setEmail("integrationtest@example.com");
        registerRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully!"));

        // Step 2: Login with the registered user
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("integrationtestuser");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.username").value("integrationtestuser"))
                .andExpect(jsonPath("$.email").value("integrationtest@example.com"));
    }

    @Test
    void testRegistrationWithExistingUsername() throws Exception {
        // Create a user first
        User existingUser = new User();
        existingUser.setUsername("existinguser");
        existingUser.setEmail("existing@example.com");
        existingUser.setPassword(passwordEncoder.encode("password123"));
        existingUser.setStatus(UserStatus.ACTIVE);
        
        Role userRole = roleRepository.findByName(ERole.ROLE_USER).orElseThrow();
        existingUser.setRoles(Set.of(userRole));
        userRepository.save(existingUser);

        // Try to register with the same username
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("existinguser");
        registerRequest.setEmail("newemail@example.com");
        registerRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testLoginWithInvalidCredentials() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("nonexistentuser");
        loginRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testRegistrationWithInvalidData() throws Exception {
        // Test with invalid email
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("invalid-email");
        registerRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());

        // Test with short password
        registerRequest.setEmail("valid@example.com");
        registerRequest.setPassword("123");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }
}
