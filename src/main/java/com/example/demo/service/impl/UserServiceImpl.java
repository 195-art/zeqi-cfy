package com.example.demo.service.impl;

import com.example.demo.auth.TokenService;
import com.example.demo.dto.*;
import com.example.demo.entity.User;
import com.example.demo.exception.BusinessException;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Transactional
    public UserResponse register(RegisterRequest request) {

        if(userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(HttpStatus.CONFLICT, "用户名已存在");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);
        log.info("用户注册成功, userId={}, username={}", user.getId(), user.getUsername());

        return toResponse(user);
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .build();
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(()->new BusinessException(HttpStatus.NOT_FOUND,"用户不存在"));

        if(!passwordEncoder.matches(request.getPassword(),user.getPassword())) {
            log.warn("用户登录失败, username={}", request.getUsername());
            throw new BusinessException(HttpStatus.UNAUTHORIZED,"密码错误");
        }

        String token = tokenService.issueToken(user);
        log.info("用户登录成功, userId={}, username={}", user.getId(), user.getUsername());

        return new LoginResponse(token);
    }

    @Override
    @Transactional
    public UserResponse getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->new BusinessException(HttpStatus.NOT_FOUND,"用户不存在"));
        return toResponse(user);
    }

    @Override
    @Transactional
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserResponse updateUsername(Long userId, UpdateUsernameRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(()->new BusinessException(HttpStatus.NOT_FOUND,"用户不存在"));
        String oldUsername = user.getUsername();
        String newUsername = request.getUsername();
        if(!oldUsername.equals(newUsername) && userRepository.existsByUsername(newUsername)) {
            throw new BusinessException(HttpStatus.CONFLICT,"用户名已存在");
        }

        user.setUsername(newUsername);
        userRepository.save(user);

        return toResponse(user);
    }

    @Override
    @Transactional
    public void updatePassword(Long userId, UpdatePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->new BusinessException(HttpStatus.NOT_FOUND,"用户不存在"));
        if(!passwordEncoder.matches(request.getOldPassword(),user.getPassword())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED,"旧密码错误");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("修改密码成功, userId={}, username={}", user.getId(), user.getUsername());

    }

    @Override
    @Transactional
    public void deleteAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "用户不存在"));
        userRepository.delete(user);
        tokenService.revokeToken(userId);
    }
}
