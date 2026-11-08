package com.tallertech.backend.repository;

import com.tallertech.backend.entity.Diagnosis;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface DiagnosisRepository extends JpaRepository<Diagnosis, String> {
    boolean existsByOrderIdAndIdNot(String value, String id);
    Optional<Diagnosis> findByOrderId(String orderId);
}

