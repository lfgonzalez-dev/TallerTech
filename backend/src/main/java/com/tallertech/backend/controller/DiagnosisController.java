package com.tallertech.backend.controller;

import com.tallertech.backend.dto.*;
import com.tallertech.backend.service.DiagnosisService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/diagnoses")
public class DiagnosisController {
    private final DiagnosisService service;

    public DiagnosisController(DiagnosisService service) { this.service = service; }

    @GetMapping
    public List<DiagnosisResponse> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public DiagnosisResponse getById(@PathVariable String id) { return service.getById(id); }

    @PostMapping
    public ResponseEntity<DiagnosisResponse> create(@Valid @RequestBody DiagnosisRequest request) {
        var result = service.create(request);
        return ResponseEntity.created(URI.create("/api/diagnoses/" + result.id())).body(result);
    }

    @PutMapping("/{id}")
    public DiagnosisResponse update(@PathVariable String id, @Valid @RequestBody DiagnosisRequest request) {
        return service.update(id, request);
    }

    @GetMapping("/order/{orderId}")
    public DiagnosisResponse byOrder(@PathVariable String orderId) {
        return service.byOrder(orderId);
    }

}

