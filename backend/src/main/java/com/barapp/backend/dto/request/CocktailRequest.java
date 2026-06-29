package com.barapp.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class CocktailRequest {
    @NotBlank
    private String name;

    private String description;
    private String imageUrl;

    @NotNull
    private Long categoryId;

    @NotEmpty
    private List<CocktailSizeRequest> sizes;

    private List<CocktailIngredientRequest> ingredients;
}
