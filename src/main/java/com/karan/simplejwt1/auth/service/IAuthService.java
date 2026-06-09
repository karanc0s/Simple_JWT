package com.karan.simplejwt1.auth.service;

import com.karan.simplejwt1.domain.*;

public interface IAuthService {

    AuthResponse logIn(AuthRequest authRequest);

    String register(RegisterRequest registerRequest);

    TokenResponse refreshToken(TokenRequest tokenRequest);

}
