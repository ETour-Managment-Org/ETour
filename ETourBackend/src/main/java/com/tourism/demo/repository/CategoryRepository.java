package com.tourism.demo.repository;

import com.tourism.demo.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    
    // Custom finder method to filter categories by status (e.g., "ACTIVE")
    List<Category> findByStatus(String status);
}