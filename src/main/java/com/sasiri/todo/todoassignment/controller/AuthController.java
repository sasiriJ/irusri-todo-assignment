package com.sasiri.todo.todoassignment.controller;

import com.sasiri.todo.todoassignment.dto.AuthResponse;
import com.sasiri.todo.todoassignment.dto.LoginRequest;
import com.sasiri.todo.todoassignment.dto.RegisterRequest;
import com.sasiri.todo.todoassignment.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        AuthResponse register = authService.register(request);
        return new ResponseEntity<>(register, HttpStatus.CREATED);
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        AuthResponse login = authService.login(request);
        return new ResponseEntity<>(login, HttpStatus.CREATED);
    }
}