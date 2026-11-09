package com.tallertech.backend.service.impl;

import com.tallertech.backend.dto.*;
import com.tallertech.backend.entity.Diagnosis;
import com.tallertech.backend.repository.*;
import com.tallertech.backend.service.*;
import com.tallertech.backend.exception.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class DiagnosisServiceImpl implements DiagnosisService {
    private final DiagnosisRepository diagnosisRepository;
    private final ServiceOrderRepository serviceOrderRepository;

    public DiagnosisServiceImpl(DiagnosisRepository diagnosisRepository, ServiceOrderRepository serviceOrderRepository) {
        this.diagnosisRepository = diagnosisRepository;
        this.serviceOrderRepository = serviceOrderRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiagnosisResponse> getAll() {
        return diagnosisRepository.findAll().stream().map(DiagnosisResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DiagnosisResponse getById(String id) {
        return DiagnosisResponse.from(require(id));
    }

    @Override
    public DiagnosisResponse create(DiagnosisRequest request) {
        String id = Ids.forCreate(request.id());
        if (diagnosisRepository.existsById(id)) throw new ConflictException("ID ya existe");
        var entity = new Diagnosis();
        entity.setId(id);
        apply(entity, request);
        entity.setUpdatedAt(request.updatedAt() == null ? System.currentTimeMillis() : request.updatedAt());
        return DiagnosisResponse.from(diagnosisRepository.saveAndFlush(entity));
    }

    @Override
    public DiagnosisResponse update(String id, DiagnosisRequest request) {
        Ids.checkUpdate(id, request.id());
        var entity = diagnosisRepository.findLockedById(id).orElseThrow(() -> new ResourceNotFoundException("Registro no encontrado"));
        if (!LastWriteWins.accepts(request.updatedAt(), entity.getUpdatedAt())) return DiagnosisResponse.from(entity);
        apply(entity, request);
        entity.setUpdatedAt(LastWriteWins.timestamp(request.updatedAt(), entity.getUpdatedAt()));
        return DiagnosisResponse.from(diagnosisRepository.saveAndFlush(entity));
    }

    private Diagnosis require(String id) {
        Ids.validate(id);
        return diagnosisRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Diagnosis no encontrado"));
    }

    private void apply(Diagnosis entity, DiagnosisRequest request) {
        if (!serviceOrderRepository.existsById(request.orderId())) throw new ResourceNotFoundException("Orden no encontrada");
        if (request.orderId() != null && diagnosisRepository.existsByOrderIdAndIdNot(request.orderId(), entity.getId())) {
            throw new ConflictException("orderId ya existe");
        }
        entity.setOrderId(request.orderId());
        entity.setDiagnosis(request.diagnosis());
        entity.setObservations(request.observations());
        entity.setRecommendations(request.recommendations());
    }

    @Override
    @Transactional(readOnly = true)
    public DiagnosisResponse byOrder(String orderId) {
        Ids.validate(orderId);
        if (!serviceOrderRepository.existsById(orderId)) throw new ResourceNotFoundException("Orden no encontrada");
        return DiagnosisResponse.from(diagnosisRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Diagnosis no encontrado")));
    }

}

