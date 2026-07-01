package com.barapp.backend.controller;

import com.barapp.backend.dto.request.OrderItemRequest;
import com.barapp.backend.dto.request.OrderRequest;
import com.barapp.backend.dto.response.OrderResponse;
import com.barapp.backend.enums.OrderStatus;
import com.barapp.backend.service.OrderService;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class OrderControllerTest {

    @Autowired WebApplicationContext context;
    @MockitoBean OrderService orderService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    private OrderResponse buildResponse() {
        return new OrderResponse(1L, 5, OrderStatus.COMMANDEE, BigDecimal.valueOf(9.5), null, List.of());
    }

    @Test
    void create_retourne_200_avec_donnees_valides() throws Exception {
        OrderItemRequest item = new OrderItemRequest();
        item.setCocktailId(1L);
        item.setSizeId(1L);
        item.setQuantity(1);

        OrderRequest request = new OrderRequest();
        request.setTableNumber(5);
        request.setItems(List.of(item));

        when(orderService.create(any())).thenReturn(buildResponse());

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tableNumber").value(5));
    }

    @Test
    void create_retourne_400_si_table_manquante() throws Exception {
        OrderRequest request = new OrderRequest();
        request.setItems(List.of());

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getByTable_retourne_200() throws Exception {
        when(orderService.getOrdersByTable(5)).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/api/orders/table/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tableNumber").value(5));
    }

    @Test
    void getById_retourne_200() throws Exception {
        when(orderService.getById(1L)).thenReturn(buildResponse());

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getById_retourne_404_si_introuvable() throws Exception {
        when(orderService.getById(99L)).thenThrow(new EntityNotFoundException("Commande non trouvée"));

        mockMvc.perform(get("/api/orders/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_retourne_204_pour_commande_terminee() throws Exception {
        mockMvc.perform(delete("/api/orders/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_retourne_400_si_commande_non_terminee() throws Exception {
        doThrow(new IllegalStateException("Seules les commandes terminées peuvent être supprimées"))
                .when(orderService).deleteOrder(1L);

        mockMvc.perform(delete("/api/orders/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "BARMAKER")
    void advanceItem_retourne_200() throws Exception {
        mockMvc.perform(patch("/api/orders/items/1/next-step"))
                .andExpect(status().isOk());
    }
}
