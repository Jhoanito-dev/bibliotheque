package com.example.bibliotheque.repository;

import com.example.bibliotheque.model.Livre;
import com.example.bibliotheque.model.Reservation;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Livre> findByAdherentIdAndEstActifTrue(Long id);

    List<Reservation> findByEstActifTrueAndDateLimiteRetrait(LocalDate now);
}