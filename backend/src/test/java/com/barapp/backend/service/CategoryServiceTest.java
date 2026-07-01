package com.barapp.backend.service;

import com.barapp.backend.dto.request.CategoryRequest;
import com.barapp.backend.dto.response.CategoryResponse;
import com.barapp.backend.entity.Category;
import com.barapp.backend.mapper.CategoryMapper;
import com.barapp.backend.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock CategoryRepository categoryRepository;
    @Mock CategoryMapper categoryMapper;

    @InjectMocks
    CategoryService categoryService;

    @Test
    void getAll_retourne_toutes_les_categories() {
        Category cat = Category.builder().id(1L).name("Classiques").description("").build();
        when(categoryRepository.findAll()).thenReturn(List.of(cat));
        when(categoryMapper.toResponse(cat)).thenReturn(new CategoryResponse(1L, "Classiques", ""));

        List<CategoryResponse> result = categoryService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Classiques");
    }

    @Test
    void getAll_retourne_liste_vide_si_aucune_categorie() {
        when(categoryRepository.findAll()).thenReturn(List.of());

        List<CategoryResponse> result = categoryService.getAll();

        assertThat(result).isEmpty();
    }

    @Test
    void create_sauvegarde_et_retourne_la_categorie() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Tropicaux");
        request.setDescription("Cocktails fruités");

        Category saved = Category.builder().id(2L).name("Tropicaux").description("Cocktails fruités").build();
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);
        when(categoryMapper.toResponse(saved)).thenReturn(new CategoryResponse(2L, "Tropicaux", "Cocktails fruités"));

        CategoryResponse result = categoryService.create(request);

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getName()).isEqualTo("Tropicaux");
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void update_modifie_et_retourne_la_categorie() {
        Category existing = Category.builder().id(1L).name("Ancien").description("").build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));

        CategoryRequest request = new CategoryRequest();
        request.setName("Nouveau");
        request.setDescription("Nouvelle description");

        Category updated = Category.builder().id(1L).name("Nouveau").description("Nouvelle description").build();
        when(categoryRepository.save(any(Category.class))).thenReturn(updated);
        when(categoryMapper.toResponse(updated)).thenReturn(new CategoryResponse(1L, "Nouveau", "Nouvelle description"));

        CategoryResponse result = categoryService.update(1L, request);

        assertThat(result.getName()).isEqualTo("Nouveau");
    }

    @Test
    void update_lance_exception_si_categorie_introuvable() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        CategoryRequest request = new CategoryRequest();
        request.setName("Test");

        assertThrows(EntityNotFoundException.class, () -> categoryService.update(99L, request));
    }

    @Test
    void delete_supprime_la_categorie_existante() {
        Category cat = Category.builder().id(1L).name("Classiques").build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat));

        categoryService.delete(1L);

        verify(categoryRepository, times(1)).deleteById(1L);
    }

    @Test
    void delete_lance_exception_si_categorie_introuvable() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> categoryService.delete(99L));
        verify(categoryRepository, never()).deleteById(any());
    }
}
