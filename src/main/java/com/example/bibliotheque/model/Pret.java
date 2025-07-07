package com.example.bibliotheque.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
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
    private String typePret; // "maison" ou "surplace"
    private boolean penaliteActive;
    private boolean prolonge;
    private int nombreProlongements;
}