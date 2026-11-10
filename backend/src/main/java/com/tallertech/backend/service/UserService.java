package com.tallertech.backend.service;

import com.tallertech.backend.dto.*;
import java.util.List;

public interface UserService {
    List<UserResponse> getAll();
    UserResponse getById(String id);
    UserResponse create(UserRequest request);
    UserResponse update(String id, UserRequest request);

}

