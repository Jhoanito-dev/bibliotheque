package com.example.bibliotheque.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "adherent_id")
    private Adherent adherent;
    
    @ManyToOne
    @JoinColumn(name = "livre_id")
    private Livre livre;
    
    private LocalDate dateReservation;
    private LocalDate dateLimiteRetrait;
    private boolean estActif;
}