package com.tallertech.backend.service;

import com.tallertech.backend.dto.*;
import java.util.List;

public interface DiagnosisService {
    List<DiagnosisResponse> getAll();
    DiagnosisResponse getById(String id);
    DiagnosisResponse create(DiagnosisRequest request);
    DiagnosisResponse update(String id, DiagnosisRequest request);
    DiagnosisResponse byOrder(String orderId);
}

