package com.sasiri.todo.todoassignment.service;

import com.sasiri.todo.todoassignment.dto.AuthResponse;
import com.sasiri.todo.todoassignment.dto.ChangePasswordRequest;
import com.sasiri.todo.todoassignment.dto.LoginRequest;
import com.sasiri.todo.todoassignment.dto.RegisterRequest;
import com.sasiri.todo.todoassignment.entity.User;
import com.sasiri.todo.todoassignment.exception.AuthenticationFailedException;
import com.sasiri.todo.todoassignment.exception.UserAlreadyExistsException;
import com.sasiri.todo.todoassignment.repository.UserRepository;
import com.sasiri.todo.todoassignment.security.JwtUtil;
import com.sasiri.todo.todoassignment.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Processing registration for email: {}", request.getEmail());

        // Check if user already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.error("Email already registered: {}", request.getEmail());
            throw new UserAlreadyExistsException("Email already registered");
        }

        // Create new user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());

        // Save user
        user = userRepository.save(user);
        log.info("User registered successfully: {}", user.getEmail());

        // Generate JWT token
        UserPrincipal userPrincipal = customUserDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userPrincipal);

        // Return response
        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Processing login for email: {}", request.getEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
                )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Get user details
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            User user = userRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new AuthenticationFailedException("User not found"));

            // Generate JWT token
            String token = jwtUtil.generateToken(principal);

            log.info("User logged in successfully: {}", user.getEmail());

            return AuthResponse.builder()
                    .token(token)
                    .email(user.getEmail())
                    .name(user.getName())
                    .build();

        } catch (Exception e) {
            log.error("Authentication failed for email: {}", request.getEmail(), e);
            throw new AuthenticationFailedException("Invalid email or password");
        }
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationFailedException("No authenticated user found");
        }

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        return userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new AuthenticationFailedException("User not found"));
    }

    @Transactional
    public AuthResponse refreshToken(String token) {
        log.info("Processing token refresh");

        if (!jwtUtil.validateToken(token)) {
            throw new AuthenticationFailedException("Invalid token");
        }

        String email = jwtUtil.extractUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        String newToken = jwtUtil.refreshToken(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationFailedException("User not found"));

        return AuthResponse.builder()
                .token(newToken)
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User currentUser = getCurrentUser();
        
        // Verify old password
        if (!passwordEncoder.matches(request.getOldPassword(), currentUser.getPassword())) {
            throw new AuthenticationFailedException("Invalid old password");
        }

        // Update password
        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(currentUser);
        
        log.info("Password changed successfully for user: {}", currentUser.getEmail());
    }

    @Transactional
    public void logout() {
        // You might want to implement token blacklisting here
        SecurityContextHolder.clearContext();
    }
}