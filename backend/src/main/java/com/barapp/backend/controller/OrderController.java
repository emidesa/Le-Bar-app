package com.barapp.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barapp.backend.dto.request.OrderRequest;
import com.barapp.backend.dto.response.OrderItemResponse;
import com.barapp.backend.dto.response.OrderResponse;
import com.barapp.backend.service.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// Gère les routes HTTP pour les commandes, côté client et barmaker
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    // Le client crée une commande depuis son panier
    @PostMapping("/client/{clientId}")
    public ResponseEntity<OrderResponse> create(@PathVariable Long clientId, @Valid @RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderService.create(clientId, request));
    }

    // Le client consulte ses commandes pour suivre leur avancement
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<OrderResponse>> getClientOrders(@PathVariable Long clientId) {
        return ResponseEntity.ok(orderService.getClientOrders(clientId));
    }

    // Le barmaker voit toutes les commandes non terminées à traiter
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getPendingOrders() {
        return ResponseEntity.ok(orderService.getPendingOrders());
    }

    // Détail d'une commande spécifique
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getById(id));
    }

    // Le barmaker fait avancer un cocktail à l'étape suivante
    @PatchMapping("/items/{itemId}/next-step")
    public ResponseEntity<OrderItemResponse> advanceItemStatus(@PathVariable Long itemId) {
        return ResponseEntity.ok(orderService.advanceItemStatus(itemId));
    }
}