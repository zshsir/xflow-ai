package com.example.my_admin_backend.service;

import com.example.my_admin_backend.dto.AuthResponse;
import com.example.my_admin_backend.dto.LoginRequest;
import com.example.my_admin_backend.dto.RegisterRequest;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
}