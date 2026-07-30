package com.tourism.demo.Impl;

import com.tourism.demo.dto.request.CategoryRequest;
import com.tourism.demo.dto.response.CategoryResponse;
import com.tourism.demo.entity.Category;
import com.tourism.demo.repository.CategoryRepository;
import com.tourism.demo.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryResponse createCategory(CategoryRequest categoryRequest) {
        Category category = new Category();
        category.setCategoryName(categoryRequest.getCategoryName());
        category.setDescription(categoryRequest.getDescription());
        category.setSuitableFor(categoryRequest.getSuitableFor());
        category.setStatus(categoryRequest.getStatus());

        Category savedCategory = categoryRepository.save(category);
        return mapToResponse(savedCategory);
    }

    @Override
    public CategoryResponse updateCategory(Integer categoryId, CategoryRequest categoryRequest) {
        Category existingCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));

        existingCategory.setCategoryName(categoryRequest.getCategoryName());
        existingCategory.setDescription(categoryRequest.getDescription());
        existingCategory.setSuitableFor(categoryRequest.getSuitableFor());
        existingCategory.setStatus(categoryRequest.getStatus());

        Category updatedCategory = categoryRepository.save(existingCategory);
        return mapToResponse(updatedCategory);
    }

    @Override
    public CategoryResponse getCategoryById(Integer categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));
        return mapToResponse(category);
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteCategory(Integer categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));
        categoryRepository.delete(category);
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .categoryId(category.getCategoryId())
                .categoryName(category.getCategoryName())
                .description(category.getDescription())
                .suitableFor(category.getSuitableFor())
                .status(category.getStatus())
                .build();
    }
}