package com.barapp.backend.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CocktailIngredientRequest {
    @NotNull
    private Long ingredientId;

    @NotNull
    private BigDecimal quantity;

    @NotBlank
    private String unit;

    private String notes;
}
