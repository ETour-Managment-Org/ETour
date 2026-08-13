package com.etour.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.etour.common.FeedbackCategory;
import com.etour.dto.feedback.FeedbackRequestDTO;
import com.etour.dto.feedback.FeedbackResponseDTO;
import com.etour.dto.feedback.PublicFeedbackDTO;
import com.etour.entities.Feedback;
import com.etour.entities.User;
import com.etour.exceptions.ResourceNotFoundException;
import com.etour.repositories.FeedbackRepository;
import com.etour.repositories.UserRepository;

@Service
public class FeedbackServiceImpl implements FeedbackService {
    private static final Logger log = LoggerFactory.getLogger(FeedbackServiceImpl.class);

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;

    public FeedbackServiceImpl(FeedbackRepository feedbackRepository,
                               UserRepository userRepository) {
        this.feedbackRepository = feedbackRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public FeedbackResponseDTO submit(FeedbackRequestDTO request, String username) {
        if (!FeedbackCategory.isValidCategory(request.getCategory())) {
            throw new IllegalArgumentException(
                    "category must be one of " + FeedbackCategory.ALL);
        }

        User user = username == null ? null
                : userRepository.findByUsername(username).orElse(null);

        String name = request.getName();
        String email = request.getEmail();

        if (user != null) {
            String fn = user.getFirstName() != null ? user.getFirstName() : "";
            String ln = user.getLastName() != null ? user.getLastName() : "";
            String full = (fn + " " + ln).trim();
            name = full.isEmpty() ? user.getUsername() : full;
            email = user.getEmail();
        }

        Feedback feedback = feedbackRepository.save(Feedback.builder()
                .user(user)
                .name(name)
                .email(email)
                .category(request.getCategory().trim().toUpperCase())
                .rating(request.getRating())
                .message(request.getMessage())
                .pageUrl(request.getPageUrl())
                .status(FeedbackCategory.NEW)

                .published(Boolean.FALSE)
                .createdAt(LocalDateTime.now())
                .build());

        log.info("Website feedback #{} received: {} from {}",
                feedback.getFeedbackId(), feedback.getCategory(),
                user != null ? user.getUsername() : "a guest");

        return toDTO(feedback);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackResponseDTO> getAll(String status) {
        List<Feedback> rows = (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status))
                ? feedbackRepository.findAllByOrderByFeedbackIdDesc()
                : feedbackRepository.findByStatusOrderByFeedbackIdDesc(status.toUpperCase());

        return rows.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FeedbackResponseDTO updateStatus(Integer feedbackId, String status) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback", feedbackId));

        feedback.setStatus(status == null ? FeedbackCategory.NEW : status.toUpperCase());
        feedbackRepository.save(feedback);
        return toDTO(feedback);
    }

    @Override
    @Transactional
    public FeedbackResponseDTO setPublished(Integer feedbackId, boolean published) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback", feedbackId));

        feedback.setPublished(published);

        if (published && FeedbackCategory.NEW.equals(feedback.getStatus())) {
            feedback.setStatus(FeedbackCategory.REVIEWED);
        }

        feedbackRepository.save(feedback);
        log.info("Feedback #{} {} the public site", feedbackId,
                published ? "published to" : "removed from");

        return toDTO(feedback);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PublicFeedbackDTO> getPublished() {
        return feedbackRepository.findByPublishedTrueOrderByFeedbackIdDesc()
                .stream().map(this::toPublicDTO).collect(Collectors.toList());
    }

    private String displayName(String name) {
        if (name == null || name.isBlank()) {
            return "A visitor";
        }

        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0];
        }

        String last = parts[parts.length - 1];
        return parts[0] + " " + last.charAt(0) + ".";
    }

    private PublicFeedbackDTO toPublicDTO(Feedback f) {
        return PublicFeedbackDTO.builder()
                .feedbackId(f.getFeedbackId())
                .name(displayName(f.getName()))
                .category(f.getCategory())
                .rating(f.getRating())
                .message(f.getMessage())

                .submittedOn(f.getCreatedAt() == null ? null : f.getCreatedAt().toLocalDate())
                .fromRegisteredUser(f.getUser() != null)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public long countNew() {
        return feedbackRepository.countByStatus(FeedbackCategory.NEW);
    }

    private FeedbackResponseDTO toDTO(Feedback f) {
        return FeedbackResponseDTO.builder()
                .feedbackId(f.getFeedbackId())
                .name(f.getName())
                .email(f.getEmail())
                .category(f.getCategory())
                .rating(f.getRating())
                .message(f.getMessage())
                .status(f.getStatus())
                .pageUrl(f.getPageUrl())
                .createdAt(f.getCreatedAt())
                .userId(f.getUser() != null ? f.getUser().getUserId() : null)
                .username(f.getUser() != null ? f.getUser().getUsername() : null)
                .fromRegisteredUser(f.getUser() != null)
                .published(Boolean.TRUE.equals(f.getPublished()))
                .build();
    }
}
