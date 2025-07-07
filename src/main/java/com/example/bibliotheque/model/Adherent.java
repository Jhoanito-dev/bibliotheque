package com.example.bibliotheque.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Adherent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom;
    private String email;
    private String motDePasse;
    private String role; // USER ou ADMIN
    
    // Champs spécifiques aux adhérents (non utilisés pour les admins)
    private Integer age;
    private LocalDate penaliteJusquAu;
    private String categorie; // ex: "Etudiant", "Professeur", "Autre"
    private Integer quotaPret; // Changé en Integer pour pouvoir être null
    private Integer quotaReservation;
    private Integer quotaProlongement;

    // Relation OneToMany avec Pret
    @OneToMany(mappedBy = "adherent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Pret> prets;

    // Relation OneToMany avec Reservation
    @OneToMany(mappedBy = "adherent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reservation> reservations;

    // Méthode pour vérifier si c'est un adhérent (et non un admin)
    public boolean isAdherent() {
        return "USER".equals(this.role);
    }
}