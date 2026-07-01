package com.barapp.backend.mapper;

import com.barapp.backend.dto.response.CocktailResponse;
import com.barapp.backend.entity.*;
import com.barapp.backend.enums.CocktailSizeEnum;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class CocktailMapperTest {

    private final CategoryMapper categoryMapper = new CategoryMapper();
    private final CocktailMapper cocktailMapper = new CocktailMapper(categoryMapper);

    @Test
    void toResponse_avec_categorie_et_tailles() {
        Category category = Category.builder().id(1L).name("Classiques").description("").build();
        CocktailSize size = CocktailSize.builder().id(1L).size(CocktailSizeEnum.M).price(BigDecimal.valueOf(9.5)).build();
        Cocktail cocktail = Cocktail.builder()
                .id(1L).name("Mojito").description("Menthe et citron").imageUrl(null)
                .category(category).sizes(new ArrayList<>()).ingredients(new ArrayList<>()).build();
        cocktail.getSizes().add(size);

        CocktailResponse response = cocktailMapper.toResponse(cocktail);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Mojito");
        assertThat(response.getDescription()).isEqualTo("Menthe et citron");
        assertThat(response.getCategory().getName()).isEqualTo("Classiques");
        assertThat(response.getSizes()).hasSize(1);
        assertThat(response.getSizes().get(0).getPrice()).isEqualByComparingTo(BigDecimal.valueOf(9.5));
        assertThat(response.getIngredients()).isEmpty();
    }

    @Test
    void toResponse_sans_categorie_retourne_null() {
        Cocktail cocktail = Cocktail.builder()
                .id(2L).name("Spritz").description(null).imageUrl(null)
                .category(null).sizes(new ArrayList<>()).ingredients(new ArrayList<>()).build();

        CocktailResponse response = cocktailMapper.toResponse(cocktail);

        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getCategory()).isNull();
        assertThat(response.getSizes()).isEmpty();
        assertThat(response.getIngredients()).isEmpty();
    }
}
