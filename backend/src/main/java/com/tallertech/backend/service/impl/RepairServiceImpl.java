package com.tallertech.backend.service.impl;

import com.tallertech.backend.dto.*;
import com.tallertech.backend.entity.Repair;
import com.tallertech.backend.repository.*;
import com.tallertech.backend.service.*;
import com.tallertech.backend.exception.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class RepairServiceImpl implements RepairService {
    private final RepairRepository repairRepository;
    private final ServiceOrderRepository serviceOrderRepository;

    public RepairServiceImpl(RepairRepository repairRepository, ServiceOrderRepository serviceOrderRepository) {
        this.repairRepository = repairRepository;
        this.serviceOrderRepository = serviceOrderRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RepairResponse> getAll() {
        return repairRepository.findAll().stream().map(RepairResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RepairResponse getById(String id) {
        return RepairResponse.from(require(id));
    }

    @Override
    public RepairResponse create(RepairRequest request) {
        String id = Ids.forCreate(request.id());
        if (repairRepository.existsById(id)) throw new ConflictException("ID ya existe");
        var entity = new Repair();
        entity.setId(id);
        apply(entity, request);
        entity.setUpdatedAt(request.updatedAt() == null ? System.currentTimeMillis() : request.updatedAt());
        return RepairResponse.from(repairRepository.saveAndFlush(entity));
    }

    @Override
    public RepairResponse update(String id, RepairRequest request) {
        Ids.checkUpdate(id, request.id());
        var entity = repairRepository.findLockedById(id).orElseThrow(() -> new ResourceNotFoundException("Registro no encontrado"));
        if (!LastWriteWins.accepts(request.updatedAt(), entity.getUpdatedAt())) return RepairResponse.from(entity);
        apply(entity, request);
        entity.setUpdatedAt(LastWriteWins.timestamp(request.updatedAt(), entity.getUpdatedAt()));
        return RepairResponse.from(repairRepository.saveAndFlush(entity));
    }

    private Repair require(String id) {
        Ids.validate(id);
        return repairRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Repair no encontrado"));
    }

    private void apply(Repair entity, RepairRequest request) {
        if (!serviceOrderRepository.existsById(request.orderId())) throw new ResourceNotFoundException("Orden no encontrada");
        if (request.orderId() != null && repairRepository.existsByOrderIdAndIdNot(request.orderId(), entity.getId())) {
            throw new ConflictException("orderId ya existe");
        }
        entity.setOrderId(request.orderId());
        entity.setRepair(request.repair());
        entity.setParts(request.parts());
        entity.setObservations(request.observations());
    }

    @Override
    @Transactional(readOnly = true)
    public RepairResponse byOrder(String orderId) {
        Ids.validate(orderId);
        if (!serviceOrderRepository.existsById(orderId)) throw new ResourceNotFoundException("Orden no encontrada");
        return RepairResponse.from(repairRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Repair no encontrado")));
    }

}

