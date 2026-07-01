package com.barapp.backend.service;

import com.barapp.backend.dto.request.CocktailIngredientRequest;
import com.barapp.backend.dto.request.CocktailRequest;
import com.barapp.backend.dto.response.CocktailResponse;
import com.barapp.backend.entity.Category;
import com.barapp.backend.entity.Cocktail;
import com.barapp.backend.entity.Ingredient;
import com.barapp.backend.mapper.CocktailMapper;
import com.barapp.backend.repository.CategoryRepository;
import com.barapp.backend.repository.CocktailRepository;
import com.barapp.backend.repository.IngredientRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CocktailServiceTest {

    @Mock CocktailRepository cocktailRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock IngredientRepository ingredientRepository;
    @Mock CocktailMapper cocktailMapper;

    @InjectMocks
    CocktailService cocktailService;

    private Category buildCategory() {
        return Category.builder().id(1L).name("Classiques").description("").build();
    }

    private Cocktail buildCocktail(Category category) {
        return Cocktail.builder()
                .id(1L).name("Mojito").description("Menthe et citron")
                .category(category).sizes(new ArrayList<>()).ingredients(new ArrayList<>())
                .build();
    }

    @Test
    void getAll_retourne_liste_cocktails() {
        Cocktail c = buildCocktail(buildCategory());
        when(cocktailRepository.findAll()).thenReturn(List.of(c));
        when(cocktailMapper.toResponse(c)).thenReturn(new CocktailResponse(1L, "Mojito", "Menthe et citron", null, null, List.of(), List.of()));

        List<CocktailResponse> result = cocktailService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Mojito");
    }

    @Test
    void getAll_retourne_liste_vide() {
        when(cocktailRepository.findAll()).thenReturn(List.of());

        List<CocktailResponse> result = cocktailService.getAll();

        assertThat(result).isEmpty();
    }

    @Test
    void getById_retourne_cocktail_existant() {
        Cocktail c = buildCocktail(buildCategory());
        when(cocktailRepository.findById(1L)).thenReturn(Optional.of(c));
        when(cocktailMapper.toResponse(c)).thenReturn(new CocktailResponse(1L, "Mojito", "Menthe et citron", null, null, List.of(), List.of()));

        CocktailResponse result = cocktailService.getById(1L);

        assertThat(result.getName()).isEqualTo("Mojito");
    }

    @Test
    void getById_lance_exception_si_introuvable() {
        when(cocktailRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> cocktailService.getById(99L));
    }

    @Test
    void getByCategory_retourne_cocktails_de_la_categorie() {
        Cocktail c = buildCocktail(buildCategory());
        when(cocktailRepository.findByCategoryId(1L)).thenReturn(List.of(c));
        when(cocktailMapper.toResponse(c)).thenReturn(new CocktailResponse(1L, "Mojito", "Menthe et citron", null, null, List.of(), List.of()));

        List<CocktailResponse> result = cocktailService.getByCategory(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Mojito");
    }

    @Test
    void create_sauvegarde_cocktail_avec_categorie_valide() {
        Category category = buildCategory();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        CocktailRequest request = new CocktailRequest();
        request.setName("Spritz");
        request.setCategoryId(1L);
        request.setSizes(List.of());
        request.setIngredients(List.of());

        Cocktail saved = Cocktail.builder()
                .id(2L).name("Spritz").category(category)
                .sizes(new ArrayList<>()).ingredients(new ArrayList<>()).build();
        when(cocktailRepository.save(any(Cocktail.class))).thenReturn(saved);
        when(cocktailMapper.toResponse(saved)).thenReturn(new CocktailResponse(2L, "Spritz", null, null, null, List.of(), List.of()));

        CocktailResponse result = cocktailService.create(request);

        assertThat(result.getName()).isEqualTo("Spritz");
        verify(cocktailRepository).save(any(Cocktail.class));
    }

    @Test
    void create_lance_exception_si_categorie_introuvable() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        CocktailRequest request = new CocktailRequest();
        request.setName("Test");
        request.setCategoryId(99L);
        request.setSizes(List.of());

        assertThrows(EntityNotFoundException.class, () -> cocktailService.create(request));
    }

    @Test
    void delete_supprime_cocktail_existant() {
        Cocktail c = buildCocktail(buildCategory());
        when(cocktailRepository.findById(1L)).thenReturn(Optional.of(c));

        cocktailService.delete(1L);

        verify(cocktailRepository).deleteById(1L);
    }

    @Test
    void delete_lance_exception_si_cocktail_introuvable() {
        when(cocktailRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> cocktailService.delete(99L));
        verify(cocktailRepository, never()).deleteById(any());
    }

    @Test
    void create_avec_ingredients_sauvegarde_cocktail_complet() {
        Category category = buildCategory();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        Ingredient ingredient = Ingredient.builder().id(1L).name("Rhum").build();
        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(ingredient));

        CocktailIngredientRequest ingredientRequest = new CocktailIngredientRequest();
        ingredientRequest.setIngredientId(1L);
        ingredientRequest.setQuantity(new java.math.BigDecimal("5"));
        ingredientRequest.setUnit("cl");

        CocktailRequest request = new CocktailRequest();
        request.setName("Dark & Stormy");
        request.setCategoryId(1L);
        request.setSizes(List.of());
        request.setIngredients(List.of(ingredientRequest));

        Cocktail saved = Cocktail.builder()
                .id(3L).name("Dark & Stormy").category(category)
                .sizes(new ArrayList<>()).ingredients(new ArrayList<>()).build();
        when(cocktailRepository.save(any(Cocktail.class))).thenReturn(saved);
        when(cocktailMapper.toResponse(saved)).thenReturn(
                new CocktailResponse(3L, "Dark & Stormy", null, null, null, List.of(), List.of()));

        CocktailResponse result = cocktailService.create(request);

        assertThat(result.getName()).isEqualTo("Dark & Stormy");
        verify(ingredientRepository).findById(1L);
    }
}
