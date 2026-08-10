package com.vijay.spring_security.Controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/hello")
    public String hello(){
        return "Hello World";
    }
    @GetMapping("/user")
    public String user() {
        return "Hello Authenticated User";
    }
    @GetMapping("/me")
    public String me(Authentication authentication) {

        return authentication.getName()
        + "_" +authentication.getAuthorities()+
                "_" +authentication.getCredentials()+
                "_" +authentication.getDetails()+
                "_" +authentication.getPrincipal();

    }
    @GetMapping("/mes")
    public String mes() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        return authentication.getName();
    }
}
