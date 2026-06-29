package com.barapp.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.barapp.backend.entity.Ingredient;
import com.barapp.backend.service.IngredientService;

import lombok.RequiredArgsConstructor;

// Gère les routes HTTP pour les ingrédients
@RestController
@RequestMapping("/api/ingredients")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class IngredientController {

    private final IngredientService ingredientService;

    @GetMapping
    public ResponseEntity<List<Ingredient>> getAll() {
        return ResponseEntity.ok(ingredientService.getAll());
    }

    @PostMapping
    public ResponseEntity<Ingredient> create(@RequestParam String name) {
        return ResponseEntity.ok(ingredientService.create(name));
    }
}