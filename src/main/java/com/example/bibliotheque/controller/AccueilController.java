package com.example.bibliotheque.controller;

import com.example.bibliotheque.model.Adherent;
import com.example.bibliotheque.model.Pret;
import com.example.bibliotheque.repository.AdherentRepository;
import com.example.bibliotheque.repository.PretRepository;
import com.example.bibliotheque.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collections;
import java.util.List;

@Controller
public class AccueilController {

    @Autowired
    private AdherentRepository adherentRepository;

    @Autowired
    private PretRepository pretRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }

    @GetMapping("/accueil")
    public String accueil(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            String email = auth.getName();
            Adherent adherent = adherentRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé : " + email));

            List<Pret> prets = pretRepository.findByAdherentIdAndDateRetourEffectifIsNull(adherent.getId());
            if (prets == null) {
                prets = Collections.emptyList();
            }

            // Fetch active reservations for this adherent
            List<com.example.bibliotheque.model.Reservation> reservations = reservationRepository.findByAdherentIdAndEstActifTrue(adherent.getId());
            if (reservations == null) {
                reservations = Collections.emptyList();
            }

            model.addAttribute("adherent", adherent);
            model.addAttribute("prets", prets);
            model.addAttribute("reservations", reservations);

            // Debug: afficher les autorités
            System.out.println("=== REDIRECTION APRÈS CONNEXION ===");
            System.out.println("Utilisateur : " + email);
            System.out.println("Autorités : " + auth.getAuthorities());
            System.out.println("Rôle de l'adhérent : " + adherent.getRole());
            
            // Vérifier le rôle de l'adhérent plutôt que les autorités
            if ("ROLE_ADMIN".equals(adherent.getRole())) {
                System.out.println("Redirection vers dashboard bibliothécaire");
                return "redirect:/bibliothecaire/dashboard";
            } else {
                System.out.println("Redirection vers dashboard adhérent");
                return "adherent/dashboard";
            }
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}