package com.vijay.spring_security.Config;

import com.vijay.spring_security.Service.CustomUserDetailsService;
import com.vijay.spring_security.Service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.AllArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@AllArgsConstructor
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader =
                request.getHeader("Authorization");

        // 1. JWT header nahi hai
        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        // 2. Bearer remove karke JWT nikalo
        String token = authHeader.substring(7);

        // 3. JWT se username nikalo
        String username =
                jwtService.extractUsername(token);

        // 4. Already authenticated nahi hai
        if (username != null &&
                SecurityContextHolder
                        .getContext()
                        .getAuthentication() == null) {

            // 5. Token validate karo
            if (jwtService.validateToken(
                    token,
                    username
            )) {

                // 6. Database se user details
                UserDetails userDetails =
                        userDetailsService
                                .loadUserByUsername(username);

                // 7. Authentication object
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                // 8. SecurityContext me authentication set
                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);
            }
        }

        // 9. Next filter
        filterChain.doFilter(request, response);
    }
}