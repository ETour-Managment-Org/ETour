package com.tourism.demo.service;

import com.tourism.demo.dto.request.CategoryRequest;
import com.tourism.demo.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    CategoryResponse createCategory(CategoryRequest categoryRequest);
    CategoryResponse updateCategory(Integer categoryId, CategoryRequest categoryRequest);
    CategoryResponse getCategoryById(Integer categoryId);
    List<CategoryResponse> getAllCategories();
    void deleteCategory(Integer categoryId);
}