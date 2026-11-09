package com.tallertech.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tallertech.backend.entity.*;
import com.tallertech.backend.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@org.springframework.security.test.context.support.WithMockUser(roles = "ADMIN")
class BackendApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired EntityManager em;
    @Autowired EquipmentRepository equipmentRepository;
    @Autowired ClientRepository clientRepository;
    @Autowired ServiceOrderRepository orderRepository;

    private static final String ABSENT = "00000000-0000-0000-0000-000000000099";

    @Test void lastWriteWinsForEveryOperationalResource() throws Exception {
        String c = client(), e = equipment(c), o = order();
        assertLastWriteWins("clients", clientBody(), "name");
        assertLastWriteWins("equipment", new HashMap<>(Map.of("clientId", c)), "brand");
        assertLastWriteWins("orders", orderBody(c, e), "issue");
        assertLastWriteWins("diagnoses", new HashMap<>(Map.of("orderId", o)), "diagnosis");
        assertLastWriteWins("repairs", new HashMap<>(Map.of("orderId", o)), "repair");
    }

    private void assertLastWriteWins(String path, Map<String, Object> body, String field) throws Exception {
        body.put("updatedAt", 1000L); body.put(field, "Original");
        String id = create(path, body).get("id").asText();
        body.put("updatedAt", 2000L); body.put(field, "Nuevo");
        mvc.perform(put("/api/" + path + "/" + id).contentType("application/json").content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk()).andExpect(jsonPath("$." + field).value("Nuevo"))
                .andExpect(jsonPath("$.updatedAt").value(2000L));
        for (long timestamp : List.of(1500L, 2000L)) {
            body.put("updatedAt", timestamp); body.put(field, "No sobrescribir");
            mvc.perform(put("/api/" + path + "/" + id).contentType("application/json").content(mapper.writeValueAsString(body)))
                    .andExpect(status().isOk()).andExpect(jsonPath("$." + field).value("Nuevo"))
                    .andExpect(jsonPath("$.updatedAt").value(2000L));
        }
    }

    private Map<String, Object> clientBody() {
        return new HashMap<>(Map.of("name", "Cliente de prueba", "phone", "70000000"));
    }

    private ResultActions send(String path, Map<String, Object> body) throws Exception {
        return mvc.perform(post("/api/" + path).contentType("application/json").content(mapper.writeValueAsString(body)));
    }

    private JsonNode create(String path, Map<String, Object> body) throws Exception {
        return mapper.readTree(send(path, body).andExpect(status().isCreated())
                .andExpect(header().exists("Location")).andReturn().getResponse().getContentAsString());
    }

    private String client() throws Exception { return create("clients", clientBody()).get("id").asText(); }

    private String equipment(String clientId) throws Exception {
        return create("equipment", Map.of("clientId", clientId, "serial", UUID.randomUUID().toString())).get("id").asText();
    }

    private Map<String, Object> orderBody(String clientId, String equipmentId) {
        return new HashMap<>(Map.of("clientId", clientId, "equipmentId", equipmentId,
                "code", "ORD-" + UUID.randomUUID(), "issue", "No enciende", "entryDate", 1700000000000L, "status", "Pendiente"));
    }

    private String order() throws Exception {
        String c = client();
        return create("orders", orderBody(c, equipment(c))).get("id").asText();
    }

    @Test void clientCreateReadUpdatePreservesUuidAndTimestamps() throws Exception {
        var body = clientBody();
        String id = "550E8400-E29B-41D4-A716-446655440000";
        body.put("id", id);
        body.put("updatedAt", 1700000000000L);
        create("clients", body);
        em.clear();
        mvc.perform(get("/api/clients/" + id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id)).andExpect(jsonPath("$.updatedAt").value(1700000000000L))
                .andExpect(jsonPath("$.syncStatus").doesNotExist());
        body.put("name", "Nombre actualizado"); body.put("updatedAt", 1700000000001L);
        mvc.perform(put("/api/clients/" + id).contentType("application/json").content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Nombre actualizado"))
                .andExpect(jsonPath("$.updatedAt").value(org.hamcrest.Matchers.greaterThan(1700000000000L)));
        mvc.perform(get("/api/clients")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(id));
    }

    @Test void duplicatePrimaryKeyDoesNotOverwriteClient() throws Exception {
        var body = clientBody();
        body.put("id", UUID.randomUUID().toString());
        create("clients", body);
        body.put("name", "No sobrescribir");
        send("clients", body).andExpect(status().isConflict());
        mvc.perform(get("/api/clients/" + body.get("id"))).andExpect(jsonPath("$.name").value("Cliente de prueba"));
    }

    @Test void equipmentRequiresExistingClientAndUniqueSerial() throws Exception {
        send("equipment", Map.of("clientId", ABSENT)).andExpect(status().isNotFound());
        String c = client();
        var body = Map.<String, Object>of("clientId", c, "serial", "SERIAL-001");
        String id = create("equipment", body).get("id").asText();
        send("equipment", body).andExpect(status().isConflict());
        mvc.perform(get("/api/equipment/client/" + c)).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(id));
        mvc.perform(get("/api/equipment/" + id)).andExpect(jsonPath("$.clientId").value(c));
    }

    @Test void optionalSerialCanBeNullForMultipleEquipment() throws Exception {
        String c = client();
        create("equipment", Map.of("clientId", c));
        create("equipment", Map.of("clientId", c));
    }

    @Test void orderRequiresMatchingOwnershipAndUniqueCode() throws Exception {
        String c = client(), other = client(), e = equipment(c);
        var body = orderBody(other, e);
        send("orders", body).andExpect(status().isConflict());
        body.put("clientId", ABSENT);
        send("orders", body).andExpect(status().isNotFound());
        body.put("clientId", c);
        body.put("equipmentId", ABSENT);
        send("orders", body).andExpect(status().isNotFound());
        body.put("equipmentId", e);
        String id = create("orders", body).get("id").asText();
        send("orders", body).andExpect(status().isConflict());
        mvc.perform(get("/api/orders/client/" + c)).andExpect(jsonPath("$[0].id").value(id));
        mvc.perform(get("/api/orders/equipment/" + e)).andExpect(jsonPath("$[0].id").value(id));
    }

    @Test void equipmentCannotChangeOwnerWhenOrdersExist() throws Exception {
        String c = client(), other = client(), e = equipment(c);
        create("orders", orderBody(c, e));
        mvc.perform(put("/api/equipment/" + e).contentType("application/json")
                .content(mapper.writeValueAsString(Map.of("clientId", other))))
                .andExpect(status().isConflict());
    }

    @Test void equipmentCanChangeOwnerWithoutOrdersAndUpdateItsOwnSerial() throws Exception {
        String c = client(), e = equipment(c), other = client();
        mvc.perform(put("/api/equipment/" + e).contentType("application/json")
                .content(mapper.writeValueAsString(Map.of("clientId", other, "serial", "UPDATED"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.clientId").value(other));
        mvc.perform(put("/api/equipment/" + e).contentType("application/json")
                .content(mapper.writeValueAsString(Map.of("clientId", other, "serial", "UPDATED"))))
                .andExpect(status().isOk());
    }

    @Test void diagnosisRequiresOrderAndIsUniquePerOrder() throws Exception {
        send("diagnoses", Map.of("orderId", ABSENT)).andExpect(status().isNotFound());
        String o = order();
        var body = Map.<String, Object>of("orderId", o, "diagnosis", "Fuente dañada");
        String id = create("diagnoses", body).get("id").asText();
        send("diagnoses", body).andExpect(status().isConflict());
        mvc.perform(get("/api/diagnoses/order/" + o)).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));
        mvc.perform(put("/api/diagnoses/" + id).contentType("application/json")
                .content(mapper.writeValueAsString(Map.of("orderId", o, "diagnosis", "Fuente comprobada"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.diagnosis").value("Fuente comprobada"));
    }

    @Test void repairRequiresOrderAndIsUniquePerOrder() throws Exception {
        send("repairs", Map.of("orderId", ABSENT)).andExpect(status().isNotFound());
        String o = order();
        var body = Map.<String, Object>of("orderId", o, "repair", "Cambio de fuente");
        String id = create("repairs", body).get("id").asText();
        send("repairs", body).andExpect(status().isConflict());
        mvc.perform(get("/api/repairs/order/" + o)).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));
        mvc.perform(put("/api/repairs/" + id).contentType("application/json")
                .content(mapper.writeValueAsString(Map.of("orderId", o, "repair", "Reparación verificada"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.repair").value("Reparación verificada"));
    }

    @Test void missingRelatedResourceQueriesReturn404() throws Exception {
        for (String path : List.of("equipment/client/", "orders/client/", "orders/equipment/", "diagnoses/order/", "repairs/order/")) {
            mvc.perform(get("/api/" + path + ABSENT)).andExpect(status().isNotFound());
        }
        String o = order();
        mvc.perform(get("/api/diagnoses/order/" + o)).andExpect(status().isNotFound());
        mvc.perform(get("/api/repairs/order/" + o)).andExpect(status().isNotFound());
    }

    @Test void missingIdsReturn404ForGetAndPut() throws Exception {
        for (String path : List.of("clients", "users", "equipment", "orders", "diagnoses", "repairs")) {
            mvc.perform(get("/api/" + path + "/" + ABSENT)).andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }
        mvc.perform(put("/api/clients/" + ABSENT).contentType("application/json")
                .content(mapper.writeValueAsString(clientBody()))).andExpect(status().isNotFound());
    }

    @Test void usersUniqueEmailUpdateAndNoPassword() throws Exception {
        var body = Map.<String, Object>of("name", "Usuario demo", "email", "demo@example.test", "role", "Técnico", "password", "test-only-password");
        String id = create("users", body).get("id").asText();
        send("users", body).andExpect(status().isConflict());
        mvc.perform(get("/api/users/" + id)).andExpect(status().isOk()).andExpect(jsonPath("$.password").doesNotExist());
        mvc.perform(put("/api/users/" + id).contentType("application/json").content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk());
        send("users", Map.of("password", "example-only")).andExpect(status().isBadRequest());
    }

    @Test void invalidInputAndMalformedJsonReturn400() throws Exception {
        send("clients", Map.of("name", "", "phone", "7000")).andExpect(status().isBadRequest());
        send("clients", Map.of("name", "Demo", "phone", "7000", "email", "invalid")).andExpect(status().isBadRequest());
        send("clients", Map.of("name", "Demo", "phone", "7000", "id", "123")).andExpect(status().isBadRequest());
        send("clients", Map.of("name", "Demo", "phone", "7000", "updatedAt", -1)).andExpect(status().isBadRequest());
        mvc.perform(post("/api/clients").contentType("application/json").content("{"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
        mvc.perform(get("/api/clients/not-a-uuid")).andExpect(status().isBadRequest());
    }

    @Test void updateCannotChangePrimaryKey() throws Exception {
        String c = client();
        var body = clientBody();
        body.put("id", UUID.randomUUID().toString());
        mvc.perform(put("/api/clients/" + c).contentType("application/json").content(mapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test void orderUpdateChecksCodeOwnershipAndStatus() throws Exception {
        String c = client(), e = equipment(c), other = client();
        var body = orderBody(c, e);
        String id = create("orders", body).get("id").asText();
        body.put("status", "Completado");
        mvc.perform(put("/api/orders/" + id).contentType("application/json").content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("Completado"));
        body.put("status", "UNKNOWN");
        mvc.perform(put("/api/orders/" + id).contentType("application/json").content(mapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
        body.put("status", "Pendiente");
        body.put("clientId", other);
        mvc.perform(put("/api/orders/" + id).contentType("application/json").content(mapper.writeValueAsString(body)))
                .andExpect(status().isConflict());
        body.put("clientId", c);
        var second = orderBody(c, e);
        create("orders", second);
        body.put("code", second.get("code"));
        mvc.perform(put("/api/orders/" + id).contentType("application/json").content(mapper.writeValueAsString(body)))
                .andExpect(status().isConflict());
    }

    @Test void databaseEnforcesUniqueCodeEvenWithoutService() throws Exception {
        String c = client(), e = equipment(c);
        var body = orderBody(c, e);
        create("orders", body);
        var duplicate = new ServiceOrder();
        duplicate.setId(UUID.randomUUID().toString());
        duplicate.setUpdatedAt(1);
        duplicate.setClientId(c);
        duplicate.setEquipmentId(e);
        duplicate.setEntryDate(1L);
        duplicate.setCode((String) body.get("code"));
        duplicate.setStatus("Pendiente");
        assertThatThrownBy(() -> orderRepository.saveAndFlush(duplicate))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    @Test void assignedIdInsertCannotMergeExistingRow() throws Exception {
        String id = client();
        em.clear();
        var duplicate = new Client();
        duplicate.setId(id);
        duplicate.setName("No sobrescribir");
        duplicate.setPhone("70000000");
        duplicate.setUpdatedAt(1);
        assertThat(duplicate.isNew()).isTrue();
        assertThatThrownBy(() -> clientRepository.saveAndFlush(duplicate))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    @Test void updatesRejectDuplicateSerialAndDuplicateOrderDetails() throws Exception {
        String c = client(), firstEquipment = equipment(c);
        String secondEquipment = create("equipment", Map.of("clientId", c, "serial", "SECOND")).get("id").asText();
        mvc.perform(put("/api/equipment/" + firstEquipment).contentType("application/json")
                .content(mapper.writeValueAsString(Map.of("clientId", c, "serial", "SECOND"))))
                .andExpect(status().isConflict());
        String firstOrder = create("orders", orderBody(c, firstEquipment)).get("id").asText();
        String secondOrder = create("orders", orderBody(c, secondEquipment)).get("id").asText();
        for (String path : List.of("diagnoses", "repairs")) {
            String id = create(path, Map.of("orderId", firstOrder)).get("id").asText();
            create(path, Map.of("orderId", secondOrder));
            mvc.perform(put("/api/" + path + "/" + id).contentType("application/json")
                    .content(mapper.writeValueAsString(Map.of("orderId", secondOrder))))
                    .andExpect(status().isConflict());
            mvc.perform(put("/api/" + path + "/" + id).contentType("application/json")
                    .content(mapper.writeValueAsString(Map.of("orderId", ABSENT))))
                    .andExpect(status().isNotFound());
        }
    }

    @Test void databaseEnforcesForeignKeysEvenWithoutService() {
        var orphan = new Equipment();
        orphan.setId(UUID.randomUUID().toString());
        orphan.setClientId(ABSENT);
        orphan.setUpdatedAt(1);
        assertThatThrownBy(() -> equipmentRepository.saveAndFlush(orphan))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }
}

