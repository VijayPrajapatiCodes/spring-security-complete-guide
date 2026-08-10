package com.vijay.spring_security.DTO;


import lombok.Data;

@Data
public class LoginRequest {

    private String username;
    private String password;
}