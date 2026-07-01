package com.barapp.backend.service;

import com.barapp.backend.entity.Ingredient;
import com.barapp.backend.repository.IngredientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IngredientServiceTest {

    @Mock IngredientRepository ingredientRepository;
    @InjectMocks IngredientService ingredientService;

    @Test
    void getAll_retourne_liste_ingredients() {
        Ingredient ingredient = Ingredient.builder().id(1L).name("Rhum").build();
        when(ingredientRepository.findAll()).thenReturn(List.of(ingredient));

        List<Ingredient> result = ingredientService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Rhum");
    }

    @Test
    void create_sauvegarde_ingredient() {
        Ingredient saved = Ingredient.builder().id(1L).name("Menthe").build();
        when(ingredientRepository.save(any(Ingredient.class))).thenReturn(saved);

        Ingredient result = ingredientService.create("Menthe");

        assertThat(result.getName()).isEqualTo("Menthe");
    }
}
