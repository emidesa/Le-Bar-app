package com.barapp.backend.service;

import com.barapp.backend.dto.request.CategoryRequest;
import com.barapp.backend.dto.response.CategoryResponse;
import com.barapp.backend.entity.Category;
import com.barapp.backend.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

// Service qui gère toute la logique métier des catégories
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // Je récupère toutes les catégories et je les convertis en DTO pour ne pas exposer l'entité directement
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll().stream()
                .map(c -> new CategoryResponse(c.getId(), c.getName(), c.getDescription()))
                .toList();
    }

    // Création d'une nouvelle catégorie à partir des données envoyées par le barmaker
    public CategoryResponse create(CategoryRequest request) {
        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        category = categoryRepository.save(category);
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription());
    }

    // Mise à jour d'une catégorie existante, je vérifie qu'elle existe avant de modifier
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Catégorie non trouvée"));
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category = categoryRepository.save(category);
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription());
    }

    // Suppression d'une catégorie, je vérifie qu'elle existe d'abord pour éviter une erreur silencieuse
    public void delete(Long id) {
        categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Catégorie non trouvée"));
        categoryRepository.deleteById(id);
    }
}