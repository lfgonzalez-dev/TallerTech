package com.tallertech.backend.repository;

import com.tallertech.backend.entity.Client;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Client e where e.id = :id")
    Optional<Client> findLockedById(@Param("id") String id);

}

