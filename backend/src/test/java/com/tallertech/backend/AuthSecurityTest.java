package com.tallertech.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tallertech.backend.entity.User;
import com.tallertech.backend.repository.UserRepository;
import com.tallertech.backend.security.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class AuthSecurityTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired UserRepository users;
    @Autowired BCryptPasswordEncoder encoder;
    @Autowired JwtService jwt;
    User user;
    @Test void nonAdminCannotManageUsers() throws Exception {
        String token = jwt.issue(user);
        for (var request : List.of(get("/api/users"), get("/api/users/" + user.getId()),
                post("/api/users").contentType("application/json").content("{}"),
                put("/api/users/" + user.getId()).contentType("application/json").content("{}"))) {
            mvc.perform(request.header("Authorization", "Bearer " + token))
                    .andExpect(status().isForbidden()).andExpect(jsonPath("$.status").value(403));
        }
    }
    @Test void roleRevocationAppliesEvenToPreviouslyIssuedAdminToken() throws Exception {
        user.setRole("ADMIN"); users.flush();
        String token = jwt.issue(user);
        user.setRole("TECNICO"); users.flush();
        mvc.perform(get("/api/users").header("Authorization", "Bearer " + token)).andExpect(status().isForbidden());
    }
    @Test void adminCreatesUpdatesAndReadsUsersWithHashedPassword() throws Exception {
        user.setRole("ADMIN"); users.flush();
        String token = jwt.issue(user);
        var body = new HashMap<String, Object>(Map.of("name", "Nuevo técnico", "email", "new@example.test",
                "password", "test-only-password", "role", "TECNICO"));
        String json = mvc.perform(post("/api/users").header("Authorization", "Bearer " + token)
                .contentType("application/json").content(mapper.writeValueAsString(body)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist()).andReturn().getResponse().getContentAsString();
        String id = mapper.readTree(json).get("id").asText();
        String hash = users.findById(id).orElseThrow().getPasswordHash();
        assertThat(hash).isNotEqualTo(body.get("password"));
        assertThat(encoder.matches("test-only-password", hash)).isTrue();
        body.remove("password"); body.put("name", "Nombre editado");
        mvc.perform(put("/api/users/" + id).header("Authorization", "Bearer " + token)
                .contentType("application/json").content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Nombre editado"));
        assertThat(users.findById(id).orElseThrow().getPasswordHash()).isEqualTo(hash);
        for (String path : List.of("/api/users", "/api/users/" + id)) {
            String response = mvc.perform(get(path).header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            assertThat(response).doesNotContain("password", hash, "test-only-password");
        }
    }
    @Test void adminCannotCreateUserWithoutPasswordOrInvalidRole() throws Exception {
        user.setRole("ADMIN"); users.flush();
        var body = new HashMap<String, Object>(Map.of("name", "User", "email", "invalid@example.test", "role", "ADMIN"));
        mvc.perform(post("/api/users").header("Authorization", "Bearer " + jwt.issue(user))
                .contentType("application/json").content(mapper.writeValueAsString(body))).andExpect(status().isBadRequest());
        body.put("password", "test-only-password"); body.put("role", "SUPERUSER");
        mvc.perform(post("/api/users").header("Authorization", "Bearer " + jwt.issue(user))
                .contentType("application/json").content(mapper.writeValueAsString(body))).andExpect(status().isBadRequest());
    }
    @BeforeEach void setup() {
        user = new User(); user.setId(UUID.randomUUID().toString()); user.setName("Test");
        user.setEmail("auth@example.test"); user.setRole("Técnico");
        user.setPasswordHash(encoder.encode("test-only-password")); users.saveAndFlush(user);
    }
    private org.springframework.test.web.servlet.ResultActions login(String email, String password) throws Exception {
        return mvc.perform(post("/api/auth/login").contentType("application/json")
                .content(mapper.writeValueAsString(Map.of("email", email, "password", password))));
    }
    @Test void loginAndUseIssuedToken() throws Exception {
        String json = login(user.getEmail(), "test-only-password").andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600000)).andReturn().getResponse().getContentAsString();
        String token = mapper.readTree(json).get("token").asText();
        mvc.perform(get("/api/clients").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
        assertThat(json).doesNotContain("passwordHash", "test-only-password", user.getPasswordHash());
    }
    @Test void wrongAndUnknownCredentialsHaveSameError() throws Exception {
        String first = login(user.getEmail(), "wrong").andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();
        String second = login("absent@example.test", "wrong").andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();
        assertThat(first).isEqualTo(second);
    }
    @Test void allResourcesRequireToken() throws Exception {
        for (String path : List.of("clients", "equipment", "orders", "diagnoses", "repairs", "users", "auth/me"))
            mvc.perform(get("/api/" + path)).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401));
    }
    @Test void malformedTokenRejected() throws Exception {
        mvc.perform(get("/api/clients").header("Authorization", "Bearer invalid")).andExpect(status().isUnauthorized());
    }
    @Test void tamperedAndWrongSchemeTokensRejected() throws Exception {
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer x" + jwt.issue(user)))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/clients").header("Authorization", "Basic ignored"))
                .andExpect(status().isUnauthorized());
    }
    @Test void forbiddenErrorUsesJsonWithoutDetails() throws Exception {
        var response = new org.springframework.mock.web.MockHttpServletResponse();
        new SecurityErrors(mapper).write(response, 403, "Acceso denegado");
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat(mapper.readTree(response.getContentAsString()).get("message").asText()).isEqualTo("Acceso denegado");
    }
    @Test void invalidLoginBodyReturns400AndDoesNotExposePassword() throws Exception {
        String response = login("not-an-email", "test-only-sensitive").andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
        assertThat(response).doesNotContain("test-only-sensitive", "passwordHash");
    }
    @Test void bootstrapDisabledAndPartialConfiguration() {
        long count = users.count();
        new BootstrapUser(users, encoder, "", "", "", "").run();
        assertThat(users.count()).isEqualTo(count);
        assertThatThrownBy(() -> new BootstrapUser(users, encoder, "Name", "", "", "").run())
                .isInstanceOf(IllegalArgumentException.class);
    }
    @Test void expiredTokenRejected() throws Exception {
        var expired = new JwtService("dGVzdC1vbmx5LWtleS0zMi1ieXRlcy1taW5pbXVtLWF1dGgtdGVzdHM=", 1);
        String token = expired.issue(user); Thread.sleep(20);
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token)).andExpect(status().isUnauthorized());
    }
    @Test void meAndUsersNeverExposeHash() throws Exception {
        user.setRole("ADMIN"); users.flush();
        for (String path : List.of("/api/auth/me", "/api/users/" + user.getId())) {
            mvc.perform(get(path).header("Authorization", "Bearer " + jwt.issue(user))).andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(user.getId())).andExpect(jsonPath("$.passwordHash").doesNotExist())
                    .andExpect(jsonPath("$.password").doesNotExist());
        }
        assertThat(mapper.writeValueAsString(user)).doesNotContain("passwordHash", user.getPasswordHash());
    }
    @Test void removedUserCannotUseToken() throws Exception {
        String token = jwt.issue(user); users.delete(user); users.flush();
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token)).andExpect(status().isUnauthorized());
    }
    @Test void bootstrapIsIdempotent() {
        var bootstrap = new BootstrapUser(users, encoder, "Bootstrap", "bootstrap@example.test", "test-only-bootstrap", "Técnico");
        bootstrap.run(); String hash = users.findByEmail("bootstrap@example.test").orElseThrow().getPasswordHash();
        bootstrap.run();
        assertThat(users.findAll().stream().filter(u -> "bootstrap@example.test".equals(u.getEmail())).count()).isEqualTo(1);
        assertThat(users.findByEmail("bootstrap@example.test").orElseThrow().getPasswordHash()).isEqualTo(hash);
        assertThat(encoder.matches("test-only-bootstrap", hash)).isTrue();
    }
    @Test void legacyPublicUserCannotLogin() throws Exception {
        user.setPasswordHash(null); users.flush();
        login(user.getEmail(), "test-only-password").andExpect(status().isUnauthorized());
    }
}
