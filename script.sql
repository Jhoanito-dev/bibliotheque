-- Creating the bibliotheque database
CREATE DATABASE bibliotheque;

-- Connecting to the bibliotheque database
\connect bibliotheque

-- Creating the JourFerie table
CREATE TABLE jour_ferie (
    id BIGSERIAL PRIMARY KEY,
    date DATE NOT NULL,
    description VARCHAR(255)
);

-- Creating the Livre table
CREATE TABLE livre (
    id BIGSERIAL PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    auteur VARCHAR(255),
    isbn VARCHAR(13),
    categorie VARCHAR(100),
    nombre_exemplaires INTEGER NOT NULL DEFAULT 0,
    age_minimum INTEGER,
    disponible BOOLEAN NOT NULL DEFAULT TRUE
);

-- Creating the Adherent table
CREATE TABLE adherent (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    mot_de_passe VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    age INTEGER,
    penalite_jusqu_au DATE,
    categorie VARCHAR(100),
    quota_pret INTEGER NOT NULL DEFAULT 0,
    quota_reservation INTEGER NOT NULL DEFAULT 0,
    quota_prolongement INTEGER NOT NULL DEFAULT 0
);

-- Ajout des colonnes pour la gestion d'abonnement des adhérents
ALTER TABLE adherent
    ADD COLUMN date_debut_abonnement DATE,
    ADD COLUMN date_fin_abonnement DATE;

-- Creating the Demande table
CREATE TABLE demande (
    id BIGSERIAL PRIMARY KEY,
    adherent_id BIGINT NOT NULL,
    livre_id BIGINT NOT NULL,
    date_soumission DATE NOT NULL,
    type_demande VARCHAR(50) NOT NULL,
    date_retrait_souhaitee DATE,
    statut VARCHAR(50) NOT NULL,
    type_pret VARCHAR(50),
    FOREIGN KEY (adherent_id) REFERENCES adherent(id),
    FOREIGN KEY (livre_id) REFERENCES livre(id)
);

-- Creating the Reservation table
CREATE TABLE reservation (
    id BIGSERIAL PRIMARY KEY,
    adherent_id BIGINT NOT NULL,
    livre_id BIGINT NOT NULL,
    date_reservation DATE NOT NULL,
    date_limite_retrait DATE NOT NULL,
    est_actif BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (adherent_id) REFERENCES adherent(id),
    FOREIGN KEY (livre_id) REFERENCES livre(id)
);

-- Creating the Pret table
CREATE TABLE pret (
    id BIGSERIAL PRIMARY KEY,
    adherent_id BIGINT NOT NULL,
    livre_id BIGINT NOT NULL,
    date_emprunt DATE NOT NULL,
    date_retour_effectif DATE,
    date_retour_prevus DATE NOT NULL,
    nombre_prolongements INTEGER NOT NULL DEFAULT 0,
    penalite_active BOOLEAN NOT NULL DEFAULT FALSE,
    prolonge BOOLEAN NOT NULL DEFAULT FALSE,
    type_pret VARCHAR(50),
    FOREIGN KEY (adherent_id) REFERENCES adherent(id),
    FOREIGN KEY (livre_id) REFERENCES livre(id)
);

-- Adding indexes for better performance
CREATE INDEX idx_demande_adherent_id ON demande(adherent_id);
CREATE INDEX idx_demande_livre_id ON demande(livre_id);
CREATE INDEX idx_reservation_adherent_id ON reservation(adherent_id);
CREATE INDEX idx_reservation_livre_id ON reservation(livre_id);
CREATE INDEX idx_pret_adherent_id ON pret(adherent_id);
CREATE INDEX idx_pret_livre_id ON pret(livre_id);