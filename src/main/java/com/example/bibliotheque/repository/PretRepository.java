package com.example.bibliotheque.repository;

import com.example.bibliotheque.model.Pret;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PretRepository extends JpaRepository<Pret, Long> {
    List<Pret> findByAdherentIdAndDateRetourEffectifIsNull(Long id);
}