package com.etour.dto.booking;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancellationResponseDTO {
    private Integer cancellationId;
    private Integer bookingId;
    private String bookingStatus;
    private LocalDateTime cancellationDate;
    private String reason;
    private BigDecimal amountPaid;
    private BigDecimal refundAmount;
    private String refundStatus;
    private String remarks;
}
