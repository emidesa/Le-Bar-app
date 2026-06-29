package com.barapp.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barapp.backend.dto.request.CocktailRequest;
import com.barapp.backend.dto.response.CocktailResponse;
import com.barapp.backend.service.CocktailService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// Gère les routes HTTP pour les cocktails de la carte
@RestController
@RequestMapping("/api/cocktails")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CocktailController {

    private final CocktailService cocktailService;

    @GetMapping
    public ResponseEntity<List<CocktailResponse>> getAll() {
        return ResponseEntity.ok(cocktailService.getAll());
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<CocktailResponse>> getByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(cocktailService.getByCategory(categoryId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CocktailResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(cocktailService.getById(id));
    }

    @PostMapping
    public ResponseEntity<CocktailResponse> create(@Valid @RequestBody CocktailRequest request) {
        return ResponseEntity.ok(cocktailService.create(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cocktailService.delete(id);
        return ResponseEntity.noContent().build();
    }
}