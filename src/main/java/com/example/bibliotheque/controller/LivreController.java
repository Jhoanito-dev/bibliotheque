package com.example.bibliotheque.controller;

import com.example.bibliotheque.model.Livre;
import com.example.bibliotheque.repository.LivreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/livres")
public class LivreController {
    @Autowired
    private LivreRepository livreRepository;

    @GetMapping
    public List<Livre> getAllLivres() {
        return livreRepository.findAll();
    }

    @GetMapping("/search")
    public List<Livre> searchLivres(@RequestParam String titre) {
        return livreRepository.findByTitreContainingIgnoreCase(titre);
    }
}