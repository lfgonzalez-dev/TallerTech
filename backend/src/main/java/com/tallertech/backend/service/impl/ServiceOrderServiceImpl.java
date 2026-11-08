package com.tallertech.backend.service.impl;

import com.tallertech.backend.dto.*;
import com.tallertech.backend.entity.ServiceOrder;
import com.tallertech.backend.repository.*;
import com.tallertech.backend.service.*;
import com.tallertech.backend.exception.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class ServiceOrderServiceImpl implements ServiceOrderService {
    private final ServiceOrderRepository serviceOrderRepository;
    private final ClientRepository clientRepository;
    private final EquipmentRepository equipmentRepository;

    public ServiceOrderServiceImpl(ServiceOrderRepository serviceOrderRepository, ClientRepository clientRepository, EquipmentRepository equipmentRepository) {
        this.serviceOrderRepository = serviceOrderRepository;
        this.clientRepository = clientRepository;
        this.equipmentRepository = equipmentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceOrderResponse> getAll() {
        return serviceOrderRepository.findAll().stream().map(ServiceOrderResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceOrderResponse getById(String id) {
        return ServiceOrderResponse.from(require(id));
    }

    @Override
    public ServiceOrderResponse create(ServiceOrderRequest request) {
        String id = Ids.forCreate(request.id());
        if (serviceOrderRepository.existsById(id)) throw new ConflictException("ID ya existe");
        var entity = new ServiceOrder();
        entity.setId(id);
        apply(entity, request);
        entity.setUpdatedAt(request.updatedAt() == null ? System.currentTimeMillis() : request.updatedAt());
        return ServiceOrderResponse.from(serviceOrderRepository.saveAndFlush(entity));
    }

    @Override
    public ServiceOrderResponse update(String id, ServiceOrderRequest request) {
        Ids.checkUpdate(id, request.id());
        var entity = require(id);
        apply(entity, request);
        entity.setUpdatedAt(Math.max(System.currentTimeMillis(), entity.getUpdatedAt() + 1));
        return ServiceOrderResponse.from(serviceOrderRepository.saveAndFlush(entity));
    }

    private ServiceOrder require(String id) {
        Ids.validate(id);
        return serviceOrderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("ServiceOrder no encontrado"));
    }

    private void apply(ServiceOrder entity, ServiceOrderRequest request) {
        if (!clientRepository.existsById(request.clientId())) throw new ResourceNotFoundException("Cliente no encontrado");
        var equipment = equipmentRepository.findLockedById(request.equipmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado"));
        if (!equipment.getClientId().equals(request.clientId())) {
            throw new ConflictException("El equipo no pertenece al cliente indicado");
        }
        if (request.code() != null && serviceOrderRepository.existsByCodeAndIdNot(request.code(), entity.getId())) {
            throw new ConflictException("code ya existe");
        }
        entity.setCode(request.code());
        entity.setClientId(request.clientId());
        entity.setEquipmentId(request.equipmentId());
        entity.setIssue(request.issue());
        entity.setEntryDate(request.entryDate());
        entity.setTechnician(request.technician());
        entity.setStatus(request.status());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceOrderResponse> byClient(String clientId) {
        Ids.validate(clientId);
        if (!clientRepository.existsById(clientId)) throw new ResourceNotFoundException("Cliente no encontrado");
        return serviceOrderRepository.findByClientId(clientId).stream().map(ServiceOrderResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceOrderResponse> byEquipment(String equipmentId) {
        Ids.validate(equipmentId);
        if (!equipmentRepository.existsById(equipmentId)) throw new ResourceNotFoundException("Equipo no encontrado");
        return serviceOrderRepository.findByEquipmentId(equipmentId).stream().map(ServiceOrderResponse::from).toList();
    }

}

