package com.tallertech.backend.service.impl;

import com.tallertech.backend.dto.*;
import com.tallertech.backend.entity.Client;
import com.tallertech.backend.repository.*;
import com.tallertech.backend.service.*;
import com.tallertech.backend.exception.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class ClientServiceImpl implements ClientService {
    private final ClientRepository clientRepository;

    public ClientServiceImpl(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientResponse> getAll() {
        return clientRepository.findAll().stream().map(ClientResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResponse getById(String id) {
        return ClientResponse.from(require(id));
    }

    @Override
    public ClientResponse create(ClientRequest request) {
        String id = Ids.forCreate(request.id());
        if (clientRepository.existsById(id)) throw new ConflictException("ID ya existe");
        var entity = new Client();
        entity.setId(id);
        apply(entity, request);
        entity.setUpdatedAt(request.updatedAt() == null ? System.currentTimeMillis() : request.updatedAt());
        return ClientResponse.from(clientRepository.saveAndFlush(entity));
    }

    @Override
    public ClientResponse update(String id, ClientRequest request) {
        Ids.checkUpdate(id, request.id());
        var entity = require(id);
        apply(entity, request);
        entity.setUpdatedAt(Math.max(System.currentTimeMillis(), entity.getUpdatedAt() + 1));
        return ClientResponse.from(clientRepository.saveAndFlush(entity));
    }

    private Client require(String id) {
        Ids.validate(id);
        return clientRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Client no encontrado"));
    }

    private void apply(Client entity, ClientRequest request) {

        entity.setName(request.name());
        entity.setPhone(request.phone());
        entity.setEmail(request.email());
        entity.setAddress(request.address());
        entity.setNotes(request.notes());
    }

}

