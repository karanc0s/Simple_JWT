package com.karan.simplejwt1.auth;

import com.karan.simplejwt1.auth.jwt.JwtService;
import com.karan.simplejwt1.auth.repo.UserRepo;
import com.karan.simplejwt1.auth.service.AuthService;
import com.karan.simplejwt1.domain.AuthRequest;
import com.karan.simplejwt1.domain.AuthResponse;
import com.karan.simplejwt1.domain.RegisterRequest;
import com.karan.simplejwt1.entity.SimpleUser;
import com.karan.simplejwt1.exception.InvalidCredentialsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private AuthService authService;


    @Test
    public void userShouldAuthenticate(){
        //given
        var request = AuthRequest.builder().username("user123").password("User@123").build();
        var authenticatedToken = new UsernamePasswordAuthenticationToken(
                request.username(),null, List.of(new SimpleGrantedAuthority("USER"))
        );
        var simpleUser = new SimpleUser();
        simpleUser.setId(1L);
        simpleUser.setEmail("user123@gmail.com");
        simpleUser.setPassword("password");
        simpleUser.setEnabled(true);
        simpleUser.setUsername("user123");
        simpleUser.setCreatedAt(Instant.now());

        // when
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authenticatedToken);
        when(jwtService.generateToken(anyString())).thenReturn("jwt-token");
        when(userRepo.findByUsername(anyString())).thenReturn(Optional.of(simpleUser));

        var response = authService.logIn(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.userData()).isNotNull();
        assertThat(response.userData().username()).isEqualTo("user123");
        assertThat(response.userData().email()).isEqualTo("user123@gmail.com");

        // verify
        verify(userRepo).findByUsername("user123");
        verify(jwtService).generateToken("user123");
        verifyNoMoreInteractions(authenticationManager, userRepo, jwtService);
        verify(authenticationManager).authenticate(
                argThat(auth ->
                        auth instanceof UsernamePasswordAuthenticationToken &&
                                Objects.equals(auth.getPrincipal(), "user123") &&
                                Objects.equals(auth.getCredentials(), "User@123")
                )
        ); // verifies semantic and existence
    }

    @Test
    public void userInvalidCredentials(){
        //given
        var request = AuthRequest.builder()
                .username("user123")
                .password("User@123")
                .build();

        // when
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("Something Invalid"));

        Exception exception = assertThrows(InvalidCredentialsException.class , ()->authService.logIn(request));

        // then
        assertThat(exception.getMessage())
                .contains("Invalid username or password")
                .contains(request.username());

        //verify
        verify(authenticationManager).authenticate(
                argThat( auth ->
                        auth instanceof UsernamePasswordAuthenticationToken &&
                                Objects.equals(auth.getPrincipal(), "user123") &&
                                Objects.equals(auth.getCredentials(), "User@123")
                )
        );
    }

    @Test
    public void shouldFailWhenRepositoryThrowsUnexpectedException(){
        //given
        var request = AuthRequest.builder()
                .username("user123")
                .password("User@123")
                .build();
        var authenticatedToken = new UsernamePasswordAuthenticationToken(
                request.username(),null, List.of(new SimpleGrantedAuthority("USER"))
        );

        // when
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authenticatedToken);
        when(userRepo.findByUsername(anyString())).thenThrow(new DataAccessResourceFailureException("DB Down"));

        Exception exception = assertThrows(RuntimeException.class , ()->authService.logIn(request));

        // then
        assertThat(exception.getMessage()).contains("Error while logging in");

        //verify
        verify(authenticationManager).authenticate(
                argThat( auth ->
                        auth instanceof UsernamePasswordAuthenticationToken &&
                                Objects.equals(auth.getPrincipal(), "user123") &&
                                Objects.equals(auth.getCredentials(), "User@123")
                )
        );
        verifyNoInteractions(jwtService);

    }

    @Test
    public void userShouldRegisterSuccessfully(){
        // given
        var request = RegisterRequest.builder()
                .username("user123")
                .password("User@123")
                .email("user123@gmail.com")
                .build();


        // when
        when(passwordEncoder.encode(request.password())).thenReturn("encoded_password");
        when(jwtService.generateToken(request.username())).thenReturn("jwt-token");
        when(userRepo.save(any(SimpleUser.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response = authService.register(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.userData()).isNotNull();
        assertThat(response.userData().username()).isEqualTo("user123");
        assertThat(response.userData().email()).isEqualTo("user123@gmail.com");


        //verify
        verify(passwordEncoder).encode("User@123");
        verify(userRepo).save(
                argThat(user ->
                        user.getUsername().equals("user123") &&
                                user.getPassword().equals("encoded_password") &&
                                user.getEmail().equals("user123@gmail.com") &&
                                user.isEnabled()
                )
        );

        verify(jwtService).generateToken("user123");
        verifyNoMoreInteractions(passwordEncoder, userRepo, jwtService);
    }

    @Test
    void shouldFailWhenUsernameAlreadyExists() {
        // given
        RegisterRequest request = RegisterRequest.builder()
                .username("user123")
                .password("User@123")
                .email("user123@gmail.com")
                .build();

        when(passwordEncoder.encode(anyString()))
                .thenReturn("encoded-password");

        when(userRepo.save(any(SimpleUser.class)))
                .thenThrow(new DataIntegrityViolationException("username already exists"));

        // when + then
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> authService.register(request)
        );

        assertThat(ex.getMessage())
                .contains("Error while registering with username")
                .contains("user123");

        // verify
        verify(passwordEncoder).encode("User@123");
        verify(userRepo).save(any(SimpleUser.class));
        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldFailWhenSomethingWentWrongWhileRegister() {
        // given
        RegisterRequest request = RegisterRequest.builder()
                .username("user123")
                .password("User@123")
                .email("user123@gmail.com")
                .build();

        when(passwordEncoder.encode(anyString()))
                .thenReturn("encoded-password");

        when(userRepo.save(any(SimpleUser.class)))
                .thenThrow(new RuntimeException("Something went wrong"));

        // when + then
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> authService.register(request)
        );

        assertThat(ex.getMessage())
                .contains("Something went wrong")
                .contains("user123");

        // verify
        verify(passwordEncoder).encode("User@123");
        verify(userRepo).save(any(SimpleUser.class));
        verifyNoInteractions(jwtService);
    }




}
