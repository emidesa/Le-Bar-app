package com.barapp.backend.controller;

import com.barapp.backend.dto.request.CocktailRequest;
import com.barapp.backend.dto.request.CocktailSizeRequest;
import com.barapp.backend.dto.response.CategoryResponse;
import com.barapp.backend.dto.response.CocktailResponse;
import com.barapp.backend.enums.CocktailSizeEnum;
import com.barapp.backend.service.CocktailService;
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

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class CocktailControllerTest {

    @Autowired WebApplicationContext context;
    @MockitoBean CocktailService cocktailService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    private CocktailResponse buildResponse() {
        return new CocktailResponse(1L, "Mojito", "Menthe et citron", null,
                new CategoryResponse(1L, "Classiques", ""), List.of(), List.of());
    }

    @Test
    void getAll_retourne_200() throws Exception {
        when(cocktailService.getAll()).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/api/cocktails"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Mojito"));
    }

    @Test
    void getById_retourne_200() throws Exception {
        when(cocktailService.getById(1L)).thenReturn(buildResponse());

        mockMvc.perform(get("/api/cocktails/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Mojito"));
    }

    @Test
    void getById_retourne_404_si_introuvable() throws Exception {
        when(cocktailService.getById(99L)).thenThrow(new EntityNotFoundException("Cocktail non trouvé"));

        mockMvc.perform(get("/api/cocktails/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByCategory_retourne_200() throws Exception {
        when(cocktailService.getByCategory(1L)).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/api/cocktails/category/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Mojito"));
    }

    @Test
    @WithMockUser(roles = "BARMAKER")
    void create_retourne_200_avec_donnees_valides() throws Exception {
        CocktailSizeRequest size = new CocktailSizeRequest();
        size.setSize(CocktailSizeEnum.M);
        size.setPrice(BigDecimal.valueOf(9.5));

        CocktailRequest request = new CocktailRequest();
        request.setName("Spritz");
        request.setCategoryId(1L);
        request.setSizes(List.of(size));

        when(cocktailService.create(any())).thenReturn(buildResponse());

        mockMvc.perform(post("/api/cocktails")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "BARMAKER")
    void create_retourne_400_si_nom_vide() throws Exception {
        CocktailRequest request = new CocktailRequest();
        request.setName("");
        request.setCategoryId(1L);
        request.setSizes(List.of());

        mockMvc.perform(post("/api/cocktails")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "BARMAKER")
    void delete_retourne_204() throws Exception {
        mockMvc.perform(delete("/api/cocktails/1"))
                .andExpect(status().isNoContent());
    }
}
