package com.barapp.backend;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.barapp.backend.dto.external.CocktailDbResponse;
import com.barapp.backend.dto.external.DrinkDto;
import com.barapp.backend.entity.Category;
import com.barapp.backend.entity.Cocktail;
import com.barapp.backend.entity.CocktailIngredient;
import com.barapp.backend.entity.CocktailSize;
import com.barapp.backend.entity.Ingredient;
import com.barapp.backend.enums.CocktailSizeEnum;
import com.barapp.backend.repository.CategoryRepository;
import com.barapp.backend.repository.CocktailRepository;
import com.barapp.backend.repository.IngredientRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// S'exécute une seule fois au démarrage pour peupler la BDD depuis TheCocktailDB
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CocktailRepository cocktailRepository;
    private final CategoryRepository categoryRepository;
    private final IngredientRepository ingredientRepository;
    private final RestTemplate restTemplate;

    private static final String API_URL =
        "https://www.thecocktaildb.com/api/json/v1/1/search.php?f=";

    @Override
    public void run(String... args) {
        // Si des cocktails existent déjà, on ne fait rien pour éviter les doublons
        if (cocktailRepository.count() > 0) {
            log.info("BDD déjà initialisée, chargement de l'API ignoré.");
            return;
        }

        log.info("Chargement des cocktails depuis TheCocktailDB...");
        for (String letter : new String[]{"m", "p", "b", "a", "d"}) {
            try {
                CocktailDbResponse response = restTemplate.getForObject(
                    API_URL + letter, CocktailDbResponse.class);
                if (response == null || response.getDrinks() == null) continue;
                response.getDrinks().forEach(this::saveDrink);
            } catch (Exception e) {
                log.warn("Erreur pour la lettre {} : {}", letter, e.getMessage());
            }
        }
        log.info("Initialisation terminée : {} cocktails chargés.", cocktailRepository.count());
    }

    private static final java.util.Map<String, String> CATEGORY_FR = java.util.Map.ofEntries(
        java.util.Map.entry("Ordinary Drink",     "Cocktails classiques"),
        java.util.Map.entry("Cocktail",           "Cocktails"),
        java.util.Map.entry("Shot",               "Shooters"),
        java.util.Map.entry("Coffee / Tea",       "Café & Thé"),
        java.util.Map.entry("Homemade Liqueur",   "Liqueurs maison"),
        java.util.Map.entry("Punch / Party Drink","Punch & Fêtes"),
        java.util.Map.entry("Beer",               "Bières"),
        java.util.Map.entry("Soft Drink",         "Boissons sans alcool"),
        java.util.Map.entry("Other/Unknown",      "Autres"),
        java.util.Map.entry("Non alcoholic",      "Sans alcool")
    );

    private void saveDrink(DrinkDto drink) {
        // Si le cocktail existe déjà, on passe au suivant
        if (cocktailRepository.findAll().stream()
                .anyMatch(c -> c.getName().equalsIgnoreCase(drink.getStrDrink()))) return;

        // Traduction de la catégorie en français
        String rawCategory = "Non alcoholic".equals(drink.getStrAlcoholic())
                ? "Non alcoholic" : drink.getStrCategory();
        String categoryName = CATEGORY_FR.getOrDefault(rawCategory, rawCategory);
        Category category = categoryRepository.findByName(categoryName)
                .orElseGet(() -> categoryRepository.save(
                    Category.builder().name(categoryName).description("").build()));

        Cocktail cocktail = Cocktail.builder()
                .name(drink.getStrDrink())
                .description(null)
                .imageUrl(drink.getStrDrinkThumb())
                .category(category)
                .build();

        // Je crée les 3 tailles avec des prix par défaut
        cocktail.getSizes().add(CocktailSize.builder().cocktail(cocktail)
            .size(CocktailSizeEnum.S).price(new BigDecimal("6.50")).build());
        cocktail.getSizes().add(CocktailSize.builder().cocktail(cocktail)
            .size(CocktailSizeEnum.M).price(new BigDecimal("8.50")).build());
        cocktail.getSizes().add(CocktailSize.builder().cocktail(cocktail)
            .size(CocktailSizeEnum.L).price(new BigDecimal("10.50")).build());

        // J'associe les ingrédients au cocktail, je les crée en BDD s'ils n'existent pas
        List<String> names = drink.getIngredientNames();
        List<String> measures = drink.getMeasureList();
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            Ingredient ingredient = ingredientRepository.findByName(name)
                    .orElseGet(() -> ingredientRepository.save(
                        Ingredient.builder().name(name).build()));
            cocktail.getIngredients().add(CocktailIngredient.builder()
                    .cocktail(cocktail)
                    .quantity(BigDecimal.ONE)
                    .ingredient(ingredient)
                    .unit(i < measures.size() ? measures.get(i) : "")
                    .build());
        }

        cocktailRepository.save(cocktail);
    }
}