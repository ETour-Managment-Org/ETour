package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "language")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Language {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "language_id")
    @EqualsAndHashCode.Include
    private Integer languageId;

    @Column(name = "language_code", length = 10)
    private String languageCode;

    @Column(name = "language_name", length = 60)
    private String languageName;
}
