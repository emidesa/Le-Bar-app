package com.barapp.backend.dto.response;

import java.math.BigDecimal;

import com.barapp.backend.enums.CocktailSizeEnum;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CocktailSizeResponse {
    private Long id;
    private CocktailSizeEnum size;
    private BigDecimal price;
}