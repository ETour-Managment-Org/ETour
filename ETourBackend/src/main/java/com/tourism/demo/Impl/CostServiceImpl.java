package com.tourism.demo.Impl;


import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.tourism.demo.dto.request.CostRequest;
import com.tourism.demo.dto.response.CostResponse;
import com.tourism.demo.entity.Cost;
import com.tourism.demo.entity.Tour;
import com.tourism.demo.repository.CostRepository;
import com.tourism.demo.repository.TourRepository;
import com.tourism.demo.service.CostService;

@Service
public class CostServiceImpl implements CostService {

    private final CostRepository costRepository;
    private final TourRepository tourRepository;

    // Constructor Injection
    public CostServiceImpl(
            CostRepository costRepository,
            TourRepository tourRepository) {

        this.costRepository = costRepository;
        this.tourRepository = tourRepository;
    }

    // CREATE COST
    @Override
    public CostResponse createCost(CostRequest request) {

        // Tour must already exist before Cost can be created
        Tour tour = tourRepository
                .findById(request.getTourId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Tour not found with id: "
                                        + request.getTourId()
                        )
                );

        Cost cost = new Cost();

        cost.setTour(tour);
        cost.setAdultPrice(request.getAdultPrice());
        cost.setSinglePersonPrice(request.getSinglePersonPrice());
        cost.setExtraPersonPrice(request.getExtraPersonPrice());
        cost.setChildWithBedPrice(request.getChildWithBedPrice());
        cost.setChildWithoutBedPrice(request.getChildWithoutBedPrice());
        cost.setValidFrom(request.getValidFrom());
        cost.setValidTo(request.getValidTo());
        cost.setIsActive(request.getIsActive());

        Cost savedCost = costRepository.save(cost);

        return convertToResponse(savedCost);
    }

    // GET ALL COSTS
    @Override
    public List<CostResponse> getAllCosts() {

        return costRepository
                .findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // GET COST BY ID
    @Override
    public CostResponse getCostById(Integer costId) {

        Cost cost = costRepository
                .findById(costId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Cost not found with id: " + costId
                        )
                );

        return convertToResponse(cost);
    }

    // UPDATE COST
    @Override
    public CostResponse updateCost(
            Integer costId,
            CostRequest request) {

        // First check whether Cost exists
        Cost cost = costRepository
                .findById(costId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Cost not found with id: " + costId
                        )
                );

        // Check whether requested Tour exists
        Tour tour = tourRepository
                .findById(request.getTourId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Tour not found with id: "
                                        + request.getTourId()
                        )
                );

        cost.setTour(tour);
        cost.setAdultPrice(request.getAdultPrice());
        cost.setSinglePersonPrice(request.getSinglePersonPrice());
        cost.setExtraPersonPrice(request.getExtraPersonPrice());
        cost.setChildWithBedPrice(request.getChildWithBedPrice());
        cost.setChildWithoutBedPrice(request.getChildWithoutBedPrice());
        cost.setValidFrom(request.getValidFrom());
        cost.setValidTo(request.getValidTo());
        cost.setIsActive(request.getIsActive());

        Cost updatedCost = costRepository.save(cost);

        return convertToResponse(updatedCost);
    }

    // DELETE COST
    @Override
    public void deleteCost(Integer costId) {

        Cost cost = costRepository
                .findById(costId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Cost not found with id: " + costId
                        )
                );

        costRepository.delete(cost);
    }

    // CONVERT ENTITY TO RESPONSE DTO
    private CostResponse convertToResponse(Cost cost) {

        return new CostResponse(
                cost.getCostId(),
                cost.getTour().getTourId(),
                cost.getAdultPrice(),
                cost.getSinglePersonPrice(),
                cost.getExtraPersonPrice(),
                cost.getChildWithBedPrice(),
                cost.getChildWithoutBedPrice(),
                cost.getValidFrom(),
                cost.getValidTo(),
                cost.getIsActive()
        );
    }
}