package com.example.demo.entity;



import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "passenger_detail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PassengerDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pax_id")
    private Integer paxId;

    // FK -> booking_header.booking_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private BookingHeader booking;

    @Column(name = "pax_name")
    private String paxName;

    @Column(name = "pax_birthdate")
    private LocalDate paxBirthdate;

    @Column(name = "pax_type")
    private String paxType;

    @Column(name = "pax_amount", precision = 10, scale = 2)
    private BigDecimal paxAmount;
}
