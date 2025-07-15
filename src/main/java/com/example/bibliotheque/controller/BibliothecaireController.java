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
            System.out.println("=== CHARGEMENT DU DASHBOARD BIBLIOTHÉCAIRE ===");
            
            // Test direct de la base de données
            long totalLivres = livreRepository.count();
            long totalAdherents = adherentRepository.count();
            long totalPrets = pretRepository.count();
            long totalReservations = reservationRepository.count();
            long totalDemandes = demandeRepository.count();
            
            System.out.println("Compteurs de base :");
            System.out.println("- Total livres : " + totalLivres);
            System.out.println("- Total adhérents : " + totalAdherents);
            System.out.println("- Total prêts : " + totalPrets);
            System.out.println("- Total réservations : " + totalReservations);
            System.out.println("- Total demandes : " + totalDemandes);
            
            List<Livre> livres = livreRepository.findAll();
            System.out.println("Livres trouvés : " + (livres != null ? livres.size() : 0));
            if (livres != null && !livres.isEmpty()) {
                System.out.println("Premier livre : " + livres.get(0).getTitre());
            }
            
            // Filtrer pour n'avoir que les adhérents (ROLE_USER)
            List<Adherent> adherents = adherentRepository.findAll().stream()
                .filter(a -> "ROLE_USER".equals(a.getRole()))
                .collect(Collectors.toList());
            System.out.println("Adhérents trouvés : " + (adherents != null ? adherents.size() : 0));
            if (adherents != null && !adherents.isEmpty()) {
                System.out.println("Premier adhérent : " + adherents.get(0).getNom());
            }
            
            List<Pret> prets = pretRepository.findByDateRetourEffectifIsNull();
            System.out.println("Prêts actifs trouvés : " + (prets != null ? prets.size() : 0));
            
            List<Reservation> reservations = reservationRepository.findAll();
            System.out.println("Réservations trouvées : " + (reservations != null ? reservations.size() : 0));
            
            List<Demande> demandes = demandeRepository.findByStatut("en attente");
            System.out.println("Demandes en attente trouvées : " + (demandes != null ? demandes.size() : 0));

            model.addAttribute("livres", livres != null ? livres : Collections.emptyList());
            model.addAttribute("adherents", adherents != null ? adherents : Collections.emptyList());
            model.addAttribute("prets", prets != null ? prets : Collections.emptyList());
            model.addAttribute("reservations", reservations != null ? reservations : Collections.emptyList());
            model.addAttribute("demandes", demandes != null ? demandes : Collections.emptyList());
            
            System.out.println("=== DASHBOARD CHARGÉ AVEC SUCCÈS ===");
        } catch (Exception e) {
            System.err.println("=== ERREUR LORS DU CHARGEMENT DU DASHBOARD ===");
            System.err.println("Erreur : " + e.getMessage());
            e.printStackTrace();
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
                throw new IllegalArgumentException("❌ L'adhérent " + adherent.getNom() + " est sous pénalité jusqu'au " + 
                    adherent.getPenaliteJusquAu().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
                    ". Impossible d'emprunter pendant cette période.");
            }

            if (livre.getAgeMinimum() > 0 && (adherent.getAge() == null || adherent.getAge() < livre.getAgeMinimum())) {
                throw new IllegalArgumentException("❌ Âge insuffisant : L'adhérent " + adherent.getNom() + " a " + 
                    (adherent.getAge() != null ? adherent.getAge() : "âge non renseigné") + " ans, mais ce livre nécessite au moins " + 
                    livre.getAgeMinimum() + " ans. Titre du livre : " + livre.getTitre());
            }

            if (livre.getNombreExemplaires() <= 0) {
                throw new IllegalArgumentException("❌ Aucun exemplaire disponible : Le livre '" + livre.getTitre() + 
                    "' n'a plus d'exemplaires disponibles. Veuillez attendre qu'un exemplaire soit retourné.");
            }

            // Validation de la date de prêt
            LocalDate dateEmprunt = pret.getDateEmprunt();
            if (dateEmprunt == null) {
                dateEmprunt = LocalDate.now();
                pret.setDateEmprunt(dateEmprunt);
            }
            if (dateEmprunt.isAfter(LocalDate.now().plusDays(30))) {
                throw new IllegalArgumentException("❌ La date de prêt ne peut pas être plus de 30 jours dans le futur.");
            }
            if (dateEmprunt.isBefore(LocalDate.now().minusDays(30))) {
                throw new IllegalArgumentException("❌ La date de prêt ne peut pas être plus de 30 jours dans le passé.");
            }

            pret.setTypePret(typePret);
            int dureeMax;
            if ("surplace".equals(typePret)) {
                dureeMax = 1;
            } else {
                dureeMax = "Etudiant".equals(adherent.getCategorie()) ? 14 : 
                          "Professeur".equals(adherent.getCategorie()) ? 30 : 21;
                long pretsActifs = pretRepository.findByAdherentIdAndDateRetourEffectifIsNull(adherent.getId()).size();
                if (pretsActifs >= adherent.getQuotaPret()) {
                    throw new IllegalArgumentException("❌ Quota de prêts atteint : L'adhérent " + adherent.getNom() + 
                        " a déjà " + pretsActifs + " prêt(s) actif(s) sur " + adherent.getQuotaPret() + " autorisé(s). " +
                        "Veuillez attendre le retour d'un livre avant d'en emprunter un autre.");
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
            return "redirect:/bibliothecaire/dashboard?success=emprunt&livre=" + livre.getTitre() + "&adherent=" + adherent.getNom();
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

    @PostMapping("/bibliothecaire/retourner-livre")
    public String retournerLivreAvecDate(@RequestParam Long pretId, @RequestParam String dateRetour) {
        Pret pret = pretRepository.findById(pretId).orElse(null);
        if (pret == null || pret.getDateRetourEffectif() != null) {
            return "redirect:/bibliothecaire/dashboard?error=Prêt introuvable ou déjà retourné";
        }
        LocalDate dateRetourEff;
        try {
            dateRetourEff = LocalDate.parse(dateRetour);
        } catch (Exception e) {
            return "redirect:/bibliothecaire/dashboard?error=Date de retour invalide";
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
        return "redirect:/bibliothecaire/dashboard?success=retour";
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
                throw new IllegalArgumentException("❌ Date invalide : La date de retrait doit être dans le futur. " +
                    "Date sélectionnée : " + dateRetrait.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }

            if (adherent.getPenaliteJusquAu() != null && adherent.getPenaliteJusquAu().isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("❌ L'adhérent " + adherent.getNom() + " est sous pénalité jusqu'au " + 
                    adherent.getPenaliteJusquAu().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
                    ". Impossible de réserver pendant cette période.");
            }

            if (livre.getAgeMinimum() > 0 && (adherent.getAge() == null || adherent.getAge() < livre.getAgeMinimum())) {
                throw new IllegalArgumentException("❌ Âge insuffisant : L'adhérent " + adherent.getNom() + " a " + 
                    (adherent.getAge() != null ? adherent.getAge() : "âge non renseigné") + " ans, mais ce livre nécessite au moins " + 
                    livre.getAgeMinimum() + " ans. Titre du livre : " + livre.getTitre());
            }

            long reservationsActives = reservationRepository.findByAdherentIdAndEstActifTrue(adherent.getId()).size();
            if (reservationsActives >= adherent.getQuotaReservation()) {
                throw new IllegalArgumentException("❌ Quota de réservations atteint : L'adhérent " + adherent.getNom() + 
                    " a déjà " + reservationsActives + " réservation(s) active(s) sur " + adherent.getQuotaReservation() + " autorisée(s). " +
                    "Veuillez attendre qu'une réservation soit traitée avant d'en faire une nouvelle.");
            }

            if (!livre.isDisponible()) {
                throw new IllegalArgumentException("❌ Livre non disponible : Le livre '" + livre.getTitre() + 
                    "' n'a plus d'exemplaires disponibles. Veuillez attendre qu'un exemplaire soit retourné.");
            }

            reservation.setDateReservation(LocalDate.now());
            reservation.setDateLimiteRetrait(dateRetrait);
            reservation.setEstActif(true);
            reservationRepository.save(reservation);
            return "redirect:/bibliothecaire/dashboard?success=reservation&livre=" + livre.getTitre() + "&adherent=" + adherent.getNom();
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
                pret.setDateEmprunt(demande.getDateSoumission());
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
                return "redirect:/bibliothecaire/dashboard?success=prolongation&livre=" + pret.getLivre().getTitre() + "&adherent=" + adherent.getNom();
            } else {
                if (pret.isProlonge()) {
                    throw new IllegalArgumentException("❌ Prêt déjà prolongé : Ce prêt a déjà été prolongé une fois. " +
                        "Les prêts ne peuvent être prolongés qu'une seule fois.");
                } else {
                    throw new IllegalArgumentException("❌ Quota de prolongement atteint : L'adhérent " + adherent.getNom() + 
                        " a déjà utilisé " + pret.getNombreProlongements() + " prolongation(s) sur " + adherent.getQuotaProlongement() + " autorisée(s). " +
                        "Quota : " + adherent.getQuotaProlongement() + " prolongation(s) pour un " + adherent.getCategorie().toLowerCase());
                }
            }
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors de la prolongation : " + e.getMessage());
            return "redirect:/bibliothecaire/dashboard";
        }
    }
}