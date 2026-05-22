package com.project.artconnect.modules.auth.service;

import com.project.artconnect.common.enums.UserRole;
import com.project.artconnect.modules.auth.dto.AuthResponse;
import com.project.artconnect.modules.auth.dto.LoginRequest;
import com.project.artconnect.modules.auth.dto.RefreshTokenRequest;
import com.project.artconnect.modules.auth.dto.RegisterRequest;
import com.project.artconnect.modules.users.entity.User;
import com.project.artconnect.modules.users.repository.UserRepository;
import com.project.artconnect.modules.users.service.UserService;
import com.project.artconnect.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .displayName(request.getDisplayName())
                .roles(new HashSet<>(Set.of(UserRole.CUSTOMER)))
                .build();

        user = userRepository.save(user);
        log.info("Registered new user: {}", user.getEmail());

        String accessToken = jwtService.generateAccessTokenWithUserId(user, user.getId());
        String refreshToken = jwtService.generateRefreshTokenWithUserId(user, user.getId());

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + request.getEmail()));

        String accessToken = jwtService.generateAccessTokenWithUserId(user, user.getId());
        String refreshToken = jwtService.generateRefreshTokenWithUserId(user, user.getId());

        log.info("User logged in: {}", user.getEmail());
        return buildAuthResponse(user, accessToken, refreshToken);
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String token = request.getRefreshToken();
        if (!jwtService.validateToken(token)) {
            throw new RuntimeException("Invalid or expired refresh token");
        }
        String email = jwtService.extractUsername(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        String newAccessToken = jwtService.generateAccessTokenWithUserId(user, user.getId());
        String newRefreshToken = jwtService.generateRefreshTokenWithUserId(user, user.getId());

        return buildAuthResponse(user, newAccessToken, newRefreshToken);
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration())
                .user(UserService.mapToDto(user))
                .build();
    }
}

