package com.karan.simplejwt1.auth.service;

import com.karan.simplejwt1.domain.AuthRequest;
import com.karan.simplejwt1.domain.AuthResponse;
import com.karan.simplejwt1.domain.RegisterRequest;

public interface IAuthService {

    AuthResponse logIn(AuthRequest authRequest);

    AuthResponse register(RegisterRequest registerRequest);

}
