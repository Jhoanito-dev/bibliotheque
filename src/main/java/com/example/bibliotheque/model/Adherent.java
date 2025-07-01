package com.example.bibliotheque.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
public class Adherent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom;
    private String email;
    private String typeProfil; // étudiant, professionnel, professeur
    private boolean cotisationPayee;
    private LocalDate dateExpirationCotisation;
    private String penalites; // ex: "Interdiction jusqu'au 30/06/2025"
}