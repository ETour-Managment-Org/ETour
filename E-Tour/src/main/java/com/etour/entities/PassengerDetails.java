package com.etour.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "passenger_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PassengerDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pax_id")
    @EqualsAndHashCode.Include
    private Integer paxId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    @ToString.Exclude
    private Booking booking;

    @Column(name = "full_name", length = 120)
    private String fullName;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "age")
    private Integer age;

    @Column(name = "passport_number", length = 30)
    private String passportNumber;

    @Column(name = "pax_type", length = 20)
    private String paxType;
}
