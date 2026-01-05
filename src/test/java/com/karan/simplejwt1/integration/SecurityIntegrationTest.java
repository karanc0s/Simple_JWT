package com.karan.simplejwt1.integration;

import com.karan.simplejwt1.auth.jwt.JwtService;
import com.karan.simplejwt1.auth.repo.UserRepo;
import com.karan.simplejwt1.entity.SimpleUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    JwtService jwtService;

    @Autowired
    UserRepo userRepo;

    @Test
    void shouldRejectRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/home/secure"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectRequestWithInvalidToken() throws Exception {
        mockMvc.perform(
                        get("/home/secure")
                                .header("Authorization", "Bearer invalid.token.value")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowRequestWithValidToken() throws Exception {
        // given: user exists in DB
        SimpleUser user = new SimpleUser();
        user.setUsername("user123");
        user.setPassword("encoded-pass");
        user.setEmail("user@gmail.com");
        user.setEnabled(true);

        userRepo.saveAndFlush(user);
        String token = jwtService.generateToken("user123");

        mockMvc.perform(
                        get("/home/secure")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk());
    }
}
