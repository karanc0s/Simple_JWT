package com.karan.simplejwt1.auth;


import com.karan.simplejwt1.auth.service.IAuthService;
import com.karan.simplejwt1.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    @Autowired
    private IAuthService iAuthService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> logIn(@RequestBody AuthRequest request){
        log.info("login request : {}", request);
        var response = iAuthService.logIn(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request){
        var response = iAuthService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@RequestBody TokenRequest tokenRequest){
        log.info("Refresh request : {}", tokenRequest);
        var response = iAuthService.refreshToken(tokenRequest);
        return ResponseEntity.ok(response);
    }

}
