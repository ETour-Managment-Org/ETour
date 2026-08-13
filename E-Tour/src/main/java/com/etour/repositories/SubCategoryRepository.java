package com.etour.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.etour.entities.SubCategoryMaster;

public interface SubCategoryRepository extends JpaRepository<SubCategoryMaster, Integer> {
    List<SubCategoryMaster> findByCategory_CategoryIdAndIsactiveTrue(Integer categoryId);
}
