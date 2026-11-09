package com.tallertech.backend.service.impl;

import com.tallertech.backend.dto.*;
import com.tallertech.backend.entity.Equipment;
import com.tallertech.backend.repository.*;
import com.tallertech.backend.service.*;
import com.tallertech.backend.exception.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class EquipmentServiceImpl implements EquipmentService {
    private final EquipmentRepository equipmentRepository;
    private final ClientRepository clientRepository;
    private final ServiceOrderRepository serviceOrderRepository;

    public EquipmentServiceImpl(EquipmentRepository equipmentRepository, ClientRepository clientRepository, ServiceOrderRepository serviceOrderRepository) {
        this.equipmentRepository = equipmentRepository;
        this.clientRepository = clientRepository;
        this.serviceOrderRepository = serviceOrderRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipmentResponse> getAll() {
        return equipmentRepository.findAll().stream().map(EquipmentResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EquipmentResponse getById(String id) {
        return EquipmentResponse.from(require(id));
    }

    @Override
    public EquipmentResponse create(EquipmentRequest request) {
        String id = Ids.forCreate(request.id());
        if (equipmentRepository.existsById(id)) throw new ConflictException("ID ya existe");
        var entity = new Equipment();
        entity.setId(id);
        apply(entity, request);
        entity.setUpdatedAt(request.updatedAt() == null ? System.currentTimeMillis() : request.updatedAt());
        return EquipmentResponse.from(equipmentRepository.saveAndFlush(entity));
    }

    @Override
    public EquipmentResponse update(String id, EquipmentRequest request) {
        Ids.checkUpdate(id, request.id());
        var entity = equipmentRepository.findLockedById(id).orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado"));
        if (!LastWriteWins.accepts(request.updatedAt(), entity.getUpdatedAt())) return EquipmentResponse.from(entity);
        apply(entity, request);
        entity.setUpdatedAt(LastWriteWins.timestamp(request.updatedAt(), entity.getUpdatedAt()));
        return EquipmentResponse.from(equipmentRepository.saveAndFlush(entity));
    }

    private Equipment require(String id) {
        Ids.validate(id);
        return equipmentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Equipment no encontrado"));
    }

    private void apply(Equipment entity, EquipmentRequest request) {
        if (!clientRepository.existsById(request.clientId())) throw new ResourceNotFoundException("Cliente no encontrado");
        if (entity.getClientId() != null && !entity.getClientId().equals(request.clientId())
                && serviceOrderRepository.existsByEquipmentId(entity.getId())) {
            throw new ConflictException("No se puede cambiar el cliente de un equipo con órdenes");
        }
        if (request.serial() != null && equipmentRepository.existsBySerialAndIdNot(request.serial(), entity.getId())) {
            throw new ConflictException("serial ya existe");
        }
        entity.setClientId(request.clientId());
        entity.setType(request.type());
        entity.setBrand(request.brand());
        entity.setModel(request.model());
        entity.setSerial(request.serial());
        entity.setDescription(request.description());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipmentResponse> byClient(String clientId) {
        Ids.validate(clientId);
        if (!clientRepository.existsById(clientId)) throw new ResourceNotFoundException("Cliente no encontrado");
        return equipmentRepository.findByClientId(clientId).stream().map(EquipmentResponse::from).toList();
    }

}

