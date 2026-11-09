package com.tallertech.backend.security;

import com.tallertech.backend.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final UserRepository users;
    private final SecurityErrors errors;
    public JwtAuthenticationFilter(JwtService jwt, UserRepository users, SecurityErrors errors) {
        this.jwt = jwt; this.users = users; this.errors = errors;
    }
    @Override protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getMethod().equals("POST") && request.getServletPath().equals("/api/auth/login");
    }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                              FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null) {
            try {
                if (!header.startsWith("Bearer ")) throw new IllegalArgumentException();
                String id = jwt.userId(header.substring(7));
                var user = users.findById(id).filter(u -> u.getPasswordHash() != null).orElseThrow(IllegalArgumentException::new);
                var auth = new UsernamePasswordAuthenticationToken(user.getId(), null, List.of());
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (JwtException | IllegalArgumentException ex) {
                SecurityContextHolder.clearContext();
                errors.write(response, 401, "Sesión inválida o expirada");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
