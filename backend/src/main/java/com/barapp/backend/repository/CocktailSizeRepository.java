package com.barapp.backend.repository;

import com.barapp.backend.entity.CocktailSize;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CocktailSizeRepository extends JpaRepository<CocktailSize, Long> {
}