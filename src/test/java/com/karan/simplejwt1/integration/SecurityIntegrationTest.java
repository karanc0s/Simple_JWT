package com.karan.simplejwt1.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karan.simplejwt1.auth.jwt.JwtService;
import com.karan.simplejwt1.auth.repo.RolesRepo;
import com.karan.simplejwt1.auth.repo.TokenRepo;
import com.karan.simplejwt1.auth.repo.UserRepo;
import com.karan.simplejwt1.domain.AuthRequest;
import com.karan.simplejwt1.domain.AuthResponse;
import com.karan.simplejwt1.domain.RegisterRequest;
import com.karan.simplejwt1.domain.TokenRequest;
import com.karan.simplejwt1.entity.SimpleRole;
import com.karan.simplejwt1.entity.SimpleUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SecurityIntegrationTest {

//    @Autowired private MockMvc mockMvc;
//    @Autowired private JwtService jwtService;
//    @Autowired private UserRepo userRepo;
//    @Autowired private TokenRepo tokenRepo;
//    @Autowired private RolesRepo rolesRepo;
//    @Autowired private PasswordEncoder passwordEncoder;
//
//    private JsonMapper jsonMapper;
//
//    private SimpleRole userRole;
//
//    @BeforeEach
//    void setUp() {
//        jsonMapper = JsonMapper.builder()
//                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
//                .findAndAddModules()
//                .build();
//
//
//        tokenRepo.deleteAll();
//        userRepo.deleteAll();
//
//        // Ensure "USER" role exists
//        userRole = rolesRepo.findByRole("USER").orElseGet(() -> {
//            SimpleRole newRole = new SimpleRole();
//            newRole.setRole("USER");
//            return rolesRepo.saveAndFlush(newRole);
//        });
//    }
//
//    private void createTestUser(String username, String password) {
//        SimpleUser user = new SimpleUser();
//        user.setUsername(username);
//        user.setPassword(passwordEncoder.encode(password));
//        user.setEmail(username + "@example.com");
//        user.setEnabled(true);
//        user.setRoles(Set.of(userRole));
//        userRepo.saveAndFlush(user);
//    }
//
//    // ---------------------------------------------------------
//    // Registration Scenarios
//    // ---------------------------------------------------------
//    @Test
//    void shouldRegisterUserSuccessfully() throws Exception {
//        String username = "newuser";
//        String password = "password123";
//        String email = "newuser@example.com";
//        RegisterRequest request = new RegisterRequest(username, password, email);
//
//
//
//        mockMvc.perform(post("/auth/register")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(jsonMapper.writeValueAsString(request)))
//                .andExpect(status().isCreated());
//
//
//    }
//
//    void shouldFailRegistrationForDuplicateUsername() throws Exception {
//        createTestUser("duplicateuser", "password123");
//
//        RegisterRequest request = new RegisterRequest("duplicateuser", "password123", "duplicateuser@example.com");
//
//        mockMvc.perform(post("/auth/register")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(jsonMapper.writeValueAsString(request)))
//                .andExpect(status().is5xxServerError())
//                .andDo(print()) // Assuming the global exception handler maps this to 400 or 500
//                ;
//    }
//
//    // ---------------------------------------------------------
//    // Login Scenarios
//    // ---------------------------------------------------------
//    @Test
//    void shouldLoginSuccessfullyAndReturnTokens() throws Exception {
//        createTestUser("validuser", "validpassword");
//
//        AuthRequest request = new AuthRequest("validuser", "validpassword");
//
//        mockMvc.perform(post("/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(jsonMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.accessToken").exists())
//                .andExpect(jsonPath("$.refreshToken").exists())
//                .andExpect(jsonPath("$.userData.username").value("validuser"));
//    }
//
//    @Test
//    void shouldFailLoginWithInvalidCredentials() throws Exception {
//        createTestUser("wrongcreduser", "correctpassword");
//
//        AuthRequest request = new AuthRequest("wrongcreduser", "wrongpassword");
//
//        mockMvc.perform(post("/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(jsonMapper.writeValueAsString(request)))
//                .andExpect(status().isUnauthorized()); // Assuming global handler returns 401 for bad credentials
//    }
//
//    // ---------------------------------------------------------
//    // Token Refresh Scenarios
//    // ---------------------------------------------------------
//
//    void shouldRefreshTokensSuccessfully() throws Exception {
//        // 1. Create User
//        createTestUser("refreshuser", "password123");
//
//        // 2. Login to get tokens
//        AuthRequest loginRequest = new AuthRequest("refreshuser", "password123");
//        MvcResult loginResult = mockMvc.perform(post("/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(jsonMapper.writeValueAsString(loginRequest)))
//                .andExpect(status().isOk())
//                .andReturn();
//
//        AuthResponse authResponse = jsonMapper.readValue(loginResult.getResponse().getContentAsString(), AuthResponse.class);
//        String oldRefreshToken = authResponse.refreshToken();
//
//        // 3. Refresh token
//        TokenRequest refreshRequest = new TokenRequest(oldRefreshToken);
//        mockMvc.perform(post("/auth/refresh")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(jsonMapper.writeValueAsString(refreshRequest)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.accessToken").exists())
//                .andExpect(jsonPath("$.refreshToken").exists());
//    }
//
//    @Test
//    void shouldFailRefreshWithInvalidToken() throws Exception {
//        TokenRequest refreshRequest = new TokenRequest("invalid.refresh.token");
//
//        mockMvc.perform(post("/auth/refresh")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(jsonMapper.writeValueAsString(refreshRequest)))
//                .andExpect(status().isUnauthorized());
//    }
//
//    // ---------------------------------------------------------
//    // Authorization Scenarios (Secure Endpoints)
//    // ---------------------------------------------------------
//    @Test
//    void shouldAllowAccessWithValidAccessToken() throws Exception {
//        createTestUser("secureuser", "password123");
//
//        // Generate Access Token Directly
//        String accessToken = jwtService.generateToken("secureuser", List.of("USER"));
//
//        mockMvc.perform(get("/home/secure")
//                        .header("Authorization", "Bearer " + accessToken))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    void shouldRejectAccessWithoutToken() throws Exception {
//        mockMvc.perform(get("/home/secure"))
//                .andExpect(status().isForbidden());
//    }
//
//    @Test
//    void shouldRejectAccessWithInvalidToken() throws Exception {
//        mockMvc.perform(get("/home/secure")
//                        .header("Authorization", "Bearer invalid.access.token"))
//                .andExpect(status().isForbidden());
//    }
//
//    @Test
//    void shouldRejectAccessWithRefreshToken() throws Exception {
//        createTestUser("refreshonly", "password");
//        String refreshToken = jwtService.generateRefreshToken("refreshonly");
//
//        mockMvc.perform(get("/home/secure")
//                        .header("Authorization", "Bearer " + refreshToken))
//                .andExpect(status().isUnauthorized());
//    }
}
