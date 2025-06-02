package com.microblog.service;

import com.microblog.dto.AuthRequest;
import com.microblog.dto.AuthResponse;
import com.microblog.dto.LoginRequest;

public interface AuthService {
    AuthResponse register(AuthRequest request);
    AuthResponse login(LoginRequest request);
}
