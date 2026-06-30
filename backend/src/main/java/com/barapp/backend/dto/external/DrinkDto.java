package com.barapp.backend.dto.external;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

// Représente un cocktail tel que renvoyé par l'API TheCocktailDB
@Data
public class DrinkDto {
    private String strDrink;
    private String strCategory;
    private String strAlcoholic;
    private String strInstructions;
    private String strDrinkThumb;

    // L'API retourne jusqu'à 15 ingrédients et mesures sous forme de champs séparés
    private String strIngredient1;  private String strMeasure1;
    private String strIngredient2;  private String strMeasure2;
    private String strIngredient3;  private String strMeasure3;
    private String strIngredient4;  private String strMeasure4;
    private String strIngredient5;  private String strMeasure5;
    private String strIngredient6;  private String strMeasure6;
    private String strIngredient7;  private String strMeasure7;
    private String strIngredient8;  private String strMeasure8;
    private String strIngredient9;  private String strMeasure9;
    private String strIngredient10; private String strMeasure10;
    private String strIngredient11; private String strMeasure11;
    private String strIngredient12; private String strMeasure12;
    private String strIngredient13; private String strMeasure13;
    private String strIngredient14; private String strMeasure14;
    private String strIngredient15; private String strMeasure15;

    // Je regroupe les ingrédients non-nuls dans une liste pour simplifier le traitement
    public List<String> getIngredientNames() {
        List<String> list = new ArrayList<>();
        for (String i : new String[]{
            strIngredient1, strIngredient2, strIngredient3, strIngredient4, strIngredient5,
            strIngredient6, strIngredient7, strIngredient8, strIngredient9, strIngredient10,
            strIngredient11, strIngredient12, strIngredient13, strIngredient14, strIngredient15
        }) { if (i != null && !i.isBlank()) list.add(i.trim()); }
        return list;
    }

    // Même logique pour les mesures (ex: "2 oz", "1 cl")
    public List<String> getMeasureList() {
        List<String> list = new ArrayList<>();
        for (String m : new String[]{
            strMeasure1, strMeasure2, strMeasure3, strMeasure4, strMeasure5,
            strMeasure6, strMeasure7, strMeasure8, strMeasure9, strMeasure10,
            strMeasure11, strMeasure12, strMeasure13, strMeasure14, strMeasure15
        }) { list.add(m != null ? m.trim() : ""); }
        return list;
    }
}