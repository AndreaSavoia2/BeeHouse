package com.prj.beehouse.repository;

import com.prj.beehouse.entity.Category;
import com.prj.beehouse.payload.response.ListCategoryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.Set;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    Optional<Category> findByCategoryName(String title);

    boolean existsByCategoryName(String categoryName);
}
