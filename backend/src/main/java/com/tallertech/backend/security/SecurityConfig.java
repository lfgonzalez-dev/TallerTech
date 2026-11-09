package com.tallertech.backend.security;

import com.tallertech.backend.repository.UserRepository;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    @Bean public org.springframework.security.authentication.AuthenticationManager authenticationManager(
            org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    @Bean public org.springframework.security.core.userdetails.UserDetailsService userDetailsService(UserRepository users) {
        return email -> {
            var user = users.findByEmail(email).filter(u -> u.getPasswordHash() != null)
                    .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("Credenciales inválidas"));
            return org.springframework.security.core.userdetails.User.withUsername(user.getId())
                    .password(user.getPasswordHash()).authorities(java.util.List.of()).build();
        };
    }
    @Bean public BCryptPasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwt,
            UserRepository users, SecurityErrors errors) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .requestCache(c -> c.disable()).formLogin(f -> f.disable())
                .httpBasic(b -> b.disable()).logout(l -> l.disable())
                .authorizeHttpRequests(a -> a.requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e.authenticationEntryPoint((req, res, ex) -> errors.write(res, 401, "Autenticación requerida"))
                        .accessDeniedHandler((req, res, ex) -> errors.write(res, 403, "Acceso denegado")))
                .addFilterBefore(new JwtAuthenticationFilter(jwt, users, errors), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
