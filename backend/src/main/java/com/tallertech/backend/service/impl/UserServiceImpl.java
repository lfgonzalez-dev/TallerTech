package com.tallertech.backend.service.impl;

import com.tallertech.backend.dto.*;
import com.tallertech.backend.entity.User;
import com.tallertech.backend.repository.*;
import com.tallertech.backend.service.*;
import com.tallertech.backend.exception.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder;

    public UserServiceImpl(UserRepository userRepository, org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAll() {
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(String id) {
        return UserResponse.from(require(id));
    }

    @Override
    public UserResponse create(UserRequest request) {
        if (request.password() == null) throw new IllegalArgumentException("La contraseña es obligatoria");
        String id = Ids.forCreate(request.id());
        if (userRepository.existsById(id)) throw new ConflictException("ID ya existe");
        var entity = new User();
        entity.setId(id);
        apply(entity, request);
        entity.setUpdatedAt(request.updatedAt() == null ? System.currentTimeMillis() : request.updatedAt());
        return UserResponse.from(userRepository.saveAndFlush(entity));
    }

    @Override
    public UserResponse update(String id, UserRequest request) {
        Ids.checkUpdate(id, request.id());
        var entity = require(id);
        apply(entity, request);
        entity.setUpdatedAt(Math.max(System.currentTimeMillis(), entity.getUpdatedAt() + 1));
        return UserResponse.from(userRepository.saveAndFlush(entity));
    }

    private User require(String id) {
        Ids.validate(id);
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User no encontrado"));
    }

    private void apply(User entity, UserRequest request) {
        if (request.email() != null && userRepository.existsByEmailAndIdNot(request.email(), entity.getId())) {
            throw new ConflictException("email ya existe");
        }
        entity.setName(request.name());
        entity.setEmail(request.email());
        String role = Roles.normalize(request.role());
        if (!role.equals("ADMIN") && !role.equals("TECNICO")) throw new IllegalArgumentException("Rol inválido");
        entity.setRole(role);
        if (request.password() != null) {
            if (request.password().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
                throw new IllegalArgumentException("La contraseña excede 72 bytes UTF-8");
            entity.setPasswordHash(encoder.encode(request.password()));
        }
    }

}

