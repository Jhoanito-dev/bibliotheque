package com.example.bibliotheque.repository;

import com.example.bibliotheque.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByAdherentIdAndEstActifTrue(Long id);

    List<Reservation> findByEstActifTrueAndDateLimiteRetrait(LocalDate date);
    
    // Méthode pour compter les réservations actives d'un adhérent
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.adherent.id = ?1 AND r.estActif = true")
    long countActiveReservationsByAdherentId(Long adherentId);
    
    // Méthode pour trouver les réservations expirées
    @Query("SELECT r FROM Reservation r WHERE r.estActif = true AND r.dateLimiteRetrait < CURRENT_DATE")
    List<Reservation> findExpiredReservations();
}