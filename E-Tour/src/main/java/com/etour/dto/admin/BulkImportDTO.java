package com.etour.dto.admin;

import java.util.ArrayList;
import java.util.List;

import lombok.*;

public class BulkImportDTO {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private List<TourRequestDTO> tours = new ArrayList<>();
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RowResult {
        private int rowNumber;
        private String tourName;
        private boolean success;
        private Integer tourId;
        private String message;
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private int total;
        private int imported;
        private int failed;
        @Builder.Default
        private List<RowResult> rows = new ArrayList<>();
    }
}
