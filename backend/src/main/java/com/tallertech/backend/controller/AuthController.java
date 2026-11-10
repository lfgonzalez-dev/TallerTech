package com.tallertech.backend.controller;

import com.tallertech.backend.dto.UserResponse;
import com.tallertech.backend.repository.UserRepository;
import com.tallertech.backend.security.JwtService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    public record LoginRequest(@NotBlank @Email @Size(max=255) String email, @NotBlank @Size(max=256) String password) {
        @Override public String toString() { return "LoginRequest[REDACTED]"; }
    }
    public record LoginResponse(String token, String tokenType, long expiresIn, UserResponse user) {
        @Override public String toString() { return "LoginResponse[token=REDACTED]"; }
    }
    private final UserRepository users;
    private final JwtService jwt;
    private final org.springframework.security.authentication.AuthenticationManager authenticationManager;
    public AuthController(UserRepository users, JwtService jwt,
            org.springframework.security.authentication.AuthenticationManager authenticationManager) {
        this.users = users; this.jwt = jwt;
        this.authenticationManager = authenticationManager;
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            if (request.password().getBytes(StandardCharsets.UTF_8).length > 72)
                throw new org.springframework.security.authentication.BadCredentialsException("Credenciales inválidas");
            authenticationManager.authenticate(org.springframework.security.authentication.UsernamePasswordAuthenticationToken
                    .unauthenticated(request.email().trim(), request.password()));
        } catch (org.springframework.security.core.AuthenticationException ex) {
            return ResponseEntity.status(401).body(java.util.Map.of("status", 401, "message", "Credenciales inválidas"));
        }
        var user = users.findByEmail(request.email().trim()).orElseThrow();
        return ResponseEntity.ok(new LoginResponse(jwt.issue(user), "Bearer", jwt.getExpiration(), UserResponse.from(user)));
    }
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Principal principal) {
        return users.findById(principal.getName()).map(u -> ResponseEntity.ok(UserResponse.from(u)))
                .orElseGet(() -> ResponseEntity.status(401).build());
    }
}
