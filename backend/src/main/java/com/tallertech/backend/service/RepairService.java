package com.tallertech.backend.service;

import com.tallertech.backend.dto.*;
import java.util.List;

public interface RepairService {
    List<RepairResponse> getAll();
    RepairResponse getById(String id);
    RepairResponse create(RepairRequest request);
    RepairResponse update(String id, RepairRequest request);
    RepairResponse byOrder(String orderId);
}

