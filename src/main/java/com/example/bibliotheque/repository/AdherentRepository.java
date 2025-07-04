package com.example.bibliotheque.repository;

import com.example.bibliotheque.model.Adherent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AdherentRepository extends JpaRepository<Adherent, Long> {
    Optional<Adherent> findByEmail(String email);
}