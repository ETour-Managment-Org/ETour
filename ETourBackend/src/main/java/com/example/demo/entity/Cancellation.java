package com.tourism.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cancellation")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cancellation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cancellation_id")
    private Integer cancellationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @Column(name = "cancellation_date")
    private LocalDateTime cancellationDate;

    @Column(name = "reason")
    private String reason;

    @Column(name = "refund_amount")
    private BigDecimal refundAmount;

    @Column(name = "refund_status")
    private String refundStatus;

    @Column(name = "remarks")
    private String remarks;
}
