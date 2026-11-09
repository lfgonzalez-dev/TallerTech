package com.tallertech.app.data.sync.mapper;

import com.tallertech.app.data.local.entity.ClientEntity;
import com.tallertech.app.data.remote.dto.clients.*;
import com.tallertech.app.data.sync.SyncStatus;

public final class ClientMapper {
    private ClientMapper() {}
    public static ClientRequest request(ClientEntity entity) {
        ClientRequest dto = new ClientRequest();
        dto.id = entity.getId();
        dto.name = entity.getName();
        dto.phone = entity.getPhone();
        dto.email = entity.getEmail();
        dto.address = entity.getAddress();
        dto.notes = entity.getNotes();
        dto.updatedAt = entity.getUpdatedAt();
        return dto;
    }
    public static ClientEntity entity(ClientResponse dto) {
        if (dto.id == null || dto.updatedAt < 0)
            throw new IllegalArgumentException("Respuesta remota inválida: Client");
        java.util.UUID.fromString(dto.id);
        return new ClientEntity(dto.id, dto.name, dto.phone, dto.email, dto.address, dto.notes, dto.updatedAt, SyncStatus.SYNCED);
    }
}
