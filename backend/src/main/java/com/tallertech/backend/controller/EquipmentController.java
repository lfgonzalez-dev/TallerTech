package com.tallertech.backend.controller;

import com.tallertech.backend.dto.*;
import com.tallertech.backend.service.EquipmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {
    private final EquipmentService service;

    public EquipmentController(EquipmentService service) { this.service = service; }

    @GetMapping
    public List<EquipmentResponse> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public EquipmentResponse getById(@PathVariable String id) { return service.getById(id); }

    @PostMapping
    public ResponseEntity<EquipmentResponse> create(@Valid @RequestBody EquipmentRequest request) {
        var result = service.create(request);
        return ResponseEntity.created(URI.create("/api/equipment/" + result.id())).body(result);
    }

    @PutMapping("/{id}")
    public EquipmentResponse update(@PathVariable String id, @Valid @RequestBody EquipmentRequest request) {
        return service.update(id, request);
    }

    @GetMapping("/client/{clientId}")
    public List<EquipmentResponse> byClient(@PathVariable String clientId) {
        return service.byClient(clientId);
    }

}

