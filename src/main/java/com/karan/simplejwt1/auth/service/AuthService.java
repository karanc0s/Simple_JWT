package com.karan.simplejwt1.auth.service;

import com.karan.simplejwt1.auth.jwt.JwtService;
import com.karan.simplejwt1.auth.repo.RolesRepo;
import com.karan.simplejwt1.auth.repo.TokenRepo;
import com.karan.simplejwt1.auth.repo.UserRepo;
import com.karan.simplejwt1.domain.*;
import com.karan.simplejwt1.entity.SimpleRole;
import com.karan.simplejwt1.entity.SimpleToken;
import com.karan.simplejwt1.entity.SimpleUser;
import com.karan.simplejwt1.exception.InvalidCredentialsException;
import com.karan.simplejwt1.exception.NotFoundException;
import io.jsonwebtoken.MalformedJwtException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@AllArgsConstructor
public class AuthService implements IAuthService{

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepo userRepo;
    private final RolesRepo rolesRepo;
    private final TokenRepo tokenRepo;

    @Override
    @Transactional
    public AuthResponse logIn(AuthRequest authRequest) {
        AuthResponse response;
        try{
            String username = authRequest.username();
            String pass = authRequest.password();
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, pass));

            SimpleUser user = userRepo.findByUsername(username)
                    .orElseThrow(() -> new NotFoundException(String.format("username : [%s] not found",username)));

            List<String> userRoles = user.getRoles().stream()
                    .map(SimpleRole::getRole)
                    .toList();

            String accessToken = jwtService.generateToken(user.getUsername(), userRoles);
            String refreshToken = jwtService.generateRefreshToken(user.getUsername());

            // save entity : simple user -< simple token (1:M)
            /* 2 Approaches
             * 1. Save the child entity by setting foreign key
             * 2. Use Helper method in entity - That depends totally on Cascadel.ALL
             *
             */
            SimpleToken simpleToken = new SimpleToken();
            simpleToken.setToken(refreshToken);
            simpleToken.setUser(user);  // foreign Key
            simpleToken.setExpiresAt(jwtService.extractExpiration(refreshToken).toInstant());


            tokenRepo.save(simpleToken);

            UserDataResponse userData = new UserDataResponse(user.getUsername() , user.getEmail(), user.getUpdatedAt());
            response = new AuthResponse(accessToken, refreshToken, userData);
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
    @Transactional
    public String register(RegisterRequest request) {
        String response;
        SimpleRole userRole = rolesRepo.findByRole("USER")
                .orElseThrow(() -> new RuntimeException("Error: Default role not found in database."));
        try{
            boolean isUserExists = userRepo.findByUsername(request.username()).isPresent();
            if(isUserExists) {
                throw new RuntimeException("Invalid username");
            }
            String username = request.username();
            String pass = passwordEncoder.encode(request.password());
            String email = request.email();

            SimpleUser newUser = new SimpleUser();
            newUser.setUsername(username);
            newUser.setPassword(pass);
            newUser.setEmail(email);
            newUser.setEnabled(true);

            // Because of  @JoinTable mapping in SimpleUser, Hibernate knows exactly what to do here.
            newUser.setRoles(Set.of(userRole));

            // 6. Save the user to the database
            // Hibernate will execute TWO SQL statements here:
            // INSERT INTO simple_user (...)
            // INSERT INTO simple_user_roles (user_id, role_id) VALUES (...)
            userRepo.save(newUser);

            response = newUser.getUsername();
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

    @Override
    @Transactional
    public TokenResponse refreshToken(TokenRequest tokenRequest) {
        log.info("Attempting to refresh token...");

        try {
            String oldRefreshToken = tokenRequest.token();

            // 1. Extract and validate payload
            String userName = jwtService.extractUserName(oldRefreshToken);
            if (userName == null) {
                throw new RuntimeException("Invalid Refresh Token: Subject is missing.");
            }

            // 2. Fetch User and Token from DB
            SimpleUser user = userRepo.findByUsername(userName)
                    .orElseThrow(() -> new NotFoundException("User not found for this token."));

            SimpleToken dbToken = tokenRepo.findByToken(oldRefreshToken)
                    .orElseThrow(() -> new RuntimeException("Refresh Token does not exist. Login Again"));

            // 3. Security & Expiration Checks
            if (dbToken.getExpiresAt().isBefore(Instant.now())) {
                tokenRepo.delete(dbToken); // Clean up dead tokens
                throw new RuntimeException("Refresh token has expired. Please log in again.");
            }
            if (dbToken.getIsRevoked()) {
                throw new RuntimeException("Refresh token has been revoked. Please log in again.");
            }
            if (!user.isEnabled()) {
                throw new RuntimeException("User account is disabled.");
            }

            // 4. Fetch up-to-date roles from DB
            List<String> roles = user.getRoles().stream()
                    .map(SimpleRole::getRole)
                    .toList();

            // 5. Generate new tokens
            String newAccessToken = jwtService.generateToken(user.getUsername(), roles);
            String newRefreshToken = jwtService.generateRefreshToken(user.getUsername());

            // 6. Token Rotation (Delete old, save new)
            tokenRepo.delete(dbToken);

            SimpleToken newSimpleToken = new SimpleToken();
            newSimpleToken.setToken(newRefreshToken); // Add passwordEncoder.encode() here if you are hashing them!
            newSimpleToken.setUser(user);
            newSimpleToken.setExpiresAt(jwtService.extractExpiration(newRefreshToken).toInstant());
            tokenRepo.save(newSimpleToken);

            log.info("Successfully refreshed tokens for user: {}", userName);

            return TokenResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .build();

        }catch (MalformedJwtException e){
            log.error("Invalid JWT token : {}", e.getMessage());
            throw new MalformedJwtException(String.format("Invalid JWT token : %s", e.getMessage()));
        }
        catch (RuntimeException e) {
            log.error("Token refresh failed: {}", e.getMessage());
            throw new RuntimeException(String.format("Token refresh failed: %s", e.getMessage()));
        }
    }
}
