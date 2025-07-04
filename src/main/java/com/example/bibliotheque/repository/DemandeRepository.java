package com.example.bibliotheque.repository;

import com.example.bibliotheque.model.Demande;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DemandeRepository extends JpaRepository<Demande, Long> {
    List<Demande> findByStatut(String statut);
}