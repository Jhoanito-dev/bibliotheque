package com.example.bibliotheque.controller;

import com.example.bibliotheque.model.Adherent;
import com.example.bibliotheque.model.Demande;
import com.example.bibliotheque.model.Livre;
import com.example.bibliotheque.model.Pret;
import com.example.bibliotheque.repository.AdherentRepository;
import com.example.bibliotheque.repository.DemandeRepository;
import com.example.bibliotheque.repository.LivreRepository;
import com.example.bibliotheque.repository.PretRepository;
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

    @Autowired
    private PretRepository pretRepository;

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
                              @RequestParam String typePret, @RequestParam(name = "dateEmprunt") String dateEmpruntStr, Model model) {
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

            // Prise en compte de la date de prêt saisie
            LocalDate dateEmprunt;
            try {
                dateEmprunt = LocalDate.parse(dateEmpruntStr);
            } catch (Exception e) {
                return "redirect:/adherent/emprunt?error=Date de prêt invalide";
            }
            if (dateEmprunt.isAfter(LocalDate.now().plusDays(30))) {
                return "redirect:/adherent/emprunt?error=La date de prêt ne peut pas être plus de 30 jours dans le futur";
            }
            if (dateEmprunt.isBefore(LocalDate.now().minusDays(30))) {
                return "redirect:/adherent/emprunt?error=La date de prêt ne peut pas être plus de 30 jours dans le passé";
            }

            demande.setAdherent(adherent);
            demande.setLivre(livre);
            demande.setDateSoumission(LocalDate.now());
            demande.setTypeDemande("pret");
            demande.setTypePret(typePret);
            demande.setStatut("en attente");
            demande.setDateRetraitSouhaitee(null);
            demande.setDateSoumission(dateEmprunt); // Utilisation de la date de prêt comme date de soumission
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

    @PostMapping("/adherent/retourner-livre")
    public String rendreLivre(@RequestParam Long pretId, @RequestParam String dateRetour) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            Pret pret = pretRepository.findById(pretId).orElse(null);
            if (pret == null || pret.getDateRetourEffectif() != null) {
                return "redirect:/accueil?error=Prêt introuvable ou déjà retourné";
            }
            LocalDate dateRetourEff;
            try {
                dateRetourEff = LocalDate.parse(dateRetour);
            } catch (Exception e) {
                return "redirect:/accueil?error=Date de retour invalide";
            }
            pret.setDateRetourEffectif(dateRetourEff);
            // Gérer la pénalité si le retour est en retard (hors prêt sur place)
            if (!"surplace".equals(pret.getTypePret()) && dateRetourEff.isAfter(pret.getDateRetourPrevus())) {
                pret.setPenaliteActive(true);
                Adherent adherent = pret.getAdherent();
                if ("ROLE_USER".equals(adherent.getRole())) {
                    adherent.setPenaliteJusquAu(dateRetourEff.plusDays(10));
                    adherentRepository.save(adherent);
                }
            }
            // Remettre l'exemplaire au stock
            Livre livre = pret.getLivre();
            livre.setNombreExemplaires(livre.getNombreExemplaires() + 1);
            livre.setDisponible(livre.getNombreExemplaires() > 0);
            livreRepository.save(livre);
            pretRepository.save(pret);
            return "redirect:/accueil?success=Livre rendu avec succès";
        }
        return "redirect:/login";
    }
}