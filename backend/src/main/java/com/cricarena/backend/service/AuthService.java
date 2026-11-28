package com.cricarena.backend.service;

import com.cricarena.backend.dto.request.LoginRequest;
import com.cricarena.backend.dto.request.RefreshTokenRequest;
import com.cricarena.backend.dto.request.RegisterRequest;
import com.cricarena.backend.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(RefreshTokenRequest request);
}

