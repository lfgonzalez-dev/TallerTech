package com.tallertech.backend.repository;

import com.tallertech.backend.entity.Diagnosis;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface DiagnosisRepository extends JpaRepository<Diagnosis, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Diagnosis e where e.id = :id")
    Optional<Diagnosis> findLockedById(@Param("id") String id);
    boolean existsByOrderIdAndIdNot(String value, String id);
    Optional<Diagnosis> findByOrderId(String orderId);
}

