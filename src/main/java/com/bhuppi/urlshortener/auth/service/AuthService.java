package com.bhuppi.urlshortener.auth.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.bhuppi.urlshortener.auth.dto.RegisterRequest;
import com.bhuppi.urlshortener.auth.dto.RegisterResponse;
import com.bhuppi.urlshortener.auth.entity.Role;
import com.bhuppi.urlshortener.auth.entity.User;
import com.bhuppi.urlshortener.auth.jwt.JwtService;
import com.bhuppi.urlshortener.auth.repository.UserRepository;
import com.bhuppi.urlshortener.exception.UserAlreadyExistsException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

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
