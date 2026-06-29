package com.barapp.backend.service;

import com.barapp.backend.dto.request.CocktailRequest;
import com.barapp.backend.dto.response.*;
import com.barapp.backend.entity.*;
import com.barapp.backend.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

// Service qui gère la carte des cocktails, utilisé à la fois par les clients et les barmakers
@Service
@RequiredArgsConstructor
public class CocktailService {

    private final CocktailRepository cocktailRepository;
    private final CategoryRepository categoryRepository;
    private final IngredientRepository ingredientRepository;

    // Retourne tous les cocktails de la carte avec leurs tailles et ingrédients
    public List<CocktailResponse> getAll() {
        return cocktailRepository.findAll().stream().map(this::toResponse).toList();
    }

    // Filtre les cocktails par catégorie, pratique pour le menu client
    public List<CocktailResponse> getByCategory(Long categoryId) {
        return cocktailRepository.findByCategoryId(categoryId).stream().map(this::toResponse).toList();
    }

    // Récupère un cocktail précis par son id
    public CocktailResponse getById(Long id) {
        return toResponse(cocktailRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cocktail non trouvé")));
    }

    // Création d'un cocktail avec ses tailles (S/M/L) et ses ingrédients en une seule requête
    public CocktailResponse create(CocktailRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Catégorie non trouvée"));

        Cocktail cocktail = Cocktail.builder()
                .name(request.getName())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .category(category)
                .build();

        // J'ajoute les tailles avec leur prix correspondant
        if (request.getSizes() != null) {
            request.getSizes().forEach(s -> {
                CocktailSize size = CocktailSize.builder()
                        .cocktail(cocktail)
                        .size(s.getSize())
                        .price(s.getPrice())
                        .build();
                cocktail.getSizes().add(size);
            });
        }

        // J'ajoute les ingrédients avec leur quantité et unité
        if (request.getIngredients() != null) {
            request.getIngredients().forEach(i -> {
                Ingredient ingredient = ingredientRepository.findById(i.getIngredientId())
                        .orElseThrow(() -> new EntityNotFoundException("Ingrédient non trouvé"));
                CocktailIngredient ci = CocktailIngredient.builder()
                        .cocktail(cocktail)
                        .ingredient(ingredient)
                        .quantity(i.getQuantity())
                        .unit(i.getUnit())
                        .notes(i.getNotes())
                        .build();
                cocktail.getIngredients().add(ci);
            });
        }

        return toResponse(cocktailRepository.save(cocktail));
    }

    // Suppression d'un cocktail, le cascade supprime automatiquement ses tailles et ingrédients
    public void delete(Long id) {
        cocktailRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cocktail non trouvé"));
        cocktailRepository.deleteById(id);
    }

    // Conversion de l'entité en DTO pour ne pas exposer les données JPA directement
    private CocktailResponse toResponse(Cocktail c) {
        CategoryResponse cat = c.getCategory() != null
                ? new CategoryResponse(c.getCategory().getId(), c.getCategory().getName(), c.getCategory().getDescription())
                : null;

        List<CocktailSizeResponse> sizes = c.getSizes().stream()
                .map(s -> new CocktailSizeResponse(s.getId(), s.getSize(), s.getPrice()))
                .toList();

        List<CocktailIngredientResponse> ingredients = c.getIngredients().stream()
                .map(i -> new CocktailIngredientResponse(i.getId(), i.getIngredient().getName(), i.getQuantity(), i.getUnit()))
                .toList();

        return new CocktailResponse(c.getId(), c.getName(), c.getDescription(), c.getImageUrl(), cat, sizes, ingredients);
    }
}