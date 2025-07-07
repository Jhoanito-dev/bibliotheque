package com.example.bibliotheque.repository;

import com.example.bibliotheque.model.Pret;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;

public interface PretRepository extends JpaRepository<Pret, Long> {
    // Trouver les prêts non retournés
    List<Pret> findByDateRetourEffectifIsNull();
    
    // Trouver les prêts non retournés pour un adhérent spécifique
    List<Pret> findByAdherentIdAndDateRetourEffectifIsNull(Long adherentId);
    
    // Trouver les prêts en retard
    @Query("SELECT p FROM Pret p WHERE p.dateRetourPrevus < CURRENT_DATE AND p.dateRetourEffectif IS NULL")
    List<Pret> findOverduePrets();
    
    // Compter les prêts actifs d'un adhérent
    @Query("SELECT COUNT(p) FROM Pret p WHERE p.adherent.id = ?1 AND p.dateRetourEffectif IS NULL")
    long countActivePretsByAdherentId(Long adherentId);
    
    // Trouver les prêts par période
    List<Pret> findByDateEmpruntBetween(LocalDate startDate, LocalDate endDate);
}