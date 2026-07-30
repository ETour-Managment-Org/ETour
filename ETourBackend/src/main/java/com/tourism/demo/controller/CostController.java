package com.tourism.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tourism.demo.dto.request.CostRequest;
import com.tourism.demo.dto.response.CostResponse;
import com.tourism.demo.service.CostService;

@RestController
@RequestMapping("/api/costs")
public class CostController {

    private final CostService costService;

    public CostController(CostService costService) {
        this.costService = costService;
    }

    @PostMapping
    public ResponseEntity<CostResponse> createCost(
            @RequestBody CostRequest request) {

        CostResponse response = costService.createCost(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<CostResponse>> getAllCosts() {

        return ResponseEntity.ok(
                costService.getAllCosts()
        );
    }

    @GetMapping("/{costId}")
    public ResponseEntity<CostResponse> getCostById(
            @PathVariable("costId") Integer costId) {

        return ResponseEntity.ok(
                costService.getCostById(costId)
        );
    }

    @PutMapping("/{costId}")
    public ResponseEntity<CostResponse> updateCost(
            @PathVariable("costId") Integer costId,
            @RequestBody CostRequest request) {

        return ResponseEntity.ok(
                costService.updateCost(costId, request)
        );
    }

    @DeleteMapping("/{costId}")
    public ResponseEntity<Void> deleteCost(
            @PathVariable("costId") Integer costId) {

        costService.deleteCost(costId);

        return ResponseEntity.noContent().build();
    }
}