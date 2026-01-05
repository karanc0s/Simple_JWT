package com.karan.simplejwt1.auth;


import com.karan.simplejwt1.auth.service.IAuthService;
import com.karan.simplejwt1.domain.AuthRequest;
import com.karan.simplejwt1.domain.AuthResponse;
import com.karan.simplejwt1.domain.RegisterRequest;
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
        var response = iAuthService.logIn(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request){
        var response = iAuthService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
