package com.tallertech.backend.repository;

import com.tallertech.backend.entity.ServiceOrder;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface ServiceOrderRepository extends JpaRepository<ServiceOrder, String> {
    boolean existsByCodeAndIdNot(String value, String id);
    Optional<ServiceOrder> findByCode(String code);
    List<ServiceOrder> findByClientId(String clientId);
    List<ServiceOrder> findByEquipmentId(String equipmentId);
    boolean existsByEquipmentId(String equipmentId);
}

