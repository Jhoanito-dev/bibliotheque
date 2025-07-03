package com.example.bibliotheque.controller;

import com.example.bibliotheque.model.Adherent;
import com.example.bibliotheque.model.Livre;
import com.example.bibliotheque.model.Pret;
import com.example.bibliotheque.model.Reservation;
import com.example.bibliotheque.repository.AdherentRepository;
import com.example.bibliotheque.repository.LivreRepository;
import com.example.bibliotheque.repository.PretRepository;
import com.example.bibliotheque.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
public class BibliothecaireController {

    @Autowired
    private LivreRepository livreRepository;

    @Autowired
    private AdherentRepository adherentRepository;

    @Autowired
    private PretRepository pretRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @GetMapping("/bibliothecaire/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("livres", livreRepository.findAll());
        model.addAttribute("adherents", adherentRepository.findAll());
        model.addAttribute("prets", pretRepository.findAll());
        model.addAttribute("reservations", reservationRepository.findAll());
        return "bibliothecaire/dashboard";
    }

    @GetMapping("/bibliothecaire/ajouter-livre")
    public String ajouterLivreForm(Model model) {
        model.addAttribute("livre", new Livre());
        return "bibliothecaire/ajouter-livre";
    }

    @PostMapping("/bibliothecaire/ajouter-livre")
    public String ajouterLivre(@ModelAttribute Livre livre) {
        livre.setDisponible(livre.getNombreExemplaires() > 0); // Initialiser disponible en fonction du nombre d'exemplaires
        livreRepository.save(livre);
        return "redirect:/bibliothecaire/dashboard";
    }

    @GetMapping("/bibliothecaire/ajouter-adherent")
    public String ajouterAdherentForm(Model model) {
        model.addAttribute("adherent", new Adherent());
        return "bibliothecaire/ajouter-adherent";
    }

    @PostMapping("/bibliothecaire/ajouter-adherent")
    public String ajouterAdherent(@ModelAttribute Adherent adherent) {
        adherentRepository.save(adherent);
        return "redirect:/bibliothecaire/dashboard";
    }

    @GetMapping("/bibliothecaire/emprunter-livre")
    public String emprunterLivreForm(Model model) {
        model.addAttribute("pret", new Pret());
        model.addAttribute("adherents", adherentRepository.findAll());
        model.addAttribute("livres", livreRepository.findAll());
        return "bibliothecaire/emprunter-livre";
    }

    @PostMapping("/bibliothecaire/emprunter-livre")
    public String emprunterLivre(@ModelAttribute Pret pret, @RequestParam String typePret) {
        Adherent adherent = adherentRepository.findById(pret.getAdherent().getId()).orElseThrow();
        Livre livre = livreRepository.findById(pret.getLivre().getId()).orElseThrow();
        if (livre.getAgeMinimum() > 0 && (adherent.getAge() == null || adherent.getAge() < livre.getAgeMinimum())) {
            throw new IllegalArgumentException("L'adhérent doit avoir au moins " + livre.getAgeMinimum() + " ans.");
        }
        if (!livre.isDisponible()) {
            throw new IllegalArgumentException("Aucun exemplaire disponible pour ce livre.");
        }
        pret.setDateEmprunt(LocalDate.now());
        pret.setDateRetourPrevus(pret.getDateEmprunt().plusDays(14));
        pret.setPenaliteActive(false);
        pret.setProlonge(false);
        if ("maison".equals(typePret) && livre.getNombreExemplaires() > 0) {
            livre.setNombreExemplaires(livre.getNombreExemplaires() - 1);
            livre.setDisponible(livre.getNombreExemplaires() > 0); // Mettre à jour disponible
        } else if (!"maison".equals(typePret)) {
            pret.setProlonge(true); // Lecture sur place
        }
        pretRepository.save(pret);
        if ("maison".equals(typePret)) {
            livreRepository.save(livre);
        }
        return "redirect:/bibliothecaire/dashboard";
    }

    @GetMapping("/bibliothecaire/retourner-livre/{id}")
    public String retournerLivre(@PathVariable Long id) {
        Pret pret = pretRepository.findById(id).orElseThrow();
        if (pret.getDateRetourEffectif() == null) {
            pret.setDateRetourEffectif(LocalDate.now());
            if (pret.getDateRetourEffectif().isAfter(pret.getDateRetourPrevus())) {
                pret.setPenaliteActive(true);
                pret.getAdherent().setPenaliteJusquAu(pret.getDateRetourEffectif().plusDays(7));
                adherentRepository.save(pret.getAdherent());
            }
            Livre livre = pret.getLivre();
            if (!pret.isProlonge()) { // Seulement pour les prêts à la maison
                livre.setNombreExemplaires(livre.getNombreExemplaires() + 1);
                livre.setDisponible(livre.getNombreExemplaires() > 0); // Mettre à jour disponible
                livreRepository.save(livre);
            }
            pretRepository.save(pret);
        }
        return "redirect:/bibliothecaire/dashboard";
    }

    @GetMapping("/bibliothecaire/reserver-livre")
    public String reserverLivreForm(Model model) {
        model.addAttribute("reservation", new Reservation());
        model.addAttribute("adherents", adherentRepository.findAll());
        model.addAttribute("livres", livreRepository.findAll());
        return "bibliothecaire/reserver-livre";
    }

    @PostMapping("/bibliothecaire/reserver-livre")
    public String reserverLivre(@ModelAttribute Reservation reservation, @RequestParam LocalDate dateRetrait) {
        Adherent adherent = adherentRepository.findById(reservation.getAdherent().getId()).orElseThrow();
        Livre livre = livreRepository.findById(reservation.getLivre().getId()).orElseThrow();
        if (livre.getAgeMinimum() > 0 && (adherent.getAge() == null || adherent.getAge() < livre.getAgeMinimum())) {
            throw new IllegalArgumentException("L'adhérent doit avoir au moins " + livre.getAgeMinimum() + " ans.");
        }
        if (!livre.isDisponible()) {
            throw new IllegalArgumentException("Aucun exemplaire disponible pour ce livre.");
        }
        reservation.setDateReservation(LocalDate.now());
        reservation.setDateLimiteRetrait(dateRetrait);
        reservation.setEstActif(true);
        reservationRepository.save(reservation);
        return "redirect:/bibliothecaire/dashboard";
    }

    @Scheduled(cron = "0 0 0 * * ?") // Exécuté tous les jours à minuit
    public void verifierReservations() {
        List<Reservation> reservations = reservationRepository.findAll();
        for (Reservation reservation : reservations) {
            if (reservation.isEstActif() && LocalDate.now().isEqual(reservation.getDateLimiteRetrait())) {
                Livre livre = reservation.getLivre();
                if (livre.isDisponible() && livre.getNombreExemplaires() > 0) {
                    Pret pret = new Pret();
                    pret.setAdherent(reservation.getAdherent());
                    pret.setLivre(livre);
                    pret.setDateEmprunt(LocalDate.now());
                    pret.setDateRetourPrevus(pret.getDateEmprunt().plusDays(14));
                    pret.setPenaliteActive(false);
                    pret.setProlonge(false);
                    pretRepository.save(pret);
                    livre.setNombreExemplaires(livre.getNombreExemplaires() - 1);
                    livre.setDisponible(livre.getNombreExemplaires() > 0);
                    livreRepository.save(livre);
                    reservation.setEstActif(false);
                    reservationRepository.save(reservation);
                }
            }
        }
    }
}