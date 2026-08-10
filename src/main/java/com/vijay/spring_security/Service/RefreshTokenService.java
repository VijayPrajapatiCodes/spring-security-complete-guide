package com.vijay.spring_security.Service;

import com.vijay.spring_security.Entity.RefreshToken;
import com.vijay.spring_security.Entity.User;
import com.vijay.spring_security.Repository.RefreshTokenRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@AllArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    private final long refreshTokenDurationMs =
            7 * 24 * 60 * 60 * 1000L;


    public RefreshToken createRefreshToken(User user) {

        // Check existing refresh token
        RefreshToken refreshToken =
                refreshTokenRepository
                        .findByUser(user)
                        .orElse(new RefreshToken());

        // Set user
        refreshToken.setUser(user);

        // Generate new token
        refreshToken.setToken(
                UUID.randomUUID().toString()
        );

        // Set expiry
        refreshToken.setExpiryDate(
                Instant.now()
                        .plusMillis(refreshTokenDurationMs)
        );

        // Save
        return refreshTokenRepository.save(refreshToken);
    }


    public RefreshToken verifyExpiration(
            RefreshToken refreshToken
    ) {

        if (refreshToken.getExpiryDate()
                .isBefore(Instant.now())) {

            refreshTokenRepository.delete(refreshToken);

            throw new RuntimeException(
                    "Refresh token expired"
            );
        }

        return refreshToken;
    }
    public void revokeRefreshToken(String token) {

        RefreshToken refreshToken =
                refreshTokenRepository
                        .findByToken(token)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Refresh token not found"
                                )
                        );

        refreshTokenRepository.delete(refreshToken);
    }
}