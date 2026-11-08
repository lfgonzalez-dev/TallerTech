package com.tallertech.backend.service;

import com.tallertech.backend.dto.*;
import java.util.List;

public interface ClientService {
    List<ClientResponse> getAll();
    ClientResponse getById(String id);
    ClientResponse create(ClientRequest request);
    ClientResponse update(String id, ClientRequest request);

}

