package com.example.bibliotheque.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Demande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Adherent adherent;

    @ManyToOne
    private Livre livre;

    private LocalDate dateSoumission;
    private String typeDemande; // "pret" ou "reservation"
    private LocalDate dateRetraitSouhaitee; // Pour les réservations
    private String statut; // "en attente", "valide", "rejete"
    private String typePret; // "surplace" ou "maison" pour les prêts

    // Getters et setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Adherent getAdherent() { return adherent; }
    public void setAdherent(Adherent adherent) { this.adherent = adherent; }
    public Livre getLivre() { return livre; }
    public void setLivre(Livre livre) { this.livre = livre; }
    public LocalDate getDateSoumission() { return dateSoumission; }
    public void setDateSoumission(LocalDate dateSoumission) { this.dateSoumission = dateSoumission; }
    public String getTypeDemande() { return typeDemande; }
    public void setTypeDemande(String typeDemande) { this.typeDemande = typeDemande; }
    public LocalDate getDateRetraitSouhaitee() { return dateRetraitSouhaitee; }
    public void setDateRetraitSouhaitee(LocalDate dateRetraitSouhaitee) { this.dateRetraitSouhaitee = dateRetraitSouhaitee; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public String getTypePret() { return typePret; }
    public void setTypePret(String typePret) { this.typePret = typePret; }
}