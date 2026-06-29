package com.barapp.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barapp.backend.entity.Cocktail;

public interface CocktailRepository extends JpaRepository<Cocktail, Long> {
    List<Cocktail> findByCategoryId(Long categoryId);
}