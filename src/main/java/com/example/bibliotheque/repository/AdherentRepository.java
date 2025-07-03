package com.example.bibliotheque.repository;

import com.example.bibliotheque.model.Adherent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdherentRepository extends JpaRepository<Adherent, Long> {
    Adherent findByEmail(String email);
}