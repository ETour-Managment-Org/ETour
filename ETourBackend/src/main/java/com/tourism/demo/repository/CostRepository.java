package com.tourism.demo.repository;

import com.tourism.demo.entity.Cost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CostRepository extends JpaRepository<Cost, Integer> {

}