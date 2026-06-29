package com.barapp.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.barapp.backend.entity.Ingredient;
import com.barapp.backend.repository.IngredientRepository;

import lombok.RequiredArgsConstructor;

// Service simple pour gérer les ingrédients disponibles dans le bar
@Service
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientRepository ingredientRepository;

    // Liste tous les ingrédients, utilisé par le barmaker quand il crée un cocktail
    public List<Ingredient> getAll() {
        return ingredientRepository.findAll();
    }

    // Ajout d'un nouvel ingrédient dans le catalogue
    public Ingredient create(String name) {
        Ingredient ingredient = Ingredient.builder().name(name).build();
        return ingredientRepository.save(ingredient);
    }
}