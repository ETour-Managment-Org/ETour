package com.etour.repositories;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.etour.entities.Tour;

public interface TourRepository extends JpaRepository<Tour, Integer> {
    List<Tour> findByCategory_CategoryId(Integer categoryId);

    List<Tour> findBySubCategory_SubcatId(Integer subcatId);

    @Query("""
           select distinct t
             from Tour t
             left join t.schedules s
             left join t.costs c
             left join t.cities city
            where (:startDate   is null or s.startDate  >= :startDate)
              and (:endDate     is null or s.startDate  <= :endDate)
              and (:minPrice    is null or c.adultPrice >= :minPrice)
              and (:maxPrice    is null or c.adultPrice <= :maxPrice)
              and (:minDuration is null or t.days       >= :minDuration)
              and (:maxDuration is null or t.days       <= :maxDuration)
              and (:city        is null
                   or lower(city.cityName) like lower(concat('%', :city, '%'))
                   or lower(t.destination) like lower(concat('%', :city, '%'))
                   or lower(t.location)    like lower(concat('%', :city, '%')))
            order by t.tourId
           """)
    List<Tour> searchTours(@Param("startDate") LocalDate startDate,
                           @Param("endDate") LocalDate endDate,
                           @Param("minPrice") BigDecimal minPrice,
                           @Param("maxPrice") BigDecimal maxPrice,
                           @Param("minDuration") Integer minDuration,
                           @Param("maxDuration") Integer maxDuration,
                           @Param("city") String city);

    @Query("""
           select distinct t
             from Tour t
             left join t.cities city
            where lower(city.cityName) like lower(concat('%', :city, '%'))
               or lower(t.destination) like lower(concat('%', :city, '%'))
               or lower(t.location)    like lower(concat('%', :city, '%'))
            order by t.tourId
           """)
    List<Tour> findByCity(@Param("city") String city);

    @Query("""
           select distinct c.cityName
             from TourCity c
            where c.cityName is not null
            order by c.cityName
           """)
    List<String> findAllCityNames();

    boolean existsByTourNameIgnoreCase(String tourName);
}
