package com.example.bibliotheque.controller;

import com.example.bibliotheque.model.Adherent;
import com.example.bibliotheque.model.JourFerie;
import com.example.bibliotheque.model.Livre;
import com.example.bibliotheque.model.Pret;
import com.example.bibliotheque.model.Reservation;
import com.example.bibliotheque.repository.AdherentRepository;
import com.example.bibliotheque.repository.JourFerieRepository;
import com.example.bibliotheque.repository.LivreRepository;
import com.example.bibliotheque.repository.PretRepository;
import com.example.bibliotheque.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@EnableScheduling
public class BibliothecaireController {

    @Autowired
    private LivreRepository livreRepository;

    @Autowired
    private AdherentRepository adherentRepository;

    @Autowired
    private PretRepository pretRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private JourFerieRepository jourFerieRepository;

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
        livre.setDisponible(livre.getNombreExemplaires() > 0);
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
        // Initialisation des quotas selon la catégorie
        if ("Etudiant".equals(adherent.getCategorie())) {
            adherent.setQuotaPret(2);
            adherent.setQuotaReservation(1);
            adherent.setQuotaProlongement(1);
        } else if ("Professeur".equals(adherent.getCategorie())) {
            adherent.setQuotaPret(5);
            adherent.setQuotaReservation(3);
            adherent.setQuotaProlongement(2);
        } else { // Autre
            adherent.setQuotaPret(3);
            adherent.setQuotaReservation(2);
            adherent.setQuotaProlongement(1);
        }
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
    public String emprunterLivre(@ModelAttribute Pret pret, @RequestParam String typePret, Model model) {
        try {
                
            Adherent adherent = adherentRepository.findById(pret.getAdherent().getId()).orElseThrow();
            Livre livre = livreRepository.findById(pret.getLivre().getId()).orElseThrow();

            if (adherent.getPenaliteJusquAu() != null && adherent.getPenaliteJusquAu().isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("L'adhérent est sous pénalité jusqu'au " + adherent.getPenaliteJusquAu());
            }

            // Vérification de l'âge pour les livres 18+
            if (livre.getAgeMinimum() > 0 && (adherent.getAge() == null || adherent.getAge() < livre.getAgeMinimum())) {
                throw new IllegalArgumentException("L'adhérent doit avoir au moins " + livre.getAgeMinimum() + " ans pour ce livre.");
            }

            // Vérification du quota de prêt
            long pretsActifs = pretRepository.findByAdherentIdAndDateRetourEffectifIsNull(adherent.getId()).size();
            if (pretsActifs >= adherent.getQuotaPret() && !"surplace".equals(typePret)) {
                throw new IllegalArgumentException("Quota de prêts atteint pour ce profil.");
            }

            if (!livre.isDisponible()) {
                throw new IllegalArgumentException("Aucun exemplaire disponible pour ce livre.");
            }

            pret.setDateEmprunt(LocalDate.now());
            pret.setTypePret(typePret);
            // Calculer la durée maximale selon la catégorie
            int dureeMax = "Etudiant".equals(adherent.getCategorie()) ? 14 : "Professeur".equals(adherent.getCategorie()) ? 30 : 21;
            // Utiliser la méthode calculerDateRetour pour exclure les jours fériés
            pret.setDateRetourPrevus(calculerDateRetour(pret.getDateEmprunt(), dureeMax));
            pret.setPenaliteActive(false);
            pret.setProlonge(false);
            pret.setNombreProlongements(0);

            if ("maison".equals(typePret) && livre.getNombreExemplaires() > 0) {
                livre.setNombreExemplaires(livre.getNombreExemplaires() - 1);
                livre.setDisponible(livre.getNombreExemplaires() > 0);
            }
            pretRepository.save(pret);
            if ("maison".equals(typePret)) {
                livreRepository.save(livre);
            }
            return "redirect:/bibliothecaire/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors de l'emprunt : " + e.getMessage());
            model.addAttribute("pret", pret);
            model.addAttribute("adherents", adherentRepository.findAll());
            model.addAttribute("livres", livreRepository.findAll());
            return "bibliothecaire/emprunter-livre";
        }
    }

    @GetMapping("/bibliothecaire/retourner-livre/{id}")
    public String retournerLivre(@PathVariable Long id) {
        Pret pret = pretRepository.findById(id).orElseThrow();
        if (pret.getDateRetourEffectif() == null) {
            pret.setDateRetourEffectif(LocalDate.now());
            if (pret.getDateRetourEffectif().isAfter(pret.getDateRetourPrevus())) {
                pret.setPenaliteActive(true);
                pret.getAdherent().setPenaliteJusquAu(pret.getDateRetourEffectif().plusDays(10));
                adherentRepository.save(pret.getAdherent());
            }
            Livre livre = pret.getLivre();
            if ("maison".equals(pret.getTypePret())) {
                livre.setNombreExemplaires(livre.getNombreExemplaires() + 1);
                livre.setDisponible(livre.getNombreExemplaires() > 0);
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

        if (dateRetrait.isBefore(LocalDate.now()) || dateRetrait.isEqual(LocalDate.now())) {
            throw new IllegalArgumentException("La date de retrait doit être dans le futur.");
        }

        if (adherent.getPenaliteJusquAu() != null && adherent.getPenaliteJusquAu().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("L'adhérent est sous pénalité jusqu'au " + adherent.getPenaliteJusquAu());
        }

        // Vérification de l'âge pour les livres 18+
        if (livre.getAgeMinimum() > 0 && (adherent.getAge() == null || adherent.getAge() < livre.getAgeMinimum())) {
            throw new IllegalArgumentException("L'adhérent doit avoir au moins " + livre.getAgeMinimum() + " ans pour ce livre.");
        }

        // Vérification du quota de réservation
        long reservationsActives = reservationRepository.findByAdherentIdAndEstActifTrue(adherent.getId()).size();
        if (reservationsActives >= adherent.getQuotaReservation()) {
            throw new IllegalArgumentException("Quota de réservations atteint pour ce profil.");
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

    private LocalDate calculerDateRetour(LocalDate dateEmprunt, int dureeMax) {
        // Calculer la date de retour initiale
        LocalDate dateRetour = dateEmprunt.plusDays(dureeMax);
        
        // Récupérer les jours fériés dans une plage raisonnable (par exemple, duréeMax + 30 jours pour couvrir les ajustements)
        List<LocalDate> joursFeries = jourFerieRepository.findByDateBetween(
            dateEmprunt, 
            dateRetour.plusDays(30)
        ).stream()
         .map(JourFerie::getDate)
         .collect(Collectors.toList());
        
        // Tant que la date de retour tombe sur un jour férié, ajouter un jour
        while (joursFeries.contains(dateRetour)) {
            dateRetour = dateRetour.plusDays(1);
        }

        return dateRetour;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void verifierReservations() {
        List<Reservation> reservations = reservationRepository.findByEstActifTrueAndDateLimiteRetrait(LocalDate.now());
        for (Reservation reservation : reservations) {
            Livre livre = reservation.getLivre();
            if (livre.isDisponible() && livre.getNombreExemplaires() > 0) {
                Pret pret = new Pret();
                pret.setAdherent(reservation.getAdherent());
                pret.setLivre(livre);
                pret.setDateEmprunt(LocalDate.now());
                Adherent adherent = reservation.getAdherent();
                int dureeMax = "Etudiant".equals(adherent.getCategorie()) ? 14 : "Professeur".equals(adherent.getCategorie()) ? 30 : 21;
                // Utiliser calculerDateRetour pour exclure les jours fériés
                pret.setDateRetourPrevus(calculerDateRetour(pret.getDateEmprunt(), dureeMax));
                pret.setPenaliteActive(false);
                pret.setProlonge(false);
                pret.setNombreProlongements(0);
                pret.setTypePret("maison");
                pretRepository.save(pret);
                livre.setNombreExemplaires(livre.getNombreExemplaires() - 1);
                livre.setDisponible(livre.getNombreExemplaires() > 0);
                livreRepository.save(livre);
                reservation.setEstActif(false);
                reservationRepository.save(reservation);
            }
        }
    }

    @GetMapping("/bibliothecaire/prolonger-pret/{id}")
    public String prolongerPret(@PathVariable Long id, Model model) {
        try {
            Pret pret = pretRepository.findById(id).orElseThrow();
            Adherent adherent = pret.getAdherent();
            if (pret.getNombreProlongements() < adherent.getQuotaProlongement() && !pret.isProlonge()) {
                int dureeAjout = "Etudiant".equals(adherent.getCategorie()) ? 7 : "Professeur".equals(adherent.getCategorie()) ? 14 : 10;
                // Utiliser calculerDateRetour pour la prolongation
                pret.setDateRetourPrevus(calculerDateRetour(pret.getDateRetourPrevus(), dureeAjout));
                pret.setProlonge(true);
                pret.setNombreProlongements(pret.getNombreProlongements() + 1);
                pretRepository.save(pret);
                return "redirect:/bibliothecaire/dashboard";
            } else {
                throw new IllegalArgumentException("Quota de prolongement atteint ou prêt déjà prolongé.");
            }
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors de la prolongation : " + e.getMessage());
            return "redirect:/bibliothecaire/dashboard";
        }
    }
}