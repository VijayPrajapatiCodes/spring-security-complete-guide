package com.vijay.spring_security.Controller;

import com.vijay.spring_security.DTO.AuthResponse;
import com.vijay.spring_security.DTO.LoginRequest;
import com.vijay.spring_security.DTO.RefreshTokenRequest;
import com.vijay.spring_security.Entity.RefreshToken;
import com.vijay.spring_security.Entity.User;
import com.vijay.spring_security.Repository.RefreshTokenRepository;
import com.vijay.spring_security.Repository.UserRepository;
import com.vijay.spring_security.Service.JwtService;
import com.vijay.spring_security.Service.RefreshTokenService;
import com.vijay.spring_security.Service.UserService;

import lombok.AllArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final UserRepository userRepository;

    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String profile() {

        return "Profile accessed successfully";
    }

    // =========================
    // REGISTER
    // =========================

    @PostMapping("/register")
    public User register(@RequestBody User user) {

        return userService.register(user);
    }


    // =========================
    // LOGIN
    // =========================
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
    @PostMapping("/login")
    public AuthResponse login(
            @RequestBody LoginRequest request
    ) {

        // 1. Authenticate username + password
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getUsername(),
                                request.getPassword()
                        )
                );

        // 2. Get username
        String username = authentication.getName();

        // 3. Get User from database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found"
                        )
                );

        // 4. Generate Access Token
        String accessToken =
                jwtService.generateToken(username);

        // 5. Generate Refresh Token
        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(user);

        // 6. Return both tokens
        return new AuthResponse(
                accessToken,
                refreshToken.getToken()
        );
    }


    // =========================
    // REFRESH TOKEN
    // =========================

    @PostMapping("/refresh")
    public AuthResponse refreshToken(
            @RequestBody RefreshTokenRequest request
    ) {

        RefreshToken oldToken =
                refreshTokenRepository
                        .findByToken(request.getRefreshToken())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Refresh token not found"
                                )
                        );

        // Check expiration
        refreshTokenService.verifyExpiration(oldToken);

        User user = oldToken.getUser();

        // Old refresh token revoke
        refreshTokenRepository.delete(oldToken);

        // New access token
        String accessToken =
                jwtService.generateToken(
                        user.getUsername()
                );

        // New refresh token
        RefreshToken newRefreshToken =
                refreshTokenService.createRefreshToken(user);

        return new AuthResponse(
                accessToken,
                newRefreshToken.getToken()
        );
    }
    @PostMapping("/logout")
    public String logout(
            @RequestBody RefreshTokenRequest request
    ) {

        refreshTokenService.revokeRefreshToken(
                request.getRefreshToken()
        );

        return "Logout successful";
    }
}