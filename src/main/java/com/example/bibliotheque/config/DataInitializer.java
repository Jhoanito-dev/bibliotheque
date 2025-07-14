package com.example.bibliotheque.config;

import com.example.bibliotheque.model.Adherent;
import com.example.bibliotheque.model.Demande;
import com.example.bibliotheque.model.JourFerie;
import com.example.bibliotheque.model.Livre;
import com.example.bibliotheque.model.Pret;
import com.example.bibliotheque.model.Reservation;
import com.example.bibliotheque.repository.AdherentRepository;
import com.example.bibliotheque.repository.DemandeRepository;
import com.example.bibliotheque.repository.JourFerieRepository;
import com.example.bibliotheque.repository.LivreRepository;
import com.example.bibliotheque.repository.PretRepository;
import com.example.bibliotheque.repository.ReservationRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final LivreRepository livreRepository;
    private final AdherentRepository adherentRepository;
    private final PretRepository pretRepository;
    private final ReservationRepository reservationRepository;
    private final DemandeRepository demandeRepository;
    private final JourFerieRepository jourFerieRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public DataInitializer(LivreRepository livreRepository, AdherentRepository adherentRepository,
                           PretRepository pretRepository, ReservationRepository reservationRepository,
                           DemandeRepository demandeRepository, JourFerieRepository jourFerieRepository) {
        this.livreRepository = livreRepository;
        this.adherentRepository = adherentRepository;
        this.pretRepository = pretRepository;
        this.reservationRepository = reservationRepository;
        this.demandeRepository = demandeRepository;
        this.jourFerieRepository = jourFerieRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Forcer l'initialisation à chaque redémarrage pour les tests
        System.out.println("=== INITIALISATION DES DONNÉES DE TEST ===");
        
        // Vider toutes les tables
        pretRepository.deleteAll();
        reservationRepository.deleteAll();
        demandeRepository.deleteAll();
        livreRepository.deleteAll();
        adherentRepository.deleteAll();
        jourFerieRepository.deleteAll();
        
        System.out.println("Tables vidées, création des nouvelles données...");
            // Créer des livres
            Livre livre1 = new Livre();
            livre1.setTitre("Le Petit Prince");
            livre1.setAuteur("Antoine de Saint-Exupéry");
            livre1.setIsbn("978-2070612750");
            livre1.setCategorie("Classique");
            livre1.setNombreExemplaires(5);
            livre1.setAgeMinimum(0);
            livre1.setDisponible(true);

            Livre livre2 = new Livre();
            livre2.setTitre("1984");
            livre2.setAuteur("George Orwell");
            livre2.setIsbn("978-0451524935");
            livre2.setCategorie("Science-Fiction");
            livre2.setNombreExemplaires(3);
            livre2.setAgeMinimum(16);
            livre2.setDisponible(true);

            // Créer plus de livres
            Livre livre3 = new Livre();
            livre3.setTitre("Harry Potter à l'école des sorciers");
            livre3.setAuteur("J.K. Rowling");
            livre3.setIsbn("978-2070541270");
            livre3.setCategorie("Fantasy");
            livre3.setNombreExemplaires(8);
            livre3.setAgeMinimum(10);
            livre3.setDisponible(true);

            Livre livre4 = new Livre();
            livre4.setTitre("Le Seigneur des Anneaux");
            livre4.setAuteur("J.R.R. Tolkien");
            livre4.setIsbn("978-2070612880");
            livre4.setCategorie("Fantasy");
            livre4.setNombreExemplaires(4);
            livre4.setAgeMinimum(12);
            livre4.setDisponible(true);

            Livre livre5 = new Livre();
            livre5.setTitre("Dune");
            livre5.setAuteur("Frank Herbert");
            livre5.setIsbn("978-2070368228");
            livre5.setCategorie("Science-Fiction");
            livre5.setNombreExemplaires(3);
            livre5.setAgeMinimum(16);
            livre5.setDisponible(true);

            Livre livre6 = new Livre();
            livre6.setTitre("Les Misérables");
            livre6.setAuteur("Victor Hugo");
            livre6.setIsbn("978-2070413118");
            livre6.setCategorie("Classique");
            livre6.setNombreExemplaires(6);
            livre6.setAgeMinimum(14);
            livre6.setDisponible(true);

            Livre livre7 = new Livre();
            livre7.setTitre("Le Petit Nicolas");
            livre7.setAuteur("René Goscinny");
            livre7.setIsbn("978-2070612750");
            livre7.setCategorie("Jeunesse");
            livre7.setNombreExemplaires(10);
            livre7.setAgeMinimum(0);
            livre7.setDisponible(true);

            Livre livre8 = new Livre();
            livre8.setTitre("Astérix le Gaulois");
            livre8.setAuteur("René Goscinny");
            livre8.setIsbn("978-2070612760");
            livre8.setCategorie("BD");
            livre8.setNombreExemplaires(15);
            livre8.setAgeMinimum(0);
            livre8.setDisponible(true);

            livreRepository.save(livre1);
            livreRepository.save(livre2);
            livreRepository.save(livre3);
            livreRepository.save(livre4);
            livreRepository.save(livre5);
            livreRepository.save(livre6);
            livreRepository.save(livre7);
            livreRepository.save(livre8);
            entityManager.flush();

            // Créer des adhérents
            Adherent adherent1 = new Adherent();
            adherent1.setNom("Dupont");
            adherent1.setEmail("jhoanito@ad.com");
            adherent1.setMotDePasse("pass123");
            adherent1.setRole("ROLE_USER"); // Ajout du préfixe ROLE_
            adherent1.setAge(20);
            adherent1.setCategorie("Etudiant");
            adherent1.setQuotaPret(2);
            adherent1.setQuotaReservation(1);
            adherent1.setQuotaProlongement(1);

            Adherent adherent2 = new Adherent();
            adherent2.setNom("Martin");
            adherent2.setEmail("martin@email.com");
            adherent2.setMotDePasse("pass456");
            adherent2.setRole("ROLE_USER"); // Ajout du préfixe ROLE_
            adherent2.setAge(35);
            adherent2.setCategorie("Professeur");
            adherent2.setQuotaPret(5);
            adherent2.setQuotaReservation(3);
            adherent2.setQuotaProlongement(2);

            Adherent bibliothecaire = new Adherent();
            bibliothecaire.setNom("Bibliothécaire");
            bibliothecaire.setEmail("bibliothecaire@example.com");
            bibliothecaire.setMotDePasse("admin123");
            bibliothecaire.setRole("ROLE_ADMIN"); // Pas de ROLE_ ici pour simplifier
            // Les champs suivants peuvent être null pour un admin
            bibliothecaire.setAge(null);
            bibliothecaire.setCategorie(null);
            bibliothecaire.setQuotaPret(null);
            bibliothecaire.setQuotaReservation(null);
            bibliothecaire.setQuotaProlongement(null);

            // Créer plus d'adhérents
            Adherent adherent3 = new Adherent();
            adherent3.setNom("Dubois");
            adherent3.setEmail("dubois@email.com");
            adherent3.setMotDePasse("pass789");
            adherent3.setRole("ROLE_USER");
            adherent3.setAge(25);
            adherent3.setCategorie("Etudiant");
            adherent3.setQuotaPret(2);
            adherent3.setQuotaReservation(1);
            adherent3.setQuotaProlongement(1);

            Adherent adherent4 = new Adherent();
            adherent4.setNom("Leroy");
            adherent4.setEmail("leroy@email.com");
            adherent4.setMotDePasse("pass101");
            adherent4.setRole("ROLE_USER");
            adherent4.setAge(45);
            adherent4.setCategorie("Professeur");
            adherent4.setQuotaPret(5);
            adherent4.setQuotaReservation(3);
            adherent4.setQuotaProlongement(2);

            Adherent adherent5 = new Adherent();
            adherent5.setNom("Moreau");
            adherent5.setEmail("moreau@email.com");
            adherent5.setMotDePasse("pass202");
            adherent5.setRole("ROLE_USER");
            adherent5.setAge(30);
            adherent5.setCategorie("Autre");
            adherent5.setQuotaPret(3);
            adherent5.setQuotaReservation(2);
            adherent5.setQuotaProlongement(1);

            Adherent adherent6 = new Adherent();
            adherent6.setNom("Simon");
            adherent6.setEmail("simon@email.com");
            adherent6.setMotDePasse("pass303");
            adherent6.setRole("ROLE_USER");
            adherent6.setAge(18);
            adherent6.setCategorie("Etudiant");
            adherent6.setQuotaPret(2);
            adherent6.setQuotaReservation(1);
            adherent6.setQuotaProlongement(1);

            adherentRepository.save(adherent1);
            adherentRepository.save(adherent2);
            adherentRepository.save(adherent3);
            adherentRepository.save(adherent4);
            adherentRepository.save(adherent5);
            adherentRepository.save(adherent6);
            adherentRepository.save(bibliothecaire);
            entityManager.flush();

            // Créer un prêt
            Pret pret = new Pret();
            pret.setAdherent(adherent1);
            pret.setLivre(livre1);
            pret.setDateEmprunt(LocalDate.now());
            pret.setDateRetourPrevus(LocalDate.now().plusDays(14));
            pret.setTypePret("maison");
            // Créer plus de prêts
            Pret pret2 = new Pret();
            pret2.setAdherent(adherent3);
            pret2.setLivre(livre3);
            pret2.setDateEmprunt(LocalDate.now().minusDays(5));
            pret2.setDateRetourPrevus(LocalDate.now().plusDays(9));
            pret2.setTypePret("maison");
            pretRepository.save(pret2);

            Pret pret3 = new Pret();
            pret3.setAdherent(adherent4);
            pret3.setLivre(livre4);
            pret3.setDateEmprunt(LocalDate.now().minusDays(10));
            pret3.setDateRetourPrevus(LocalDate.now().plusDays(20));
            pret3.setTypePret("maison");
            pretRepository.save(pret3);

            Pret pret4 = new Pret();
            pret4.setAdherent(adherent5);
            pret4.setLivre(livre5);
            pret4.setDateEmprunt(LocalDate.now().minusDays(2));
            pret4.setDateRetourPrevus(LocalDate.now().plusDays(12));
            pret4.setTypePret("surplace");
            pretRepository.save(pret4);

            entityManager.flush();

            // Créer plus de réservations
            Reservation reservation = new Reservation();
            reservation.setAdherent(adherent2);
            reservation.setLivre(livre2);
            reservation.setDateReservation(LocalDate.now());
            reservation.setDateLimiteRetrait(LocalDate.now().plusDays(6));
            reservation.setEstActif(true);
            reservationRepository.save(reservation);

            Reservation reservation2 = new Reservation();
            reservation2.setAdherent(adherent6);
            reservation2.setLivre(livre6);
            reservation2.setDateReservation(LocalDate.now().minusDays(1));
            reservation2.setDateLimiteRetrait(LocalDate.now().plusDays(5));
            reservation2.setEstActif(true);
            reservationRepository.save(reservation2);

            Reservation reservation3 = new Reservation();
            reservation3.setAdherent(adherent1);
            reservation3.setLivre(livre7);
            reservation3.setDateReservation(LocalDate.now().minusDays(3));
            reservation3.setDateLimiteRetrait(LocalDate.now().plusDays(3));
            reservation3.setEstActif(true);
            reservationRepository.save(reservation3);

            entityManager.flush();

            // Créer des demandes en attente
            Demande demande1 = new Demande();
            demande1.setAdherent(adherent1);
            demande1.setLivre(livre8);
            demande1.setTypeDemande("emprunt");
            demande1.setDateSoumission(LocalDate.now().minusDays(2));
            demande1.setDateRetraitSouhaitee(LocalDate.now().plusDays(3));
            demande1.setTypePret("maison");
            demande1.setStatut("en attente");
            demandeRepository.save(demande1);

            Demande demande2 = new Demande();
            demande2.setAdherent(adherent3);
            demande2.setLivre(livre1);
            demande2.setTypeDemande("reservation");
            demande2.setDateSoumission(LocalDate.now().minusDays(1));
            demande2.setDateRetraitSouhaitee(LocalDate.now().plusDays(5));
            demande2.setTypePret("surplace");
            demande2.setStatut("en attente");
            demandeRepository.save(demande2);

            Demande demande3 = new Demande();
            demande3.setAdherent(adherent5);
            demande3.setLivre(livre4);
            demande3.setTypeDemande("emprunt");
            demande3.setDateSoumission(LocalDate.now());
            demande3.setDateRetraitSouhaitee(LocalDate.now().plusDays(7));
            demande3.setTypePret("maison");
            demande3.setStatut("en attente");
            demandeRepository.save(demande3);

            entityManager.flush();

            // Créer des jours fériés
            JourFerie jourFerie1 = new JourFerie();
            jourFerie1.setDate(LocalDate.of(2025, 7, 14));
            jourFerie1.setDescription("Fête Nationale");

            JourFerie jourFerie2 = new JourFerie();
            jourFerie2.setDate(LocalDate.of(2025, 12, 25));
            jourFerie2.setDescription("Noël");

            jourFerieRepository.save(jourFerie1);
            jourFerieRepository.save(jourFerie2);
            entityManager.flush();

            System.out.println("=== DONNÉES DE TEST CRÉÉES AVEC SUCCÈS ===");
            System.out.println("Date : " + LocalDate.now() + " " + LocalTime.now());
            System.out.println("Livres créés : 8");
            System.out.println("Adhérents créés : 7 (6 utilisateurs + 1 admin)");
            System.out.println("Prêts actifs : 4");
            System.out.println("Réservations : 3");
            System.out.println("Demandes en attente : 3");
            System.out.println("Jours fériés : 2");
            System.out.println("==========================================");
    }
}