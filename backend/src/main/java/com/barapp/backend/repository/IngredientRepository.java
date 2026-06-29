package com.barapp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barapp.backend.entity.Ingredient;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
}
