package com.example.demo.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.category.BreadcrumbDTO;
import com.example.demo.dto.category.CategoryDTO;
import com.example.demo.dto.category.SubCategoryDTO;
import com.example.demo.entities.Category;
import com.example.demo.entities.SubCategoryMaster;
import com.example.demo.entities.Tour;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.repositories.CategoryRepository;
import com.example.demo.repositories.SubCategoryRepository;
import com.example.demo.repositories.TourRepository;

@Service
public class CategoryServiceImpl implements CategoryService {

    private static final String ACTION_SHOW_TOURS = "SHOW_TOURS";
    private static final String ACTION_SHOW_CATEGORIES = "SHOW_CATEGORIES";
    private static final int MAX_TREE_DEPTH = 20;

    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final TourRepository tourRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository,
                               SubCategoryRepository subCategoryRepository,
                               TourRepository tourRepository) {
        this.categoryRepository = categoryRepository;
        this.subCategoryRepository = subCategoryRepository;
        this.tourRepository = tourRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getRootCategories() {
        return decorate(categoryRepository.findByParentIsNullOrderByCategoryIdAsc());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getChildren(Integer categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category", categoryId);
        }
        return decorate(categoryRepository.findByParent_CategoryIdOrderByCategoryIdAsc(categoryId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BreadcrumbDTO> getBreadcrumb(Integer categoryId) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));

        List<BreadcrumbDTO> trail = new ArrayList<>();
        Category node = category;
        int guard = 0;

        while (node != null && guard++ < MAX_TREE_DEPTH) {
            trail.add(BreadcrumbDTO.builder()
                    .categoryId(node.getCategoryId())
                    .catCode(node.getCatCode())
                    .categoryName(node.getCategoryName())
                    .build());
            node = node.getParent();
        }

        Collections.reverse(trail);
        for (int i = 0; i < trail.size(); i++) {
            trail.get(i).setLevel(i + 1);
        }
        return trail;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        return decorate(categoryRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDTO getCategory(Integer categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));
        return decorate(List.of(category)).get(0);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryByCode(String catCode) {
        Category category = categoryRepository.findByCatCode(catCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with code " + catCode));
        return decorate(List.of(category)).get(0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubCategoryDTO> getSubCategories(Integer categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category", categoryId);
        }
        Map<Integer, Long> counts = tourCountBySubCategory();
        return subCategoryRepository.findByCategory_CategoryIdAndIsactiveTrue(categoryId)
                .stream()
                .map(s -> toDTO(s, counts.getOrDefault(s.getSubcatId(), 0L)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubCategoryDTO> getAllSubCategories() {
        Map<Integer, Long> counts = tourCountBySubCategory();
        return subCategoryRepository.findAll().stream()
                .map(s -> toDTO(s, counts.getOrDefault(s.getSubcatId(), 0L)))
                .collect(Collectors.toList());
    }

    private List<CategoryDTO> decorate(List<Category> categories) {

        Map<Integer, Long> tourCounts = tourCountByCategory();
        Map<Integer, Long> childCounts = childCountByCategory();

        return categories.stream()
                .map(c -> {
                    long children = childCounts.getOrDefault(c.getCategoryId(), 0L);
                    boolean leaf = Boolean.TRUE.equals(c.getFlag()) || children == 0;

                    return CategoryDTO.builder()
                            .categoryId(c.getCategoryId())
                            .catCode(c.getCatCode())
                            .categoryName(c.getCategoryName())
                            .catImagePath(c.getCatImagePath())
                            .description(c.getDescription())
                            .suitableFor(c.getSuitableFor())
                            .status(c.getStatus())
                            .parentId(c.getParent() != null ? c.getParent().getCategoryId() : null)
                            .parentName(c.getParent() != null ? c.getParent().getCategoryName() : null)
                            .flag(Boolean.TRUE.equals(c.getFlag()))
                            .nextAction(leaf ? ACTION_SHOW_TOURS : ACTION_SHOW_CATEGORIES)
                            .childCount(children)
                            .tourCount(tourCounts.getOrDefault(c.getCategoryId(), 0L))
                            .build();
                })
                .collect(Collectors.toList());
    }

    private Map<Integer, Long> childCountByCategory() {
        Map<Integer, Long> counts = new HashMap<>();
        for (Category c : categoryRepository.findAll()) {
            if (c.getParent() != null) {
                counts.merge(c.getParent().getCategoryId(), 1L, Long::sum);
            }
        }
        return counts;
    }

    private Map<Integer, Long> tourCountByCategory() {
        Map<Integer, Long> counts = new HashMap<>();
        for (Tour t : tourRepository.findAll()) {
            if (t.getCategory() != null) {
                counts.merge(t.getCategory().getCategoryId(), 1L, Long::sum);
            }
        }
        return counts;
    }

    private Map<Integer, Long> tourCountBySubCategory() {
        Map<Integer, Long> counts = new HashMap<>();
        for (Tour t : tourRepository.findAll()) {
            if (t.getSubCategory() != null) {
                counts.merge(t.getSubCategory().getSubcatId(), 1L, Long::sum);
            }
        }
        return counts;
    }

    private SubCategoryDTO toDTO(SubCategoryMaster s, Long tourCount) {
        return SubCategoryDTO.builder()
                .subcatId(s.getSubcatId())
                .subcatName(s.getSubcatName())
                .subcatImagePath(s.getSubcatImagePath())
                .categoryId(s.getCategory() != null ? s.getCategory().getCategoryId() : null)
                .categoryName(s.getCategory() != null ? s.getCategory().getCategoryName() : null)
                .tourCount(tourCount)
                .build();
    }
}
