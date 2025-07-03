package com.example.bibliotheque.repository;

import com.example.bibliotheque.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}