package com.tallertech.backend.controller;

import com.tallertech.backend.dto.*;
import com.tallertech.backend.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientController {
    private final ClientService service;

    public ClientController(ClientService service) { this.service = service; }

    @GetMapping
    public List<ClientResponse> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public ClientResponse getById(@PathVariable String id) { return service.getById(id); }

    @PostMapping
    public ResponseEntity<ClientResponse> create(@Valid @RequestBody ClientRequest request) {
        var result = service.create(request);
        return ResponseEntity.created(URI.create("/api/clients/" + result.id())).body(result);
    }

    @PutMapping("/{id}")
    public ClientResponse update(@PathVariable String id, @Valid @RequestBody ClientRequest request) {
        return service.update(id, request);
    }

}

