package com.barapp.backend.controller;

import com.barapp.backend.dto.request.CategoryRequest;
import com.barapp.backend.dto.response.CategoryResponse;
import com.barapp.backend.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class CategoryControllerTest {

    @Autowired WebApplicationContext context;
    @MockitoBean CategoryService categoryService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    void getAll_retourne_200_avec_liste() throws Exception {
        when(categoryService.getAll()).thenReturn(List.of(new CategoryResponse(1L, "Classiques", "")));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Classiques"));
    }

    @Test
    @WithMockUser(roles = "BARMAKER")
    void create_retourne_200_avec_donnees_valides() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Tropicaux");
        request.setDescription("Cocktails fruités");

        when(categoryService.create(any())).thenReturn(new CategoryResponse(2L, "Tropicaux", "Cocktails fruités"));

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Tropicaux"));
    }

    @Test
    @WithMockUser(roles = "BARMAKER")
    void create_retourne_400_si_nom_vide() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("");

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "BARMAKER")
    void update_retourne_200_avec_donnees_valides() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Mis à jour");

        when(categoryService.update(eq(1L), any())).thenReturn(new CategoryResponse(1L, "Mis à jour", ""));

        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Mis à jour"));
    }

    @Test
    @WithMockUser(roles = "BARMAKER")
    void update_retourne_404_si_introuvable() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Test");

        when(categoryService.update(eq(99L), any()))
                .thenThrow(new EntityNotFoundException("Catégorie non trouvée"));

        mockMvc.perform(put("/api/categories/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "BARMAKER")
    void delete_retourne_204() throws Exception {
        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isNoContent());
    }
}
