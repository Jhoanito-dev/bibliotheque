package com.example.bibliotheque.repository;

import com.example.bibliotheque.model.Exemplaire;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExemplaireRepository extends JpaRepository<Exemplaire, Long> {
    List<Exemplaire> findByStatut(String statut);
}