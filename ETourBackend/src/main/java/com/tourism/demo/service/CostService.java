package com.tourism.demo.service;



import java.util.List;

import com.tourism.demo.dto.request.CostRequest;
import com.tourism.demo.dto.response.CostResponse;

public interface CostService {

    CostResponse createCost(CostRequest request);

    List<CostResponse> getAllCosts();

    CostResponse getCostById(Integer costId);

    CostResponse updateCost(Integer costId, CostRequest request);

    void deleteCost(Integer costId);
}