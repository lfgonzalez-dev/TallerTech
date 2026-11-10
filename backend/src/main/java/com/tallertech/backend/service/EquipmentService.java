package com.tallertech.backend.service;

import com.tallertech.backend.dto.*;
import java.util.List;

public interface EquipmentService {
    List<EquipmentResponse> getAll();
    EquipmentResponse getById(String id);
    EquipmentResponse create(EquipmentRequest request);
    EquipmentResponse update(String id, EquipmentRequest request);
    List<EquipmentResponse> byClient(String clientId);
}

