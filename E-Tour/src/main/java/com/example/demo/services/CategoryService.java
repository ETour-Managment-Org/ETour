package com.example.demo.services;

import java.util.List;

import com.example.demo.dto.category.BreadcrumbDTO;
import com.example.demo.dto.category.CategoryDTO;
import com.example.demo.dto.category.SubCategoryDTO;

public interface CategoryService {

    List<CategoryDTO> getRootCategories();

    List<CategoryDTO> getChildren(Integer categoryId);

    List<BreadcrumbDTO> getBreadcrumb(Integer categoryId);

    List<CategoryDTO> getAllCategories();

    CategoryDTO getCategory(Integer categoryId);

    CategoryDTO getCategoryByCode(String catCode);

    List<SubCategoryDTO> getSubCategories(Integer categoryId);

    List<SubCategoryDTO> getAllSubCategories();
}
