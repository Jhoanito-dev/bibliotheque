package com.example.bibliotheque.controller;

import com.example.bibliotheque.model.Livre;
import com.example.bibliotheque.repository.LivreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.http.ResponseEntity;
import com.example.bibliotheque.repository.AdherentRepository;
import com.example.bibliotheque.repository.PretRepository;
import com.example.bibliotheque.repository.ReservationRepository;
import com.example.bibliotheque.model.Adherent;
import com.example.bibliotheque.model.Pret;
import com.example.bibliotheque.model.Reservation;

@RestController
@RequestMapping("/api/livres")
public class LivreController {
    @Autowired
    private LivreRepository livreRepository;
    @Autowired
    private AdherentRepository adherentRepository;
    @Autowired
    private PretRepository pretRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    @GetMapping
    public List<Livre> getAllLivres() {
        return livreRepository.findAll();
    }

    @GetMapping("/search")
    public List<Livre> searchLivres(@RequestParam String titre) {
        return livreRepository.findByTitreContainingIgnoreCase(titre);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLivreById(@PathVariable Long id) {
        Livre livre = livreRepository.findById(id).orElse(null);
        if (livre == null) {
            return ResponseEntity.notFound().build();
        }
        // Générer la liste des exemplaires avec leur disponibilité
        int total = livre.getNombreExemplaires();
        List<Object> exemplaires = new java.util.ArrayList<>();
        for (int i = 1; i <= total; i++) {
            exemplaires.add(java.util.Map.of(
                "numero", i,
                "disponible", livre.isDisponible()
            ));
        }
        java.util.Map<String, Object> result = java.util.Map.of(
            "id", livre.getId(),
            "titre", livre.getTitre(),
            "auteur", livre.getAuteur(),
            "isbn", livre.getIsbn(),
            "categorie", livre.getCategorie(),
            "ageMinimum", livre.getAgeMinimum(),
            "exemplaires", exemplaires
        );
        return ResponseEntity.ok(result);
    }

    @GetMapping("/utilisateur/{id}")
    public ResponseEntity<?> getUserStats(@PathVariable Long id) {
        Adherent adherent = adherentRepository.findById(id).orElse(null);
        if (adherent == null) {
            return ResponseEntity.notFound().build();
        }
        // Prêts actifs
        List<Pret> pretsActifs = pretRepository.findByAdherentIdAndDateRetourEffectifIsNull(id);
        // Réservations actives
        List<Reservation> reservationsActives = reservationRepository.findByAdherentIdAndEstActifTrue(id);
        // Construction de la réponse JSON
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", adherent.getId());
        result.put("nom", adherent.getNom());
        result.put("email", adherent.getEmail());
        result.put("role", adherent.getRole());
        result.put("age", adherent.getAge());
        result.put("categorie", adherent.getCategorie());
        result.put("quotaPret", adherent.getQuotaPret());
        result.put("quotaReservation", adherent.getQuotaReservation());
        result.put("quotaProlongement", adherent.getQuotaProlongement());
        result.put("dateDebutAbonnement", adherent.getDateDebutAbonnement());
        result.put("dateFinAbonnement", adherent.getDateFinAbonnement());
        result.put("penaliteJusquAu", adherent.getPenaliteJusquAu());
        result.put("pretsActifs", pretsActifs);
        result.put("reservationsActives", reservationsActives);
        return ResponseEntity.ok(result);
    }
}