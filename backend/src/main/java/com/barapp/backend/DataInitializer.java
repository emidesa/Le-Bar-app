package com.barapp.backend;

import com.barapp.backend.dto.external.CocktailDbResponse;
import com.barapp.backend.dto.external.DrinkDto;
import com.barapp.backend.entity.*;
import com.barapp.backend.enums.CocktailSizeEnum;
import com.barapp.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

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

    private void saveDrink(DrinkDto drink) {
        // Si le cocktail existe déjà, on passe au suivant
        if (cocktailRepository.findAll().stream()
                .anyMatch(c -> c.getName().equalsIgnoreCase(drink.getStrDrink()))) return;

        // Je récupère ou crée la catégorie selon le champ strAlcoholic de l'API
        String categoryName = "Non alcoholic".equals(drink.getStrAlcoholic())
                ? "Sans alcool" : drink.getStrCategory();
        Category category = categoryRepository.findByName(categoryName)
                .orElseGet(() -> categoryRepository.save(
                    Category.builder().name(categoryName).description("").build()));

        Cocktail cocktail = Cocktail.builder()
                .name(drink.getStrDrink())
                .description(drink.getStrInstructions())
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
                    .ingredient(ingredient)
                    .unit(i < measures.size() ? measures.get(i) : "")
                    .build());
        }

        cocktailRepository.save(cocktail);
    }
}