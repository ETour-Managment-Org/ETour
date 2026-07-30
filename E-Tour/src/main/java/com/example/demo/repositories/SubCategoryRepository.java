package com.example.demo.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entities.SubCategoryMaster;

public interface SubCategoryRepository extends JpaRepository<SubCategoryMaster, Integer> {

    List<SubCategoryMaster> findByCategory_CategoryIdAndIsactiveTrue(Integer categoryId);
}
