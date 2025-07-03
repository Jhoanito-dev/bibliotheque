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
    @JoinColumn(name = "livre_id")
    private Livre livre;

    private LocalDate dateEmprunt;
    private LocalDate dateRetourPrevus;
    private LocalDate dateRetourEffectif;
    private boolean penaliteActive;
    private boolean prolonge; // Indique si le prêt a été prolongé
    private int nombreProlongements; // Nombre de prolongations effectuées
    private String typePret; // "surplace" ou "maison"
}