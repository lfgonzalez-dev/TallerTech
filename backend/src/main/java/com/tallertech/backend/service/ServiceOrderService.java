package com.tallertech.backend.service;

import com.tallertech.backend.dto.*;
import java.util.List;

public interface ServiceOrderService {
    List<ServiceOrderResponse> getAll();
    ServiceOrderResponse getById(String id);
    ServiceOrderResponse create(ServiceOrderRequest request);
    ServiceOrderResponse update(String id, ServiceOrderRequest request);
    List<ServiceOrderResponse> byClient(String clientId);
    List<ServiceOrderResponse> byEquipment(String equipmentId);
}

