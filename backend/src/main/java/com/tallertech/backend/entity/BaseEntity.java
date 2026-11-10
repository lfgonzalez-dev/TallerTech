package com.tallertech.backend.entity;

import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;

@MappedSuperclass
public abstract class BaseEntity implements Persistable<String> {
    @Id
    @Column(length = 36, nullable = false, updatable = false)
    private String id;

    @Column(nullable = false)
    private long updatedAt;

    @Transient
    private boolean newEntity = true;

    // Assigned UUIDs must use INSERT on POST, never merge an existing record.
    @Override
    public boolean isNew() { return newEntity; }

    @PostLoad
    @PostPersist
    private void markPersisted() { newEntity = false; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}

