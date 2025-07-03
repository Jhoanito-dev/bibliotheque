package com.example.bibliotheque.repository;

import com.example.bibliotheque.model.Livre;
import com.example.bibliotheque.model.Pret;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PretRepository extends JpaRepository<Pret, Long> {

    List<Livre> findByAdherentIdAndDateRetourEffectifIsNull(Long id);
}