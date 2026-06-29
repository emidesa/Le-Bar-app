package com.barapp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barapp.backend.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
