package com.microblog.services;

import com.microblog.dto.request.LoginRequest;
import com.microblog.dto.request.RegisterRequest;
import com.microblog.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
