package com.example.demo.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "booking_header")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Integer bookingId;

    @Column(name = "booking_date")
    private LocalDate bookingDate;

//    // FK -> customer table
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "cust_id", nullable = false)
//    private CustomerMaster customer;
//
//    // FK -> tour table
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "tour_id", nullable = false)
//    private TourMaster tour;
//
//    // FK -> departure table
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "departure_id", nullable = false)
//    private DepartureMaster departure;

    @Column(name = "no_of_pax")
    private Integer noOfPax;

    @Column(name = "tour_amount", precision = 10, scale = 2)
    private BigDecimal tourAmount;

    @Column(name = "taxes", precision = 10, scale = 2)
    private BigDecimal taxes;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    // One BookingHeader -> Many PassengerDetails
    @OneToMany(
        mappedBy = "booking",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<PassengerDetail> passengers = new ArrayList<>();
}