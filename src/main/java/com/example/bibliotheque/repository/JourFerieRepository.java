package com.example.bibliotheque.repository;

import com.example.bibliotheque.model.JourFerie;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface JourFerieRepository extends JpaRepository<JourFerie, Long> {
    List<JourFerie> findByDateBetween(LocalDate startDate, LocalDate endDate);
}