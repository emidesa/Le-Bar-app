package com.barapp.backend.mapper;

import com.barapp.backend.dto.response.CategoryResponse;
import com.barapp.backend.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription()
        );
    }
}
