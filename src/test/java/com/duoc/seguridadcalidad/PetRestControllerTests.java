package com.duoc.seguridadcalidad;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PetRestControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BackendService backendService;

    @Test
    void shouldProxyGetAllPets() throws Exception {
        when(backendService.getPets()).thenReturn(List.of(
                Map.of("id", 1, "name", "Luna", "species", "Felino"),
                Map.of("id", 2, "name", "Rocky", "species", "Canino")
        ));

        mockMvc.perform(get("/api/pets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].name").value("Rocky"));
    }

    @Test
    void shouldProxyGetAvailablePets() throws Exception {
        when(backendService.getAvailablePets()).thenReturn(List.of(
                Map.of("id", 1, "name", "Luna", "status", "AVAILABLE")
        ));

        mockMvc.perform(get("/api/pets/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"));
    }

    @Test
    void shouldProxySearchPets() throws Exception {
        when(backendService.searchPets("Canino", null, null, null, null))
                .thenReturn(List.of(Map.of("id", 2, "species", "Canino")));

        mockMvc.perform(get("/api/pets/search").param("species", "Canino"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].species").value("Canino"));
    }

    @Test
    void shouldProxyCreatePet() throws Exception {
        when(backendService.createPet(eq("valid-token"), anyMap()))
                .thenReturn(Map.of("id", 3, "name", "Nina", "species", "Felino"));

        String payload = """
                {
                  "name": "Nina",
                  "species": "Felino",
                  "gender": "F",
                  "age": 2
                }
                """;

        mockMvc.perform(post("/api/pets")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("Nina"));
    }

    @Test
    void shouldRequireTokenForCreatePet() throws Exception {
        mockMvc.perform(post("/api/pets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldProxyUpdatePet() throws Exception {
        when(backendService.updatePet(eq("valid-token"), eq(1), anyMap()))
                .thenReturn(Map.of("id", 1, "name", "Luna", "age", 4));

        String payload = "{\"name\":\"Luna\",\"age\":4}";

        mockMvc.perform(put("/api/pets/1")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.age").value(4));
    }

    @Test
    void shouldRequireTokenForUpdatePet() throws Exception {
        mockMvc.perform(put("/api/pets/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldProxyDeletePet() throws Exception {
        when(backendService.deletePet(eq("valid-token"), eq(1)))
                .thenReturn(Map.of("id", 1, "deleted", true));

        mockMvc.perform(delete("/api/pets/1")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldRequireTokenForDeletePet() throws Exception {
        mockMvc.perform(delete("/api/pets/1"))
                .andExpect(status().isUnauthorized());
    }
}
