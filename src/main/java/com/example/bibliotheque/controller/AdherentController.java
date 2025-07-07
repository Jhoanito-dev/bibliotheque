package com.example.bibliotheque.controller;

import com.example.bibliotheque.model.Adherent;
import com.example.bibliotheque.model.Demande;
import com.example.bibliotheque.model.Livre;
import com.example.bibliotheque.repository.AdherentRepository;
import com.example.bibliotheque.repository.DemandeRepository;
import com.example.bibliotheque.repository.LivreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
public class AdherentController {

    @Autowired
    private AdherentRepository adherentRepository;

    @Autowired
    private LivreRepository livreRepository;

    @Autowired
    private DemandeRepository demandeRepository;

    @GetMapping("/adherent/emprunt")
    public String showEmpruntForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            String email = auth.getName();
            Adherent adherent = adherentRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé : " + email));
            
            // Vérifier que c'est bien un adhérent
            if (!"ROLE_USER".equals(adherent.getRole())) {
                return "redirect:/accueil?error=Seuls les adhérents peuvent emprunter des livres";
            }

            model.addAttribute("adherent", adherent);
            model.addAttribute("livres", livreRepository.findAll());
            model.addAttribute("demande", new Demande());
            return "adherent/emprunter-livre";
        }
        return "redirect:/login";
    }

    @PostMapping("/adherent/emprunt/submit")
    public String submitEmprunt(@ModelAttribute Demande demande, @RequestParam Long livreId, 
                              @RequestParam String typePret, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            String email = auth.getName();
            Adherent adherent = adherentRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé : " + email));
            
            // Vérifier que c'est bien un adhérent
            if (!"ROLE_USER".equals(adherent.getRole())) {
                return "redirect:/accueil?error=Seuls les adhérents peuvent emprunter des livres";
            }

            Livre livre = livreRepository.findById(livreId)
                    .orElseThrow(() -> new RuntimeException("Livre non trouvé : " + livreId));

            // Vérifications supplémentaires
            if (adherent.getPenaliteJusquAu() != null && adherent.getPenaliteJusquAu().isAfter(LocalDate.now())) {
                return "redirect:/adherent/emprunt?error=Vous êtes sous pénalité jusqu'au " + adherent.getPenaliteJusquAu();
            }

            if (livre.getAgeMinimum() > 0 && (adherent.getAge() == null || adherent.getAge() < livre.getAgeMinimum())) {
                return "redirect:/adherent/emprunt?error=Vous devez avoir au moins " + livre.getAgeMinimum() + " ans pour ce livre";
            }

            if (!"surplace".equals(typePret)) {
                long pretsActifs = adherent.getPrets().stream()
                        .filter(p -> p.getDateRetourEffectif() == null)
                        .count();
                if (pretsActifs >= adherent.getQuotaPret()) {
                    return "redirect:/adherent/emprunt?error=Vous avez atteint votre quota de prêts";
                }
            }

            demande.setAdherent(adherent);
            demande.setLivre(livre);
            demande.setDateSoumission(LocalDate.now());
            demande.setTypeDemande("pret");
            demande.setTypePret(typePret);
            demande.setStatut("en attente");
            demandeRepository.save(demande);
            return "redirect:/accueil?success=Demande d'emprunt envoyée";
        }
        return "redirect:/login";
    }

    @GetMapping("/adherent/reservation")
    public String showReservationForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            String email = auth.getName();
            Adherent adherent = adherentRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé : " + email));
            
            // Vérifier que c'est bien un adhérent
            if (!"ROLE_USER".equals(adherent.getRole())) {
                return "redirect:/accueil?error=Seuls les adhérents peuvent réserver des livres";
            }

            model.addAttribute("adherent", adherent);
            model.addAttribute("livres", livreRepository.findAll());
            model.addAttribute("demande", new Demande());
            return "adherent/reserver-livre";
        }
        return "redirect:/login";
    }

    @PostMapping("/adherent/reservation/submit")
    public String submitReservation(@ModelAttribute Demande demande, @RequestParam Long livreId, 
                                  @RequestParam LocalDate dateRetraitSouhaitee, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            String email = auth.getName();
            Adherent adherent = adherentRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé : " + email));
            
            // Vérifier que c'est bien un adhérent
            if (!"ROLE_USER".equals(adherent.getRole())) {
                return "redirect:/accueil?error=Seuls les adhérents peuvent réserver des livres";
            }

            Livre livre = livreRepository.findById(livreId)
                    .orElseThrow(() -> new RuntimeException("Livre non trouvé : " + livreId));

            // Vérifications supplémentaires
            if (adherent.getPenaliteJusquAu() != null && adherent.getPenaliteJusquAu().isAfter(LocalDate.now())) {
                return "redirect:/adherent/reservation?error=Vous êtes sous pénalité jusqu'au " + adherent.getPenaliteJusquAu();
            }

            if (livre.getAgeMinimum() > 0 && (adherent.getAge() == null || adherent.getAge() < livre.getAgeMinimum())) {
                return "redirect:/adherent/reservation?error=Vous devez avoir au moins " + livre.getAgeMinimum() + " ans pour ce livre";
            }

            long reservationsActives = adherent.getReservations().stream()
                    .filter(r -> r.isEstActif())
                    .count();
            if (reservationsActives >= adherent.getQuotaReservation()) {
                return "redirect:/adherent/reservation?error=Vous avez atteint votre quota de réservations";
            }

            if (dateRetraitSouhaitee.isBefore(LocalDate.now().plusDays(1))) {
                return "redirect:/adherent/reservation?error=La date de retrait doit être au moins demain";
            }

            demande.setAdherent(adherent);
            demande.setLivre(livre);
            demande.setDateSoumission(LocalDate.now());
            demande.setTypeDemande("reservation");
            demande.setDateRetraitSouhaitee(dateRetraitSouhaitee);
            demande.setStatut("en attente");
            demandeRepository.save(demande);
            return "redirect:/accueil?success=Demande de réservation envoyée";
        }
        return "redirect:/login";
    }
}