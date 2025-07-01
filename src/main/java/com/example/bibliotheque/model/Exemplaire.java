package com.example.bibliotheque.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Exemplaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String numeroUnique; // ex: LIVRE001-EX01
    private String statut; // disponible, emprunté, sur place, en réparation
    @ManyToOne
    @JoinColumn(name = "livre_id")
    private Livre livre;
}