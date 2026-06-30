package com.barapp.backend.dto.external;

import java.util.List;

import lombok.Data;

// Enveloppe la réponse JSON de TheCocktailDB : { "drinks": [ ... ] }
@Data
public class CocktailDbResponse {
    private List<DrinkDto> drinks;
}