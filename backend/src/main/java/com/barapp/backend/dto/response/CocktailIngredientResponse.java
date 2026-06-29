package com.barapp.backend.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CocktailIngredientResponse {
    private Long id;
    private String ingredientName;
    private BigDecimal quantity;
    private String unit;
}