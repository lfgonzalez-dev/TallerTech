package com.tallertech.backend.controller;

import com.tallertech.backend.dto.*;
import com.tallertech.backend.service.RepairService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/repairs")
public class RepairController {
    private final RepairService service;

    public RepairController(RepairService service) { this.service = service; }

    @GetMapping
    public List<RepairResponse> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public RepairResponse getById(@PathVariable String id) { return service.getById(id); }

    @PostMapping
    public ResponseEntity<RepairResponse> create(@Valid @RequestBody RepairRequest request) {
        var result = service.create(request);
        return ResponseEntity.created(URI.create("/api/repairs/" + result.id())).body(result);
    }

    @PutMapping("/{id}")
    public RepairResponse update(@PathVariable String id, @Valid @RequestBody RepairRequest request) {
        return service.update(id, request);
    }

    @GetMapping("/order/{orderId}")
    public RepairResponse byOrder(@PathVariable String orderId) {
        return service.byOrder(orderId);
    }

}

