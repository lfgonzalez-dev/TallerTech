package com.tallertech.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class SecurityErrors {
    private final ObjectMapper mapper;
    public SecurityErrors(ObjectMapper mapper) { this.mapper = mapper; }
    public void write(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        mapper.writeValue(response.getOutputStream(), Map.of("status", status, "message", message));
    }
}
