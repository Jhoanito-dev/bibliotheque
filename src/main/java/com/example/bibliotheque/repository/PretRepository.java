package com.example.bibliotheque.repository;

import com.example.bibliotheque.model.Pret;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PretRepository extends JpaRepository<Pret, Long> {
}