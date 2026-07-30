package com.example.demo.repositories;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entities.Tour;

public interface TourRepository extends JpaRepository<Tour, Integer> {

    List<Tour> findByCategory_CategoryId(Integer categoryId);

    List<Tour> findBySubCategory_SubcatId(Integer subcatId);

    @Query("""
           select distinct t
             from Tour t
             left join t.schedules s
             left join t.costs c
            where (:startDate   is null or s.startDate  >= :startDate)
              and (:endDate     is null or s.startDate  <= :endDate)
              and (:minPrice    is null or c.adultPrice >= :minPrice)
              and (:maxPrice    is null or c.adultPrice <= :maxPrice)
              and (:minDuration is null or t.days       >= :minDuration)
              and (:maxDuration is null or t.days       <= :maxDuration)
            order by t.tourId
           """)
    List<Tour> searchTours(@Param("startDate") LocalDate startDate,
                           @Param("endDate") LocalDate endDate,
                           @Param("minPrice") BigDecimal minPrice,
                           @Param("maxPrice") BigDecimal maxPrice,
                           @Param("minDuration") Integer minDuration,
                           @Param("maxDuration") Integer maxDuration);
}
