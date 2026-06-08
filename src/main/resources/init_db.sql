-- ============================================================
-- EPF Africa — Script SQL
-- Création de la base de données + données de test
-- ============================================================

-- Créer la base
CREATE DATABASE IF NOT EXISTS epf_pointage
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE epf_pointage;

-- ============================================================
-- Données de test (à insérer APRÈS que Hibernate ait créé
-- les tables via hbm2ddl.auto=update au premier lancement)
-- ============================================================

-- Compte Admin (mot de passe : Admin@2024)
-- Note : le hash BCrypt est inséré automatiquement par AuthService.initialiserCompteAdmin()
-- Exécutez d'abord TestConsole.java puis ces données de test

-- Salles
INSERT IGNORE INTO salles (id, nom, capacite, batiment, equipements) VALUES
(1, 'Labo Info 1', 30, 'Bâtiment A', '20 postes, projecteur, tableau blanc'),
(2, 'Labo Info 2', 25, 'Bâtiment A', '25 postes, projecteur'),
(3, 'Amphi 200',  200, 'Bâtiment B', 'Sono, vidéoprojecteur HD'),
(4, 'Salle TP Réseau', 20, 'Bâtiment C', 'Équipements Cisco, 20 postes'),
(5, 'Salle de cours A1', 40, 'Bâtiment A', 'Tableau, projecteur');

-- Cours
INSERT IGNORE INTO cours (id, code, intitule, volume_horaire_total, niveau_etude, filiere, semestre) VALUES
(1, 'INF301', 'Programmation Java Avancée',    60, 'L3', 'CSI', 'S5'),
(2, 'INF302', 'Bases de Données Avancées',     45, 'L3', 'CSI', 'S5'),
(3, 'INF303', 'Réseaux et Protocoles',         45, 'L3', 'CSI', 'S5'),
(4, 'INF401', 'Architecture des SI',           60, 'L3', 'CSI', 'S6'),
(5, 'INF402', 'Développement Web Full Stack',  60, 'L3', 'CSI', 'S6'),
(6, 'GE301',  'Électronique Numérique',        45, 'L3', 'GE',  'S5'),
(7, 'GC301',  'Mécanique des Fluides',         45, 'L3', 'GC',  'S5');

-- ============================================================
-- Requêtes utiles pour vérifier l'état du système
-- ============================================================

-- Voir tous les professeurs actifs
-- SELECT p.matricule, p.nom, p.prenom, p.type_contrat, p.filiere
-- FROM professeurs p WHERE p.actif = 1 ORDER BY p.nom;

-- Séances du jour sans pointage (absences)
-- SELECT p.nom, p.prenom, c.intitule, sp.date_heure, sp.statut
-- FROM seances_planifiees sp
-- JOIN assignations a ON sp.assignation_id = a.id
-- JOIN professeurs p ON a.professeur_id = p.id
-- JOIN cours c ON a.cours_id = c.id
-- WHERE DATE(sp.date_heure) = CURDATE()
-- AND sp.id NOT IN (SELECT DISTINCT seance_id FROM pointages WHERE type_pointage = 'DEBUT')
-- ORDER BY sp.date_heure;

-- Rapport mensuel résumé
-- SELECT p.nom, p.prenom, r.mois, r.annee, r.heures_realisees, r.montant_xof, r.statut
-- FROM rapports_mensuels r
-- JOIN professeurs p ON r.professeur_id = p.id
-- ORDER BY r.annee DESC, r.mois DESC;

-- Statistiques de présence
-- SELECT
--   COUNT(*) AS total_seances,
--   SUM(CASE WHEN statut = 'REALISEE' THEN 1 ELSE 0 END) AS realisees,
--   SUM(CASE WHEN statut = 'PLANIFIEE' THEN 1 ELSE 0 END) AS planifiees,
--   ROUND(SUM(CASE WHEN statut = 'REALISEE' THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 1) AS taux_presence
-- FROM seances_planifiees
-- WHERE MONTH(date_heure) = MONTH(NOW())
-- AND YEAR(date_heure) = YEAR(NOW());
