package com.tallertech.backend.repository;

import com.tallertech.backend.entity.Repair;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface RepairRepository extends JpaRepository<Repair, String> {
    boolean existsByOrderIdAndIdNot(String value, String id);
    Optional<Repair> findByOrderId(String orderId);
}

