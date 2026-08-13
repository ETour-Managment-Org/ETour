package com.etour.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.etour.common.FeedbackCategory;
import com.etour.dto.feedback.FeedbackRequestDTO;
import com.etour.dto.feedback.FeedbackResponseDTO;
import com.etour.dto.feedback.PublicFeedbackDTO;
import com.etour.services.FeedbackService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {
    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping
    public ResponseEntity<FeedbackResponseDTO> submit(
            @Valid @RequestBody FeedbackRequestDTO request,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;

        return new ResponseEntity<>(
                feedbackService.submit(request, username), HttpStatus.CREATED);
    }

    @GetMapping("/published")
    public ResponseEntity<List<PublicFeedbackDTO>> published() {
        return new ResponseEntity<>(feedbackService.getPublished(), HttpStatus.OK);
    }

    @GetMapping("/categories")
    public ResponseEntity<Map<String, Object>> categories() {
        return new ResponseEntity<>(Map.of("categories", FeedbackCategory.ALL), HttpStatus.OK);
    }
}
