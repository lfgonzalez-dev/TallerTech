package com.tallertech.backend.repository;

import com.tallertech.backend.entity.Equipment;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface EquipmentRepository extends JpaRepository<Equipment, String> {
    boolean existsBySerialAndIdNot(String value, String id);
    List<Equipment> findByClientId(String clientId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Equipment e where e.id = :id")
    Optional<Equipment> findLockedById(@Param("id") String id);
}

