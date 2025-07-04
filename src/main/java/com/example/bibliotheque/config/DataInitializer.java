package com.example.bibliotheque.config;

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
    private final JourFerieRepository jourFerieRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public DataInitializer(LivreRepository livreRepository, AdherentRepository adherentRepository,
                           PretRepository pretRepository, ReservationRepository reservationRepository,
                           JourFerieRepository jourFerieRepository) {
        this.livreRepository = livreRepository;
        this.adherentRepository = adherentRepository;
        this.pretRepository = pretRepository;
        this.reservationRepository = reservationRepository;
        this.jourFerieRepository = jourFerieRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (livreRepository.count() == 0 && adherentRepository.count() == 0) {
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

            livreRepository.save(livre1);
            livreRepository.save(livre2);
            entityManager.flush();
            entityManager.refresh(livre1);
            entityManager.refresh(livre2);

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
            bibliothecaire.setRole("ROLE_ADMIN"); // Ajout du préfixe ROLE_
            bibliothecaire.setAge(40);
            bibliothecaire.setCategorie("Autre");
            bibliothecaire.setQuotaPret(5);
            bibliothecaire.setQuotaReservation(3);
            bibliothecaire.setQuotaProlongement(2);

            adherentRepository.save(adherent1);
            adherentRepository.save(adherent2);
            adherentRepository.save(bibliothecaire);
            entityManager.flush();
            entityManager.refresh(adherent1);
            entityManager.refresh(adherent2);
            entityManager.refresh(bibliothecaire);

            // Créer un prêt
            Pret pret = new Pret();
            pret.setAdherent(adherent1);
            pret.setLivre(livre1);
            pret.setDateEmprunt(LocalDate.now());
            pret.setDateRetourPrevus(LocalDate.now().plusDays(14));
            pret.setTypePret("maison");
            pretRepository.save(pret);
            entityManager.flush();
            entityManager.refresh(pret);

            // Créer une réservation
            Reservation reservation = new Reservation();
            reservation.setAdherent(adherent2);
            reservation.setLivre(livre2);
            reservation.setDateReservation(LocalDate.now());
            reservation.setDateLimiteRetrait(LocalDate.now().plusDays(6));
            reservation.setEstActif(true);
            reservationRepository.save(reservation);
            entityManager.flush();
            entityManager.refresh(reservation);

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
            entityManager.refresh(jourFerie1);
            entityManager.refresh(jourFerie2);

            System.out.println("Données initiales insérées avec succès à " + LocalDate.now() + " " + LocalTime.now());
        }
    }
}