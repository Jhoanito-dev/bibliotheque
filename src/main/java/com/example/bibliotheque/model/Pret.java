package com.example.bibliotheque.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Pret {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Adherent adherent;

    @ManyToOne
    private Livre livre;

    private LocalDate dateEmprunt;
    private LocalDate dateRetourEffectif;
    private LocalDate dateRetourPrevus;
    private int nombreProlongements;
    private boolean penaliteActive;
    private boolean prolonge;
    private String typePret;

    // Getters et setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Adherent getAdherent() { return adherent; }
    public void setAdherent(Adherent adherent) { this.adherent = adherent; }
    public Livre getLivre() { return livre; }
    public void setLivre(Livre livre) { this.livre = livre; }
    public LocalDate getDateEmprunt() { return dateEmprunt; }
    public void setDateEmprunt(LocalDate dateEmprunt) { this.dateEmprunt = dateEmprunt; }
    public LocalDate getDateRetourEffectif() { return dateRetourEffectif; }
    public void setDateRetourEffectif(LocalDate dateRetourEffectif) { this.dateRetourEffectif = dateRetourEffectif; }
    public LocalDate getDateRetourPrevus() { return dateRetourPrevus; }
    public void setDateRetourPrevus(LocalDate dateRetourPrevus) { this.dateRetourPrevus = dateRetourPrevus; }
    public int getNombreProlongements() { return nombreProlongements; }
    public void setNombreProlongements(int nombreProlongements) { this.nombreProlongements = nombreProlongements; }
    public boolean isPenaliteActive() { return penaliteActive; }
    public void setPenaliteActive(boolean penaliteActive) { this.penaliteActive = penaliteActive; }
    public boolean isProlonge() { return prolonge; }
    public void setProlonge(boolean prolonge) { this.prolonge = prolonge; }
    public String getTypePret() { return typePret; }
    public void setTypePret(String typePret) { this.typePret = typePret; }
}