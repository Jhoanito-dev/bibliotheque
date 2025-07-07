package com.example.bibliotheque.controller;

import com.example.bibliotheque.model.*;
import com.example.bibliotheque.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
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

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DemandeRepository demandeRepository;

    @GetMapping("/bibliothecaire/dashboard")
    @Transactional(readOnly = true)
    public String dashboard(Model model) {
        try {
            List<Livre> livres = livreRepository.findAll();
            // Filtrer pour n'avoir que les adhérents (ROLE_USER)
            List<Adherent> adherents = adherentRepository.findAll().stream()
                .filter(a -> "ROLE_USER".equals(a.getRole()))
                .collect(Collectors.toList());
            List<Pret> prets = pretRepository.findByDateRetourEffectifIsNull();
            List<Reservation> reservations = reservationRepository.findAll();
            List<Demande> demandes = demandeRepository.findByStatut("en attente");

            model.addAttribute("livres", livres != null ? livres : Collections.emptyList());
            model.addAttribute("adherents", adherents != null ? adherents : Collections.emptyList());
            model.addAttribute("prets", prets != null ? prets : Collections.emptyList());
            model.addAttribute("reservations", reservations != null ? reservations : Collections.emptyList());
            model.addAttribute("demandes", demandes != null ? demandes : Collections.emptyList());
        } catch (Exception e) {
            model.addAttribute("livres", Collections.emptyList());
            model.addAttribute("adherents", Collections.emptyList());
            model.addAttribute("prets", Collections.emptyList());
            model.addAttribute("reservations", Collections.emptyList());
            model.addAttribute("demandes", Collections.emptyList());
            model.addAttribute("error", "Erreur lors du chargement des données : " + e.getMessage());
        }
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
        adherent.setMotDePasse(passwordEncoder.encode(adherent.getMotDePasse()));
        adherent.setRole("ROLE_USER"); // Toujours ROLE_USER pour les nouveaux adhérents
        
        if ("Etudiant".equals(adherent.getCategorie())) {
            adherent.setQuotaPret(2);
            adherent.setQuotaReservation(1);
            adherent.setQuotaProlongement(1);
        } else if ("Professeur".equals(adherent.getCategorie())) {
            adherent.setQuotaPret(5);
            adherent.setQuotaReservation(3);
            adherent.setQuotaProlongement(2);
        } else {
            adherent.setQuotaPret(3);
            adherent.setQuotaReservation(2);
            adherent.setQuotaProlongement(1);
        }
        adherentRepository.save(adherent);
        return "redirect:/bibliothecaire/dashboard";
    }

    @GetMapping("/bibliothecaire/emprunter-livre")
    public String emprunterLivreForm(Model model) {
        // Ne montrer que les adhérents (ROLE_USER)
        List<Adherent> adherents = adherentRepository.findAll().stream()
            .filter(a -> "ROLE_USER".equals(a.getRole()))
            .collect(Collectors.toList());
        
        model.addAttribute("pret", new Pret());
        model.addAttribute("adherents", adherents);
        model.addAttribute("livres", livreRepository.findAll());
        return "bibliothecaire/emprunter-livre";
    }

    @PostMapping("/bibliothecaire/emprunter-livre")
    public String emprunterLivre(@ModelAttribute Pret pret, @RequestParam String typePret, Model model) {
        try {
            Adherent adherent = adherentRepository.findById(pret.getAdherent().getId()).orElseThrow();
            
            // Vérifier que c'est bien un adhérent
            if (!"ROLE_USER".equals(adherent.getRole())) {
                throw new IllegalArgumentException("Seuls les adhérents peuvent emprunter des livres");
            }

            Livre livre = livreRepository.findById(pret.getLivre().getId()).orElseThrow();

            if (adherent.getPenaliteJusquAu() != null && adherent.getPenaliteJusquAu().isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("L'adhérent est sous pénalité jusqu'au " + adherent.getPenaliteJusquAu());
            }

            if (livre.getAgeMinimum() > 0 && (adherent.getAge() == null || adherent.getAge() < livre.getAgeMinimum())) {
                throw new IllegalArgumentException("L'adhérent doit avoir au moins " + livre.getAgeMinimum() + " ans pour ce livre.");
            }

            if (livre.getNombreExemplaires() <= 0) {
                throw new IllegalArgumentException("Aucun exemplaire disponible pour ce livre.");
            }

            pret.setDateEmprunt(LocalDate.now());
            pret.setTypePret(typePret);
            
            int dureeMax;
            if ("surplace".equals(typePret)) {
                dureeMax = 1;
            } else {
                dureeMax = "Etudiant".equals(adherent.getCategorie()) ? 14 : 
                          "Professeur".equals(adherent.getCategorie()) ? 30 : 21;
                
                long pretsActifs = pretRepository.findByAdherentIdAndDateRetourEffectifIsNull(adherent.getId()).size();
                if (pretsActifs >= adherent.getQuotaPret()) {
                    throw new IllegalArgumentException("Quota de prêts à domicile atteint pour ce profil.");
                }
            }

            pret.setDateRetourPrevus(calculerDateRetour(pret.getDateEmprunt(), dureeMax));
            pret.setPenaliteActive(false);
            pret.setProlonge(false);
            pret.setNombreProlongements(0);

            livre.setNombreExemplaires(livre.getNombreExemplaires() - 1);
            livre.setDisponible(livre.getNombreExemplaires() > 0);
            livreRepository.save(livre);

            pretRepository.save(pret);
            return "redirect:/bibliothecaire/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors de l'emprunt : " + e.getMessage());
            model.addAttribute("pret", pret);
            // Ne montrer que les adhérents (ROLE_USER)
            List<Adherent> adherents = adherentRepository.findAll().stream()
                .filter(a -> "ROLE_USER".equals(a.getRole()))
                .collect(Collectors.toList());
            model.addAttribute("adherents", adherents);
            model.addAttribute("livres", livreRepository.findAll());
            return "bibliothecaire/emprunter-livre";
        }
    }

    @GetMapping("/bibliothecaire/retourner-livre/{id}")
    public String retournerLivre(@PathVariable Long id) {
        Pret pret = pretRepository.findById(id).orElseThrow();
        if (pret.getDateRetourEffectif() == null) {
            pret.setDateRetourEffectif(LocalDate.now());
            
            if (!"surplace".equals(pret.getTypePret()) && pret.getDateRetourEffectif().isAfter(pret.getDateRetourPrevus())) {
                pret.setPenaliteActive(true);
                Adherent adherent = pret.getAdherent();
                // Vérifier que c'est bien un adhérent
                if ("ROLE_USER".equals(adherent.getRole())) {
                    adherent.setPenaliteJusquAu(pret.getDateRetourEffectif().plusDays(10));
                    adherentRepository.save(adherent);
                }
            }
            
            Livre livre = pret.getLivre();
            livre.setNombreExemplaires(livre.getNombreExemplaires() + 1);
            livre.setDisponible(livre.getNombreExemplaires() > 0);
            livreRepository.save(livre);
            
            pretRepository.save(pret);
        }
        return "redirect:/bibliothecaire/dashboard";
    }

    @GetMapping("/bibliothecaire/reserver-livre")
    public String reserverLivreForm(Model model) {
        // Ne montrer que les adhérents (ROLE_USER)
        List<Adherent> adherents = adherentRepository.findAll().stream()
            .filter(a -> "ROLE_USER".equals(a.getRole()))
            .collect(Collectors.toList());
        
        model.addAttribute("reservation", new Reservation());
        model.addAttribute("adherents", adherents);
        model.addAttribute("livres", livreRepository.findAll());
        return "bibliothecaire/reserver-livre";
    }

    @PostMapping("/bibliothecaire/reserver-livre")
    public String reserverLivre(@ModelAttribute Reservation reservation, @RequestParam LocalDate dateRetrait, Model model) {
        try {
            Adherent adherent = adherentRepository.findById(reservation.getAdherent().getId()).orElseThrow();
            
            // Vérifier que c'est bien un adhérent
            if (!"ROLE_USER".equals(adherent.getRole())) {
                throw new IllegalArgumentException("Seuls les adhérents peuvent réserver des livres");
            }

            Livre livre = livreRepository.findById(reservation.getLivre().getId()).orElseThrow();

            if (dateRetrait.isBefore(LocalDate.now()) || dateRetrait.isEqual(LocalDate.now())) {
                throw new IllegalArgumentException("La date de retrait doit être dans le futur.");
            }

            if (adherent.getPenaliteJusquAu() != null && adherent.getPenaliteJusquAu().isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("L'adhérent est sous pénalité jusqu'au " + adherent.getPenaliteJusquAu());
            }

            if (livre.getAgeMinimum() > 0 && (adherent.getAge() == null || adherent.getAge() < livre.getAgeMinimum())) {
                throw new IllegalArgumentException("L'adhérent doit avoir au moins " + livre.getAgeMinimum() + " ans pour ce livre.");
            }

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
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors de la réservation : " + e.getMessage());
            model.addAttribute("reservation", reservation);
            // Ne montrer que les adhérents (ROLE_USER)
            List<Adherent> adherents = adherentRepository.findAll().stream()
                .filter(a -> "ROLE_USER".equals(a.getRole()))
                .collect(Collectors.toList());
            model.addAttribute("adherents", adherents);
            model.addAttribute("livres", livreRepository.findAll());
            return "bibliothecaire/reserver-livre";
        }
    }

    @GetMapping("/bibliothecaire/valider-demande/{id}")
    public String validerDemande(@PathVariable Long id, Model model) {
        Demande demande = demandeRepository.findById(id).orElseThrow();
        if ("en attente".equals(demande.getStatut())) {
            // Vérifier que le demandeur est un adhérent
            if (!"ROLE_USER".equals(demande.getAdherent().getRole())) {
                throw new IllegalArgumentException("Seuls les adhérents peuvent faire des demandes");
            }

            if ("pret".equals(demande.getTypeDemande())) {
                Pret pret = new Pret();
                pret.setAdherent(demande.getAdherent());
                pret.setLivre(demande.getLivre());
                pret.setDateEmprunt(LocalDate.now());
                pret.setTypePret(demande.getTypePret());
                int dureeMax = "Etudiant".equals(demande.getAdherent().getCategorie()) ? 14 : "Professeur".equals(demande.getAdherent().getCategorie()) ? 30 : 21;
                pret.setDateRetourPrevus(calculerDateRetour(pret.getDateEmprunt(), dureeMax));
                pret.setPenaliteActive(false);
                pret.setProlonge(false);
                pret.setNombreProlongements(0);
                if ("maison".equals(demande.getTypePret()) && demande.getLivre().getNombreExemplaires() > 0) {
                    demande.getLivre().setNombreExemplaires(demande.getLivre().getNombreExemplaires() - 1);
                    demande.getLivre().setDisponible(demande.getLivre().getNombreExemplaires() > 0);
                    livreRepository.save(demande.getLivre());
                }
                pretRepository.save(pret);
            } else if ("reservation".equals(demande.getTypeDemande())) {
                Reservation reservation = new Reservation();
                reservation.setAdherent(demande.getAdherent());
                reservation.setLivre(demande.getLivre());
                reservation.setDateReservation(LocalDate.now());
                reservation.setDateLimiteRetrait(demande.getDateRetraitSouhaitee());
                reservation.setEstActif(true);
                reservationRepository.save(reservation);
            }
            demande.setStatut("valide");
            demandeRepository.save(demande);
        }
        return "redirect:/bibliothecaire/dashboard";
    }

    @GetMapping("/bibliothecaire/rejeter-demande/{id}")
    public String rejeterDemande(@PathVariable Long id) {
        Demande demande = demandeRepository.findById(id).orElseThrow();
        if ("en attente".equals(demande.getStatut())) {
            demande.setStatut("rejete");
            demandeRepository.save(demande);
        }
        return "redirect:/bibliothecaire/dashboard";
    }

    private LocalDate calculerDateRetour(LocalDate dateEmprunt, int dureeMax) {
        LocalDate dateRetour = dateEmprunt.plusDays(dureeMax);
        List<LocalDate> joursFeries = jourFerieRepository.findByDateBetween(
            dateEmprunt, dateRetour.plusDays(30)
        ).stream()
         .map(JourFerie::getDate)
         .collect(Collectors.toList());
        
        while (joursFeries.contains(dateRetour)) {
            dateRetour = dateRetour.plusDays(1);
        }
        return dateRetour;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void verifierReservations() {
        List<Reservation> reservations = reservationRepository.findByEstActifTrueAndDateLimiteRetrait(LocalDate.now());
        for (Reservation reservation : reservations) {
            // Vérifier que le réservataire est un adhérent
            if (!"ROLE_USER".equals(reservation.getAdherent().getRole())) {
                continue;
            }

            Livre livre = reservation.getLivre();
            if (livre.isDisponible() && livre.getNombreExemplaires() > 0) {
                Pret pret = new Pret();
                pret.setAdherent(reservation.getAdherent());
                pret.setLivre(livre);
                pret.setDateEmprunt(LocalDate.now());
                Adherent adherent = reservation.getAdherent();
                int dureeMax = "Etudiant".equals(adherent.getCategorie()) ? 14 : "Professeur".equals(adherent.getCategorie()) ? 30 : 21;
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
            
            // Vérifier que c'est bien un adhérent
            if (!"ROLE_USER".equals(adherent.getRole())) {
                throw new IllegalArgumentException("Seuls les adhérents peuvent prolonger des prêts");
            }

            if (pret.getNombreProlongements() < adherent.getQuotaProlongement() && !pret.isProlonge()) {
                int dureeAjout = "Etudiant".equals(adherent.getCategorie()) ? 7 : "Professeur".equals(adherent.getCategorie()) ? 14 : 10;
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