package com.vijay.spring_security.Config;

import com.vijay.spring_security.Entity.RefreshToken;
import com.vijay.spring_security.Entity.User;
import com.vijay.spring_security.Repository.UserRepository;
import com.vijay.spring_security.Service.JwtService;
import com.vijay.spring_security.Service.RefreshTokenService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.UUID;

@Component
@AllArgsConstructor
public class OAuth2LoginSuccessHandler
        implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        // 1. Google user
        OAuth2User oauth2User =
                (OAuth2User) authentication.getPrincipal();

        // 2. Google se email
        String email =
                oauth2User.getAttribute("email");

        // 3. Database me user check
        User user =
                userRepository.findByUsername(email)
                        .orElseGet(() -> {

                            User newUser = new User();

                            newUser.setUsername(email);

                            // OAuth user ko password ki zarurat nahi,
                            // lekin current DB schema me password nullable=false hai
                            newUser.setPassword(
                                    UUID.randomUUID().toString()
                            );

                            newUser.setRole("USER");

                            return userRepository.save(newUser);
                        });

        // 4. JWT Access Token
        String accessToken =
                jwtService.generateToken(user.getUsername());

        // 5. Refresh Token
        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(user);

        // 6. Temporary response
        response.setContentType("application/json");

        response.getWriter().write(
                """
                {
                    "message": "Google Login Successful",
                    "accessToken": "%s",
                    "refreshToken": "%s"
                }
                """.formatted(
                        accessToken,
                        refreshToken.getToken()
                )
        );
    }
}