package com.example.bibliotheque.repository;

import com.example.bibliotheque.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByAdherentIdAndEstActifTrue(Long id);

    List<Reservation> findByEstActifTrueAndDateLimiteRetrait(LocalDate date);
}