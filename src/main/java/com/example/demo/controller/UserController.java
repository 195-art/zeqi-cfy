package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/auth/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request){
        UserResponse user = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/auth/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request){
        return userService.login(request);
    }

    @GetMapping("/users/me")
    public UserResponse getCurrentUser(@RequestAttribute("userId") Long userId){
        return userService.getCurrentUser(userId);
    }

    @GetMapping("/users")
    public List<UserResponse> listUsers(){
        return userService.listUsers();
    }

    @PatchMapping("/users/me/username")
    public UserResponse updateUsername(@Valid @RequestBody UpdateUsernameRequest request,@RequestAttribute("userId") Long userId){
        return userService.updateUsername(userId,request);
    }

    @PatchMapping("/users/me/password")
    public ResponseEntity<Void> updatePassword(@Valid @RequestBody UpdatePasswordRequest request,@RequestAttribute("userId") Long userId){
        userService.updatePassword(userId,request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/users/me")
    public ResponseEntity<Void> deleteAccount(@RequestAttribute("userId") Long userId){
        userService.deleteAccount(userId);
        return ResponseEntity.noContent().build();
    }

}
