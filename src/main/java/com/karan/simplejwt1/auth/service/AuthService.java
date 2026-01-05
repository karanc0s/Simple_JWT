package com.karan.simplejwt1.auth.service;

import com.karan.simplejwt1.auth.jwt.JwtService;
import com.karan.simplejwt1.auth.repo.UserRepo;
import com.karan.simplejwt1.domain.AuthRequest;
import com.karan.simplejwt1.domain.AuthResponse;
import com.karan.simplejwt1.domain.RegisterRequest;
import com.karan.simplejwt1.domain.UserDataResponse;
import com.karan.simplejwt1.entity.SimpleUser;
import com.karan.simplejwt1.exception.InvalidCredentialsException;
import com.karan.simplejwt1.exception.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class AuthService implements IAuthService{

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepo userRepo;

    @Override
    public AuthResponse logIn(AuthRequest authRequest) {
        AuthResponse response;
        try{
            String username = authRequest.username();
            String pass = authRequest.password();
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, pass));
            SimpleUser user = userRepo.findByUsername(username)
                    .orElseThrow(() -> new NotFoundException(String.format("username : [%s] not found",username)));

            String token = jwtService.generateToken(user.getUsername());
            UserDataResponse userData = new UserDataResponse(user.getUsername() , user.getEmail(), user.getUpdatedAt());
            response = new AuthResponse(token, userData);
        } catch (BadCredentialsException e) {
            log.error("Invalid username or password, for username : {}", authRequest.username());
            throw new InvalidCredentialsException(String.format("Invalid username or password, for username : %s",authRequest.username()));
        }catch (Exception e){
            log.error("Error while logging in with username : {} \nMessage : {}", authRequest.username(), e.getMessage());
            throw new RuntimeException(String.format("Error while logging in with username : %s \n Message : %s", authRequest.username(), e.getMessage()));
        }
        log.info("Login Success for : {}", authRequest.username());
        return response;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        AuthResponse response;
        try{
            String username = request.username();
            String pass = passwordEncoder.encode(request.password());
            String email = request.email();

            SimpleUser user = new SimpleUser();
            user.setUsername(username);
            user.setPassword(pass);
            user.setEmail(email);
            user.setEnabled(true);

            userRepo.save(user);
            String token = jwtService.generateToken(user.getUsername());

            UserDataResponse userData = new UserDataResponse(user.getUsername() , user.getEmail(), user.getUpdatedAt());
            response = new AuthResponse(token, userData);
        }catch (DataIntegrityViolationException | ConstraintViolationException e){
            log.error("Something Invalid in credentials {} \n Message : {}", request.username(), e.getMessage());
            throw new InvalidCredentialsException(String.format("Error while registering with username : %s \n Message : %s", request.username(), e.getMessage()));
        }
        catch (RuntimeException e) {
            log.error("Error while registering with username : {} \n Message : {}", request.username(), e.getMessage());
            throw new RuntimeException(String.format("Error while registering with username : %s \n Message : %s", request.username(), e.getMessage()));

        }
        log.info("Registration done for : {}", request.username());
        return response;
    }
}
