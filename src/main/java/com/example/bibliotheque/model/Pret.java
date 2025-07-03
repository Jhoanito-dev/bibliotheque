package com.example.bibliotheque.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
public class Pret {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "adherent_id")
    private Adherent adherent;
    @ManyToOne
    @JoinColumn(name = "exemplaire_id")
    private Exemplaire exemplaire;
    private String typePret; // sur place, à domicile
    private LocalDate dateRetour;
    private int nombreProlongements; // Nouvel attribut pour le quota de prolongement
    private boolean prolonge;
}