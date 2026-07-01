package com.barapp.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.barapp.backend.dto.request.CategoryRequest;
import com.barapp.backend.dto.response.CategoryResponse;
import com.barapp.backend.entity.Category;
import com.barapp.backend.mapper.CategoryMapper;
import com.barapp.backend.repository.CategoryRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

// Service qui gère toute la logique métier des catégories
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    // Je récupère toutes les catégories et je les convertis en DTO pour ne pas exposer l'entité directement
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    // Création d'une nouvelle catégorie à partir des données envoyées par le barmaker
    public CategoryResponse create(CategoryRequest request) {
        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    // Mise à jour d'une catégorie existante, je vérifie qu'elle existe avant de modifier
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Catégorie non trouvée"));
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    // Suppression d'une catégorie, je vérifie qu'elle existe d'abord pour éviter une erreur silencieuse
    public void delete(Long id) {
        categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Catégorie non trouvée"));
        categoryRepository.deleteById(id);
    }
}