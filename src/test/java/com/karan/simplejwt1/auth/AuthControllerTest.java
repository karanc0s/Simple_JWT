package com.karan.simplejwt1.auth;

import com.karan.simplejwt1.auth.service.AuthService;
import com.karan.simplejwt1.domain.AuthRequest;
import com.karan.simplejwt1.domain.AuthResponse;
import com.karan.simplejwt1.domain.RegisterRequest;
import com.karan.simplejwt1.domain.UserDataResponse;
import com.karan.simplejwt1.exception.InvalidCredentialsException;
import com.karan.simplejwt1.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void userShouldLogin() throws Exception{
        var request = AuthRequest.builder().username("user123").password("password").build();

        var response = AuthResponse.builder()
                .token("jwt-token").userData(UserDataResponse.builder()
                        .username("user123")
                        .email("user@gmail.com")
                        .build()
                ).build();

        when(authService.logIn(ArgumentMatchers.any(AuthRequest.class))).thenReturn(response);

        this.mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.userData.username").value("user123"))
                .andExpect(jsonPath("$.userData.email").value("user@gmail.com"))
                .andDo(print());

        verify(authService , times(1)).logIn(any(AuthRequest.class));
        verify(authService).logIn(
                argThat(req ->
                        req.username().equals("user123") && req.password().equals("password")
                )
        );
    }

    @Test
    public void userNotFound() throws Exception{
        var request = AuthRequest.builder().username("user123").password("password").build();

        when(authService.logIn(any(AuthRequest.class)))
                .thenThrow(new NotFoundException("User not found"));

        this.mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andDo(print());

        verify(authService, times(1)).logIn(any(AuthRequest.class));
    }

    @Test
    public void invalidCredentials() throws Exception{
        var request = AuthRequest.builder().username("user123").password("password").build();

        when(authService.logIn(any(AuthRequest.class)))
                .thenThrow(new InvalidCredentialsException("Invalid username of password"));

        this.mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andDo(print());
        verify(authService, times(1)).logIn(any(AuthRequest.class));
    }

    @Test
    public void userShouldRegister() throws Exception{
        var request = RegisterRequest.builder()
                .username("user123")
                .password("User@123")
                .email("user123@gmail.com")
                .build();
        var response = AuthResponse.builder()
                .token("jwt-token").userData(UserDataResponse.builder()
                        .username("user123")
                        .email("user123@gmail.com")
                        .build()
                ).build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        this.mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.userData.username").value("user123"))
                .andExpect(jsonPath("$.userData.email").value("user123@gmail.com"))
                .andDo(print());

        verify(authService , times(1)).register(any(RegisterRequest.class));
        verify(authService).register(
                argThat(req ->
                        req.username().equals("user123") && req.password().equals("User@123")
                )
        );
    }

}
