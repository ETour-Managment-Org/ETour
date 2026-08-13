package com.etour.services;

import java.util.List;

import com.etour.dto.feedback.FeedbackRequestDTO;
import com.etour.dto.feedback.FeedbackResponseDTO;
import com.etour.dto.feedback.PublicFeedbackDTO;

public interface FeedbackService {
    FeedbackResponseDTO submit(FeedbackRequestDTO request, String username);

    List<FeedbackResponseDTO> getAll(String status);

    FeedbackResponseDTO updateStatus(Integer feedbackId, String status);

    FeedbackResponseDTO setPublished(Integer feedbackId, boolean published);

    List<PublicFeedbackDTO> getPublished();

    long countNew();
}
