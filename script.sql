DROP TABLE IF EXISTS livre;
CREATE TABLE livre (
    id SERIAL PRIMARY KEY,
    titre VARCHAR(255),
    auteur VARCHAR(255),
    isbn VARCHAR(17), 
    categorie VARCHAR(100),
    restrictions VARCHAR(100),
    nombre_exemplaires INT
);

DROP TABLE IF EXISTS exemplaire;
CREATE TABLE exemplaire (
    id SERIAL PRIMARY KEY,
    numero_unique VARCHAR(20),
    statut VARCHAR(50),
    livre_id BIGINT,
    FOREIGN KEY (livre_id) REFERENCES livre(id)
);

DROP TABLE IF EXISTS adherent;
CREATE TABLE adherent (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    type_profil VARCHAR(50),
    cotisation_payee BOOLEAN,
    date_expiration_cotisation DATE,
    penalites VARCHAR(255)
);
-- Insérer 5 livres
INSERT INTO livre (titre, auteur, isbn, categorie, restrictions, nombre_exemplaires) VALUES
('Le Petit Prince', 'Antoine de Saint-Exupéry', '978-2-07-061275-8', 'Littérature', NULL, 3),
('1984', 'George Orwell', '978-0-451-52493-5', 'Science-fiction', 'Réservé aux +18', 2),
('Harry Potter', 'J.K. Rowling', '978-0-747-53269-9', 'Fantasy', NULL, 4),
('Les Misérables', 'Victor Hugo', '978-0-140-44430-5', 'Classique', NULL, 2),
('Dune', 'Frank Herbert', '978-0-441-17271-9', 'Science-fiction', 'Professeurs uniquement', 1);

-- Insérer des exemplaires pour chaque livre
INSERT INTO exemplaire (numero_unique, statut, livre_id) VALUES
('LIVRE001-EX01', 'disponible', 1),
('LIVRE001-EX02', 'disponible', 1),
('LIVRE001-EX03', 'disponible', 1),
('LIVRE002-EX01', 'disponible', 2),
('LIVRE002-EX02', 'disponible', 2),
('LIVRE003-EX01', 'disponible', 3),
('LIVRE003-EX02', 'disponible', 3),
('LIVRE003-EX03', 'disponible', 3),
('LIVRE003-EX04', 'disponible', 3),
('LIVRE004-EX01', 'disponible', 4),
('LIVRE004-EX02', 'disponible', 4),
('LIVRE005-EX01', 'disponible', 5);

-- Insérer 2 adhérents
INSERT INTO adherent (nom, email, type_profil, cotisation_payee, date_expiration_cotisation, penalites) VALUES
('nova', 'nova97pa@gmail.com', 'étudiant', true, '2025-12-31', NULL),
('prof', 'prof@gmail.com', 'professeur', true, '2025-12-31', NULL);