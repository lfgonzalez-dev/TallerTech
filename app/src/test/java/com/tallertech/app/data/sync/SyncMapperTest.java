package com.tallertech.app.data.sync;
import com.tallertech.app.data.remote.dto.clients.*;
import com.tallertech.app.data.remote.dto.equipment.*;
import com.tallertech.app.data.remote.dto.orders.*;
import com.tallertech.app.data.remote.dto.diagnoses.*;
import com.tallertech.app.data.remote.dto.repairs.*;
import com.tallertech.app.data.sync.mapper.*;
import org.junit.Test;
import static org.junit.Assert.*;
public class SyncMapperTest {
    @Test public void clientRoundTripPreservesUuidFieldsAndEpoch() {
        ClientResponse dto = new ClientResponse();
        dto.id = "550e8400-e29b-41d4-a716-446655440000"; dto.updatedAt = 1700000000000L;
        dto.name = "name-value";
        dto.phone = "phone-value";
        dto.email = "email-value";
        dto.address = "address-value";
        dto.notes = "notes-value";
        var entity = ClientMapper.entity(dto);
        var request = ClientMapper.request(entity);
        assertEquals(dto.id, entity.getId()); assertEquals(dto.id, request.id);
        assertEquals(dto.updatedAt, request.updatedAt.longValue()); assertEquals(SyncStatus.SYNCED, entity.getSyncStatus());
        assertEquals(dto.name, request.name);
        assertEquals(dto.phone, request.phone);
        assertEquals(dto.email, request.email);
        assertEquals(dto.address, request.address);
        assertEquals(dto.notes, request.notes);
    }
    @Test public void equipmentRoundTripPreservesUuidFieldsAndEpoch() {
        EquipmentResponse dto = new EquipmentResponse();
        dto.id = "550e8400-e29b-41d4-a716-446655440000"; dto.updatedAt = 1700000000000L;
        dto.clientId = "clientId-value";
        dto.type = "type-value";
        dto.brand = "brand-value";
        dto.model = "model-value";
        dto.serial = "serial-value";
        dto.description = "description-value";
        var entity = EquipmentMapper.entity(dto);
        var request = EquipmentMapper.request(entity);
        assertEquals(dto.id, entity.getId()); assertEquals(dto.id, request.id);
        assertEquals(dto.updatedAt, request.updatedAt.longValue()); assertEquals(SyncStatus.SYNCED, entity.getSyncStatus());
        assertEquals(dto.clientId, request.clientId);
        assertEquals(dto.type, request.type);
        assertEquals(dto.brand, request.brand);
        assertEquals(dto.model, request.model);
        assertEquals(dto.serial, request.serial);
        assertEquals(dto.description, request.description);
    }
    @Test public void serviceOrderRoundTripPreservesUuidFieldsAndEpoch() {
        ServiceOrderResponse dto = new ServiceOrderResponse();
        dto.id = "550e8400-e29b-41d4-a716-446655440000"; dto.updatedAt = 1700000000000L;
        dto.code = "code-value";
        dto.clientId = "clientId-value";
        dto.equipmentId = "equipmentId-value";
        dto.issue = "issue-value";
        dto.entryDate = 1600000000000L;
        dto.technician = "technician-value";
        dto.status = "status-value";
        var entity = ServiceOrderMapper.entity(dto);
        var request = ServiceOrderMapper.request(entity);
        assertEquals(dto.id, entity.getId()); assertEquals(dto.id, request.id);
        assertEquals(dto.updatedAt, request.updatedAt.longValue()); assertEquals(SyncStatus.SYNCED, entity.getSyncStatus());
        assertEquals(dto.code, request.code);
        assertEquals(dto.clientId, request.clientId);
        assertEquals(dto.equipmentId, request.equipmentId);
        assertEquals(dto.issue, request.issue);
        assertEquals(dto.entryDate, request.entryDate);
        assertEquals(dto.technician, request.technician);
        assertEquals(dto.status, request.status);
    }
    @Test public void diagnosisRoundTripPreservesUuidFieldsAndEpoch() {
        DiagnosisResponse dto = new DiagnosisResponse();
        dto.id = "550e8400-e29b-41d4-a716-446655440000"; dto.updatedAt = 1700000000000L;
        dto.orderId = "orderId-value";
        dto.diagnosis = "diagnosis-value";
        dto.observations = "observations-value";
        dto.recommendations = "recommendations-value";
        var entity = DiagnosisMapper.entity(dto);
        var request = DiagnosisMapper.request(entity);
        assertEquals(dto.id, entity.getId()); assertEquals(dto.id, request.id);
        assertEquals(dto.updatedAt, request.updatedAt.longValue()); assertEquals(SyncStatus.SYNCED, entity.getSyncStatus());
        assertEquals(dto.orderId, request.orderId);
        assertEquals(dto.diagnosis, request.diagnosis);
        assertEquals(dto.observations, request.observations);
        assertEquals(dto.recommendations, request.recommendations);
    }
    @Test public void repairRoundTripPreservesUuidFieldsAndEpoch() {
        RepairResponse dto = new RepairResponse();
        dto.id = "550e8400-e29b-41d4-a716-446655440000"; dto.updatedAt = 1700000000000L;
        dto.orderId = "orderId-value";
        dto.repair = "repair-value";
        dto.parts = "parts-value";
        dto.observations = "observations-value";
        var entity = RepairMapper.entity(dto);
        var request = RepairMapper.request(entity);
        assertEquals(dto.id, entity.getId()); assertEquals(dto.id, request.id);
        assertEquals(dto.updatedAt, request.updatedAt.longValue()); assertEquals(SyncStatus.SYNCED, entity.getSyncStatus());
        assertEquals(dto.orderId, request.orderId);
        assertEquals(dto.repair, request.repair);
        assertEquals(dto.parts, request.parts);
        assertEquals(dto.observations, request.observations);
    }
}

