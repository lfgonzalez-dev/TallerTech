package com.tallertech.backend.repository;

import com.tallertech.backend.entity.User;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByEmailAndIdNot(String value, String id);
    Optional<User> findByEmail(String email);
}

