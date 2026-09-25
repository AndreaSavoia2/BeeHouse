package com.prj.beehouse.service;

import com.prj.beehouse.entity.Category;
import com.prj.beehouse.entity.User;
import com.prj.beehouse.exception.ConflictException;
import com.prj.beehouse.exception.GenericException;
import com.prj.beehouse.exception.ResourceNotFoundException;
import com.prj.beehouse.payload.response.CategoryResponse;
import com.prj.beehouse.payload.response.ListCategoryResponse;
import com.prj.beehouse.repository.CategoryRepository;
import com.prj.beehouse.repository.TransactionRepository;
import com.prj.beehouse.security.CustomUserDetails;
import com.prj.beehouse.util.StringUtility;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public CategoryResponse createCategory(String categoryName) {
        categoryName = StringUtility.cleanString(categoryName);
        if (categoryRepository.existsByCategoryName(categoryName))
            throw new ConflictException("Category", "Name", categoryName);

        Category category = Category.builder()
                .categoryName(categoryName)
                .build();
        categoryRepository.save(category);
        return CategoryResponse.toResponse(category);
    }

    public Set<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponse::toResponse)
                .collect(Collectors.toSet());
    }

    @Transactional
    public Set<CategoryResponse> deleteCategory(int id) {
        Category category = findCategoryById(id);
        transactionRepository.clearCategory(category.getId());
        categoryRepository.delete(category);
        return getAllCategories();
    }

    @Transactional
    public Set<CategoryResponse> updateCategory(String oldCategoryName, String newCategoryName) {

        if (categoryRepository.existsByCategoryName(newCategoryName))
            throw new ConflictException("Category", "Name", newCategoryName);

        Category category = findCategoryByCategoryName(oldCategoryName);
        category.setCategoryName(newCategoryName);
        return getAllCategories();
    }

    protected Category findCategoryByCategoryName(String categoryName) {
        return categoryRepository.findByCategoryName(categoryName)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "name", categoryName));
    }

    protected Category findCategoryById(int id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
    }
}
