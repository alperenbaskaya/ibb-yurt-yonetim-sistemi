package com.ibb.yurtlar.controller;

import com.ibb.yurtlar.dto.CurrentUserResponse;
import com.ibb.yurtlar.dto.LoginRequest;
import com.ibb.yurtlar.dto.LoginResponse;
import com.ibb.yurtlar.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(
            AuthService authService
    ) {
        this.authService =
                authService;
    }

    // ENDPOINT ----> POST /api/auth/login
    @PostMapping("/login")
    public LoginResponse login(
            @Valid
            @RequestBody
            LoginRequest request
    ) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public CurrentUserResponse getCurrentUser(
            Authentication authentication
    ) {
        return authService.getCurrentUser(
                authentication.getName()
        );
    }
}
