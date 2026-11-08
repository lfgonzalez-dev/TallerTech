package com.tallertech.backend.controller;

import com.tallertech.backend.dto.*;
import com.tallertech.backend.service.ServiceOrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class ServiceOrderController {
    private final ServiceOrderService service;

    public ServiceOrderController(ServiceOrderService service) { this.service = service; }

    @GetMapping
    public List<ServiceOrderResponse> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public ServiceOrderResponse getById(@PathVariable String id) { return service.getById(id); }

    @PostMapping
    public ResponseEntity<ServiceOrderResponse> create(@Valid @RequestBody ServiceOrderRequest request) {
        var result = service.create(request);
        return ResponseEntity.created(URI.create("/api/orders/" + result.id())).body(result);
    }

    @PutMapping("/{id}")
    public ServiceOrderResponse update(@PathVariable String id, @Valid @RequestBody ServiceOrderRequest request) {
        return service.update(id, request);
    }

    @GetMapping("/client/{clientId}")
    public List<ServiceOrderResponse> byClient(@PathVariable String clientId) {
        return service.byClient(clientId);
    }

    @GetMapping("/equipment/{equipmentId}")
    public List<ServiceOrderResponse> byEquipment(@PathVariable String equipmentId) {
        return service.byEquipment(equipmentId);
    }

}

