package com.barapp.backend.dto.request;

import java.math.BigDecimal;

import com.barapp.backend.enums.CocktailSizeEnum;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CocktailSizeRequest {
    @NotNull
    private CocktailSizeEnum size;

    @NotNull
    private BigDecimal price;
}
