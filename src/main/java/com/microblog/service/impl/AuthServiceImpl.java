package com.microblog.service.impl;

import com.microblog.dto.AuthRequest;
import com.microblog.dto.AuthResponse;
import com.microblog.dto.LoginRequest;
import com.microblog.dto.UserDTO;
import com.microblog.entity.User;
import com.microblog.exception.UserNotFoundException;
import com.microblog.repository.UserRepository;
import com.microblog.security.JwtTokenProvider;
import com.microblog.service.AuthService;
import com.microblog.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;

    @Override
    public AuthResponse register(AuthRequest request) {
        log.info("Registering new user: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setDisplayName(request.getUsername());
        user.setLastActive(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        log.info("User saved successfully: {}", savedUser.getUsername());

        // Create authentication token for auto-login
        String jwt = tokenProvider.generateTokenForUser(savedUser);
        UserDTO userDTO = userService.convertToDTO(savedUser, savedUser.getId());

        return new AuthResponse(jwt, userDTO);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("User login attempt: {}", request.getUsernameOrEmail());

        try {
            // Find user first
            User user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail())
                    .orElseThrow(() -> new BadCredentialsException("Invalid username/email or password"));

            // Verify password
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new BadCredentialsException("Invalid username/email or password");
            }

            // Update last active time
            user.setLastActive(LocalDateTime.now());
            userRepository.save(user);

            // Generate token
            String jwt = tokenProvider.generateTokenForUser(user);
            UserDTO userDTO = userService.convertToDTO(user, user.getId());

            log.info("User logged in successfully: {}", user.getUsername());
            return new AuthResponse(jwt, userDTO);

        } catch (Exception e) {
            log.error("Login failed for user: {}", request.getUsernameOrEmail(), e);
            throw new BadCredentialsException("Invalid username/email or password");
        }
    }
}
