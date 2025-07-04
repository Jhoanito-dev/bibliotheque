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
    private String motDePasse; // Nouveau champ pour le mot de passe haché
    private String role; // Nouveau champ pour le rôle (USER ou ADMIN)
    private Integer age;
    private LocalDate penaliteJusquAu;
    private String categorie; // ex: "Etudiant", "Professeur", "Autre"
    private int quotaPret; // Quota de prêts actifs
    private int quotaReservation; // Quota de réservations actives
    private int quotaProlongement; // Quota de prolongements autorisés
}