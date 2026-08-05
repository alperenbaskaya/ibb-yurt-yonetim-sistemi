package com.ibb.yurtlar.controller;

import com.ibb.yurtlar.dto.CreateUserRequest;
import com.ibb.yurtlar.dto.UserResponse;
import com.ibb.yurtlar.service.AppUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.ibb.yurtlar.dto.UpdateUserRequest;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class AppUserController {

    private final AppUserService appUserService;

    public AppUserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(
            @Valid @RequestBody CreateUserRequest request
    ) {
        return appUserService.create(request);
    }

    @GetMapping
    public List<UserResponse> getAll() {
        return appUserService.getAll();
    }

    @GetMapping("/{id}")
    public UserResponse getById(
            @PathVariable Long id
    ) {
        return appUserService.getById(id);
    }

    @PutMapping("/{id}")
    public UserResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return appUserService.update(id, request);
    }
}