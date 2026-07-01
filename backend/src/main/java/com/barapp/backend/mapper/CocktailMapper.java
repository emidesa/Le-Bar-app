package com.barapp.backend.mapper;

import com.barapp.backend.dto.response.CategoryResponse;
import com.barapp.backend.dto.response.CocktailIngredientResponse;
import com.barapp.backend.dto.response.CocktailResponse;
import com.barapp.backend.dto.response.CocktailSizeResponse;
import com.barapp.backend.entity.Cocktail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CocktailMapper {

    private final CategoryMapper categoryMapper;

    public CocktailResponse toResponse(Cocktail cocktail) {
        CategoryResponse cat = cocktail.getCategory() != null
                ? categoryMapper.toResponse(cocktail.getCategory())
                : null;

        List<CocktailSizeResponse> sizes = cocktail.getSizes().stream()
                .map(s -> new CocktailSizeResponse(s.getId(), s.getSize(), s.getPrice()))
                .toList();

        List<CocktailIngredientResponse> ingredients = cocktail.getIngredients().stream()
                .map(i -> new CocktailIngredientResponse(
                        i.getId(), i.getIngredient().getName(), i.getQuantity(), i.getUnit()))
                .toList();

        return new CocktailResponse(
                cocktail.getId(),
                cocktail.getName(),
                cocktail.getDescription(),
                cocktail.getImageUrl(),
                cat,
                sizes,
                ingredients
        );
    }
}
