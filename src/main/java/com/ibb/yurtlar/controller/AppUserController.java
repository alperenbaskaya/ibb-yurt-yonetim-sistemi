package com.ibb.yurtlar.controller;

import com.ibb.yurtlar.dto.CreateUserRequest;
import com.ibb.yurtlar.dto.UpdateUserRequest;
import com.ibb.yurtlar.dto.UserResponse;
import com.ibb.yurtlar.service.AppUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class AppUserController {

    private final AppUserService
            appUserService;

    public AppUserController(
            AppUserService appUserService
    ) {
        this.appUserService =
                appUserService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(
            @Valid
            @RequestBody
            CreateUserRequest request,
            Authentication authentication
    ) {
        return appUserService.create(
                request,
                authentication.getName()
        );
    }

    @GetMapping
    public List<UserResponse> getAll(
            Authentication authentication
    ) {
        return appUserService.getAll(
                authentication.getName()
        );
    }

    @GetMapping("/{id}")
    public UserResponse getById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return appUserService.getById(
                id,
                authentication.getName()
        );
    }

    @PutMapping("/{id}")
    public UserResponse update(
            @PathVariable Long id,
            @Valid
            @RequestBody
            UpdateUserRequest request,
            Authentication authentication
    ) {
        return appUserService.update(
                id,
                request,
                authentication.getName()
        );
    }
}