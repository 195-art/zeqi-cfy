package com.example.demo.service;

import com.example.demo.dto.*;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {

    UserResponse register(@Valid RegisterRequest request);

    LoginResponse login(@Valid LoginRequest request);

    UserResponse getCurrentUser(Long userId);

    List<UserResponse> listUsers();

    UserResponse updateUsername(Long userId, @Valid UpdateUsernameRequest request);

    void updatePassword(Long userId, @Valid UpdatePasswordRequest request);

    void deleteAccount(Long userId);
}
