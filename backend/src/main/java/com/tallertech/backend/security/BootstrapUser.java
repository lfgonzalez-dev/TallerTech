package com.tallertech.backend.security;

import com.tallertech.backend.entity.User;
import com.tallertech.backend.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class BootstrapUser implements CommandLineRunner {
    private final UserRepository users;
    private final BCryptPasswordEncoder encoder;
    private final String name, email, password, role;
    public BootstrapUser(UserRepository users, BCryptPasswordEncoder encoder,
            @Value("${app.bootstrap.name}") String name, @Value("${app.bootstrap.email}") String email,
            @Value("${app.bootstrap.password}") String password, @Value("${app.bootstrap.role}") String role) {
        this.users = users; this.encoder = encoder; this.name = name; this.email = email;
        this.password = password; this.role = role;
    }
    @Override @Transactional public void run(String... args) {
        if (name.isBlank() && email.isBlank() && password.isBlank() && role.isBlank()) return;
        if (name.isBlank() || email.isBlank() || password.isBlank() || role.isBlank())
            throw new IllegalArgumentException("Configure all BOOTSTRAP_ADMIN variables");
        if (users.findByEmail(email.trim()).isPresent()) return;
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$") || name.length() > 255 || email.length() > 255 || role.length() > 255
                || password.length() < 12 || password.getBytes(StandardCharsets.UTF_8).length > 72)
            throw new IllegalArgumentException("Invalid bootstrap fields; password must have at least 12 characters and at most 72 UTF-8 bytes");
        User user = new User();
        user.setId(UUID.randomUUID().toString()); user.setName(name); user.setEmail(email.trim()); user.setRole(role);
        user.setPasswordHash(encoder.encode(password)); user.setUpdatedAt(System.currentTimeMillis());
        users.saveAndFlush(user);
    }
}
