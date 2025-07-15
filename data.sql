-- Vider toutes les tables (ordre pour respecter les contraintes de clés étrangères)
DELETE FROM pret;
DELETE FROM reservation;
DELETE FROM demande;
DELETE FROM livre;
DELETE FROM adherent;
DELETE FROM jour_ferie;

-- Réinitialiser les séquences d'auto-incrément (pour PostgreSQL)
ALTER SEQUENCE pret_id_seq RESTART WITH 1;
ALTER SEQUENCE reservation_id_seq RESTART WITH 1;
ALTER SEQUENCE demande_id_seq RESTART WITH 1;
ALTER SEQUENCE livre_id_seq RESTART WITH 1;
ALTER SEQUENCE adherent_id_seq RESTART WITH 1;
ALTER SEQUENCE jour_ferie_id_seq RESTART WITH 1;

-- Données initiales
-- LIVRES
INSERT INTO livre (titre, auteur, isbn, categorie, nombre_exemplaires, age_minimum, disponible) VALUES
('Ny Zanak''i Dadaibe', 'Jean-Joseph Rabearivelo', '978-2070612751', 'Classique', 5, 0, true),
('Iarivo amineko', 'Elie Rajaonarison', '978-0451524936', 'Poésie', 3, 12, true),
('Madagascar, une île à la dérive', 'Michèle Rakotoson', '978-2070541271', 'Essai', 8, 16, true),
('Les Cauchemars du gecko', 'Johary Ravaloson', '978-2070612881', 'Roman', 4, 14, true),
('Lamba ho an''ny maty', 'David Jaomanoro', '978-2070368229', 'Théâtre', 3, 16, true),
('Tanteraka', 'Charlotte Rafenomanjato', '978-2070413119', 'Roman', 6, 14, true),
('Ny Voay', 'Tojo Andriamanantoanina', '978-2070612752', 'Jeunesse', 10, 0, true),
('Les Aventures de Gasy', 'Rado', '978-2070612761', 'BD', 15, 0, true);

-- ADHERENTS
INSERT INTO adherent (nom, email, mot_de_passe, role, age, categorie, quota_pret, quota_reservation, quota_prolongement, date_debut_abonnement, date_fin_abonnement) VALUES
('Jhoanito', 'jhoanito@ad.com', 'pass123', 'ROLE_USER', 20, 'Etudiant', 2, 1, 1, '2022-01-01', '2023-01-01'),
('Rakoto', 'rakoto@email.com', 'pass456', 'ROLE_USER', 35, 'Professeur', 5, 3, 2, '2024-01-01', '2025-12-31'),
('Rasoa', 'rasoa@email.com', 'pass789', 'ROLE_USER', 14, 'Etudiant', 2, 1, 1, '2024-01-01', '2025-12-31'),
('Randria', 'randria@email.com', 'pass101', 'ROLE_USER', 45, 'Professeur', 5, 3, 2, '2024-01-01', '2025-12-31'),
('Andriamalala', 'andriamalala@email.com', 'pass202', 'ROLE_USER', 24, 'Autre', 3, 2, 1, '2024-01-01', '2025-12-31'),
('Nirina', 'nirina@email.com', 'pass303', 'ROLE_USER', 18, 'Etudiant', 2, 1, 1, '2024-01-01', '2025-12-31'),
('Bibliothécaire', 'bibliothecaire@example.com', 'admin123', 'ROLE_ADMIN', NULL, NULL, NULL, NULL, NULL, NULL, NULL);

-- PRETS
-- Supposons que les IDs générés pour les livres et adhérents commencent à 1 et suivent l'ordre d'insertion ci-dessus
-- INSERT INTO pret (adherent_id, livre_id, date_emprunt, date_retour_prevus, type_pret) VALUES
-- (1, 1, '2024-06-01', '2024-06-15', 'maison'),
-- (3, 3, '2024-05-27', '2024-06-10', 'maison'),
-- (4, 4, '2024-05-22', '2024-06-21', 'maison'),
-- (5, 5, '2024-05-30', '2024-06-11', 'surplace');

-- RESERVATIONS
-- INSERT INTO reservation (adherent_id, livre_id, date_reservation, date_limite_retrait, est_actif) VALUES
-- (2, 2, '2024-06-01', '2024-06-07', true),
-- (6, 6, '2024-05-31', '2024-06-06', true),
-- (1, 7, '2024-05-29', '2024-06-04', true);

-- JOURS FERIES
INSERT INTO jour_ferie (date, description) VALUES
('2025-06-26', 'Fête de l''Indépendance'),
('2025-03-29', 'Martyrs de l''Insurrection de 1947'),
('2025-01-01', 'Nouvel An'),
('2025-03-08', 'Journée Internationale de la Femme'),
('2025-05-01', 'Fête du Travail'),
('2025-11-01', 'Toussaint');