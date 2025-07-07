package com.example.bibliotheque.repository;

import com.example.bibliotheque.model.Livre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface LivreRepository extends JpaRepository<Livre, Long> {
    List<Livre> findByTitreContainingIgnoreCase(String titre);
    
    // Méthode pour trouver les livres disponibles
    List<Livre> findByDisponibleTrue();
    
    // Méthode pour trouver les livres par catégorie
    List<Livre> findByCategorie(String categorie);
    
    // Méthode pour trouver les livres par tranche d'âge
    @Query("SELECT l FROM Livre l WHERE l.ageMinimum <= ?1 OR l.ageMinimum IS NULL")
    List<Livre> findByAgeMaximum(Integer age);
}