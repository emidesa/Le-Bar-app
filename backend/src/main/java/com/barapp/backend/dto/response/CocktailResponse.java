package com.barapp.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class CocktailResponse {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private CategoryResponse category;
    private List<CocktailSizeResponse> sizes;
    private List<CocktailIngredientResponse> ingredients;
}