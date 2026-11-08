package com.tallertech.backend.controller;

import com.tallertech.backend.dto.*;
import com.tallertech.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService service;

    public UserController(UserService service) { this.service = service; }

    @GetMapping
    public List<UserResponse> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable String id) { return service.getById(id); }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        var result = service.create(request);
        return ResponseEntity.created(URI.create("/api/users/" + result.id())).body(result);
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable String id, @Valid @RequestBody UserRequest request) {
        return service.update(id, request);
    }

}

