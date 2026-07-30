package com.example.demo.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.category.BreadcrumbDTO;
import com.example.demo.dto.category.CategoryDTO;
import com.example.demo.dto.category.SubCategoryDTO;
import com.example.demo.services.CategoryService;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/roots")
    public ResponseEntity<List<CategoryDTO>> getRootCategories() {
        return new ResponseEntity<>(categoryService.getRootCategories(), HttpStatus.OK);
    }

    @GetMapping("/{categoryId}/children")
    public ResponseEntity<List<CategoryDTO>> getChildren(@PathVariable Integer categoryId) {
        return new ResponseEntity<>(categoryService.getChildren(categoryId), HttpStatus.OK);
    }

    @GetMapping("/{categoryId}/breadcrumb")
    public ResponseEntity<List<BreadcrumbDTO>> getBreadcrumb(@PathVariable Integer categoryId) {
        return new ResponseEntity<>(categoryService.getBreadcrumb(categoryId), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        return new ResponseEntity<>(categoryService.getAllCategories(), HttpStatus.OK);
    }

    @GetMapping("/code/{catCode}")
    public ResponseEntity<CategoryDTO> getCategoryByCode(@PathVariable String catCode) {
        return new ResponseEntity<>(categoryService.getCategoryByCode(catCode), HttpStatus.OK);
    }

    @GetMapping("/subcategories")
    public ResponseEntity<List<SubCategoryDTO>> getAllSubCategories() {
        return new ResponseEntity<>(categoryService.getAllSubCategories(), HttpStatus.OK);
    }

    @GetMapping("/{categoryId}/subcategories")
    public ResponseEntity<List<SubCategoryDTO>> getSubCategories(@PathVariable Integer categoryId) {
        return new ResponseEntity<>(categoryService.getSubCategories(categoryId), HttpStatus.OK);
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryDTO> getCategory(@PathVariable Integer categoryId) {
        return new ResponseEntity<>(categoryService.getCategory(categoryId), HttpStatus.OK);
    }
}
