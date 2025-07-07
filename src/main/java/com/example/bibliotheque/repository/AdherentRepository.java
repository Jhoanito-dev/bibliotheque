package com.example.bibliotheque.repository;

import com.example.bibliotheque.model.Adherent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface AdherentRepository extends JpaRepository<Adherent, Long> {
    Optional<Adherent> findByEmail(String email);
    
    // Nouvelle méthode pour trouver les adhérents par rôle
    List<Adherent> findByRole(String role);
    
    // Méthode pour trouver les adhérents avec pénalité active
    @Query("SELECT a FROM Adherent a WHERE a.penaliteJusquAu IS NOT NULL AND a.penaliteJusquAu > CURRENT_DATE")
    List<Adherent> findAdherentsWithActivePenalite();
}