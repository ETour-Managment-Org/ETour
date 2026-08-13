package com.etour.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.etour.entities.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    List<Category> findByStatus(String status);

    Optional<Category> findByCatCode(String catCode);

    List<Category> findByParentIsNullOrderByCategoryIdAsc();

    List<Category> findByParent_CategoryIdOrderByCategoryIdAsc(Integer parentId);

    List<Category> findByFlagTrue();

    @Query("select count(c) from Category c where c.parent.categoryId = :parentId")
    long countChildren(Integer parentId);
}
