package com.bhuppi.urlshortener.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.bhuppi.urlshortener.auth.dto.RegisterRequest;
import com.bhuppi.urlshortener.auth.dto.RegisterResponse;
import com.bhuppi.urlshortener.auth.entity.Role;
import com.bhuppi.urlshortener.auth.entity.User;
import com.bhuppi.urlshortener.auth.repository.UserRepository;
import com.bhuppi.urlshortener.exception.UserAlreadyExistsException;

public class AuthService {
    UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = null;

    public RegisterResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(
                    "Email already exists.");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User newUser = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(encodedPassword)
                .role(Role.USER)
                .enabled(true)
                .build();

        userRepository.save(newUser);

        return RegisterResponse.builder()
                .message("User registered successfully.")
                .build();
    }
}
