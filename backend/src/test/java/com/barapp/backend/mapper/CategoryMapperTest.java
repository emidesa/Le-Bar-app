package com.barapp.backend.mapper;

import com.barapp.backend.dto.response.CategoryResponse;
import com.barapp.backend.entity.Category;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryMapperTest {

    private final CategoryMapper mapper = new CategoryMapper();

    @Test
    void toResponse_convertit_category_en_dto() {
        Category category = Category.builder().id(1L).name("Classiques").description("Cocktails classiques").build();

        CategoryResponse response = mapper.toResponse(category);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Classiques");
        assertThat(response.getDescription()).isEqualTo("Cocktails classiques");
    }

    @Test
    void toResponse_avec_description_nulle() {
        Category category = Category.builder().id(2L).name("Exotiques").description(null).build();

        CategoryResponse response = mapper.toResponse(category);

        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getDescription()).isNull();
    }
}
