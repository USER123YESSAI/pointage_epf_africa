# EPF Africa — Système de Gestion de Pointage
## Rapport de Structure du Projet

Ce document explique le rôle et le fonctionnement de chaque dossier et fichier du projet de système de gestion de pointage des professeurs pour EPF Africa.

---

## Structure Générale du Projet

```
pointage_epf_africa/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── sn/epf/pointage/
│   │   │       ├── config/
│   │   │       ├── dao/
│   │   │       ├── model/
│   │   │       ├── service/
│   │   │       └── ui/
│   │   └── resources/
│   │       ├── css/
│   │       ├── fxml/
│   │       ├── hibernate.cfg.xml
│   │       └── init_db.sql
├── pom.xml
├── mvnw
├── mvnw.cmd
└── .gitignore
```

---

## 1. Racine du Projet

### `pom.xml`
**Rôle :** Fichier de configuration Maven  
**Fonctionnement :** Définit les dépendances du projet (JavaFX, Hibernate, MySQL, BCrypt, JasperReports, JavaMail), la version Java (21), et les plugins de compilation. C'est le fichier central pour la gestion des bibliothèques et le build.

### `mvnw` et `mvnw.cmd`
**Rôle :** Maven Wrapper  
**Fonctionnement :** Scripts permettant d'exécuter Maven sans l'installer localement. `mvnw` pour Linux/Mac, `mvnw.cmd` pour Windows. Ils téléchargent automatiquement la version de Maven spécifiée dans `.mvn/wrapper/maven-wrapper.properties`.

### `.gitignore`
**Rôle :** Configuration Git  
**Fonctionnement :** Indique à Git quels fichiers/dossiers ignorer (comme `target/`, `.idea/`, logs) pour ne pas les inclure dans le dépôt.

### `.idea/`
**Rôle :** Configuration IntelliJ IDEA  
**Fonctionnement :** Contient les fichiers de configuration de l'IDE (non versionné normalement).

---

## 2. Dossier `src/main/java/sn/epf/pointage/`

Ce dossier contient tout le code source Java du projet, organisé en packages selon l'architecture en couches.

### `MainApp.java`
**Rôle :** Point d'entrée de l'application JavaFX  
**Fonctionnement :** 
- Initialise Hibernate via `HibernateConfig.getSessionFactory()`
- Charge la vue de connexion `login.fxml`
- Configure la scène principale
- Gère la fermeture propre de SessionFactory à l'arrêt

### `Launcher.java`
**Rôle :** Classe de lancement alternative  
**Fonctionnement :** Simplement appelle `MainApp.launch()`, permet de lancer l'application depuis une classe distincte.

### `TestConsole.java`
**Rôle :** Script de test console  
**Fonctionnement :** 
- Teste la connexion Hibernate
- Initialise les comptes utilisateurs (admin, scolarité, professeur)
- Crée des données de test (cours, salles, assignations, séances)
- Teste les services (enrôlement, pointage, rapports)
- À exécuter AVANT de lancer l'interface JavaFX pour valider la base de données

---

## 3. Dossier `config/`

Contient la configuration de l'application.

### `HibernateConfig.java`
**Rôle :** Configuration Hibernate (Singleton)  
**Fonctionnement :** 
- Implémente le pattern Singleton pour SessionFactory
- Charge `hibernate.cfg.xml` depuis les ressources
- Fournit une instance unique de SessionFactory pour toute l'application
- Gère la fermeture propre des ressources Hibernate

### `SessionContext.java`
**Rôle :** Gestion de session utilisateur (Singleton)  
**Fonctionnement :** 
- Maintient l'utilisateur connecté et son rôle
- Gère le timeout d'inactivité (30 minutes)
- Fournit des méthodes utilitaires : `isAdmin()`, `isScolarite()`, `isProfesseur()`
- Contrôle les permissions : `peutEnrolerProfesseurs()`, `peutValiderRapports()`

---

## 4. Dossier `model/`

Contient toutes les entités JPA (modèle de données).

### Entités Principales

#### `Professeur.java`
**Rôle :** Entité représentant un professeur  
**Fonctionnement :** 
- Stocke les informations personnelles (nom, prénom, email, téléphone)
- Type de contrat (VACATAIRE/PERMANENT)
- Taux horaire XOF pour le calcul des paiements
- Date d'embauche, filière, spécialité
- Statut actif/inactif
- Relations : `@OneToMany` avec Assignation, RapportMensuel

#### `Cours.java`
**Rôle :** Entité représentant un cours  
**Fonctionnement :** 
- Code unique, intitulé, volume horaire total
- Niveau d'étude, filière, semestre
- Relation : `@OneToMany` avec Assignation

#### `Salle.java`
**Rôle :** Entité représentant une salle  
**Fonctionnement :** 
- Nom, capacité, bâtiment
- Équipements disponibles
- Relation : `@OneToMany` avec Assignation

#### `Assignation.java`
**Rôle :** Entité liant un professeur à un cours dans une salle  
**Fonctionnement :** 
- Année académique, heures prévues
- Relations : `@ManyToOne` avec Professeur, Cours, Salle
- Relations : `@OneToMany` avec SeancePlanifiee, PeriodiciteCours

#### `SeancePlanifiee.java`
**Rôle :** Entité représentant une séance de cours planifiée  
**Fonctionnement :** 
- Date et heure de la séance
- Durée en minutes
- Statut (PLANIFIEE, REALISEE, ANNULEE, REPORTEE)
- Relations : `@ManyToOne` avec Assignation, `@OneToMany` avec Pointage

#### `Pointage.java`
**Rôle :** Entité représentant un pointage de présence  
**Fonctionnement :** 
- Heure du pointage
- Type (DEBUT/FIN)
- Statut (A_LHEURE, EN_RETARD)
- Écart en minutes par rapport à l'heure prévue
- Relations : `@ManyToOne` avec SeancePlanifiee, Professeur

#### `PeriodiciteCours.java`
**Rôle :** Entité définissant la périodicité d'un cours  
**Fonctionnement :** 
- Jour de la semaine
- Heure de début et fin
- Fréquence (HEBDO, BIMENSUEL, MENSUEL)
- Relation : `@ManyToOne` avec Assignation

#### `RapportMensuel.java`
**Rôle :** Entité représentant un rapport mensuel de paiement  
**Fonctionnement :** 
- Mois et année concernés
- Heures réalisées et montant XOF
- Statut (EN_ATTENTE, VALIDE, PAYE)
- Relation : `@ManyToOne` avec Professeur

#### `Utilisateur.java`
**Rôle :** Entité représentant un compte utilisateur  
**Fonctionnement :** 
- Login unique, mot de passe hashé (BCrypt)
- Rôle (ADMIN, SCOLARITE, PROFESSEUR)
- Relation optionnelle `@OneToOne` avec Professeur
- Statut actif/inactif

#### `JournalConnexion.java`
**Rôle :** Entité journalisant les connexions  
**Fonctionnement :** 
- Login, action (CONNEXION/DECONNEXION)
- Horodatage, adresse IP
- Succès/échec de la tentative

### Dossier `model/enums/`

Contient toutes les énumérations du projet :

- `TypeContrat.java` : VACATAIRE, PERMANENT
- `StatutSeance.java` : PLANIFIEE, REALISEE, ANNULEE, REPORTEE
- `TypePointage.java` : DEBUT, FIN
- `StatutPointage.java` : A_LHEURE, EN_RETARD
- `FrequenceCours.java` : HEBDO, BIMENSUEL, MENSUEL
- `StatutRapport.java` : EN_ATTENTE, VALIDE, PAYE
- `RoleUtilisateur.java` : ADMIN, SCOLARITE, PROFESSEUR
- `ResultatPointage.java` : SUCCES, EN_RETARD, TROP_TOT, PROF_INACTIF, DEJA_POINTE

---

## 5. Dossier `dao/`

Contient les Data Access Objects pour la persistance des données.

### `GenericDAO.java`
**Rôle :** Interface générique définissant les opérations CRUD standard  
**Fonctionnement :** Définit les méthodes : `save()`, `findById()`, `findAll()`, `update()`, `delete()`, `exists()`, `count()`

### `AbstractDAO.java`
**Rôle :** Implémentation abstraite de GenericDAO  
**Fonctionnement :** 
- Fournit l'implémentation par défaut des opérations CRUD
- Gère les sessions et transactions Hibernate
- Méthodes protégées `openSession()` pour les sous-classes

### DAOs Spécialisés

#### `ProfesseurDAO.java`
**Rôle :** DAO spécifique pour Professeur  
**Fonctionnement :** Méthodes spécialisées :
- `findByMatricule()` : Recherche par matricule
- `findByEmail()` : Recherche par email (insensible à la casse)
- `findByNom()` : Recherche par nom/prénom (LIKE)
- `findByFiliere()` : Recherche par filière
- `findByTypeContrat()` : Recherche par type de contrat
- `findAllActifs()` : Liste des professeurs actifs
- `countByTypeContrat()` : Compte par type de contrat

#### `SeanceDAO.java`
**Rôle :** DAO spécifique pour SeancePlanifiee  
**Fonctionnement :** Méthodes spécialisées :
- `findSeancesDuJour()` : Séances du jour courant
- `findSeancesSansPointage()` : Séances sans pointage (alertes)
- `findByProfesseurAndMois()` : Séances d'un prof pour un mois
- `findSeancesDuJourParProfesseur()` : Séances du jour d'un prof
- `countSeancesPlanifieesParMois()` : Compte par mois

#### `PointageDAO.java`
**Rôle :** DAO spécifique pour Pointage  
**Fonctionnement :** Méthodes spécialisées :
- `findBySeanceAndType()` : Pointage d'une séance par type
- `findByProfesseurAndMois()` : Pointages d'un prof pour un mois
- `countBySeance()` : Nombre de pointages pour une séance
- `countByProfesseurAndMois()` : Compte par prof et mois

#### `RapportDAO.java`
**Rôle :** DAO spécifique pour RapportMensuel  
**Fonctionnement :** Méthodes spécialisées :
- `findByProfesseurAndPeriode()` : Rapport d'un prof pour une période
- `findRapportsNonPayes()` : Liste des rapports non payés
- `findByProfesseur()` : Tous les rapports d'un prof

#### `CoursDAO.java`, `SalleDAO.java`, `AssignationDAO.java`, `UtilisateurDAO.java`
**Rôle :** DAOs spécifiques pour leurs entités respectives  
**Fonctionnement :** Héritent d'AbstractDAO, fournissent les méthodes CRUD de base.

---

## 6. Dossier `service/`

Contient la logique métier de l'application.

### `AuthService.java`
**Rôle :** Service d'authentification et gestion des sessions  
**Fonctionnement :** 
- `authentifier()` : Vérifie login/mot de passe avec BCrypt
- `deconnecter()` : Déconnecte l'utilisateur et journalise
- `initialiserCompteAdmin()` : Crée le compte admin par défaut
- Journalise toutes les tentatives de connexion dans JournalConnexion
- Utilise SessionContext pour gérer la session active

### `EnrolementService.java`
**Rôle :** Service de gestion du cycle de vie des professeurs  
**Fonctionnement :** 
- `enrollerProfesseur()` : Crée un professeur, génère matricule, crée compte utilisateur
- `desactiverProfesseur()` : Désactive un professeur et son compte
- `mettreAJourProfil()` : Met à jour les informations d'un professeur
- `assignerCours()` : Crée une assignation et génère les séances du semestre
- `genererSeances()` : Algorithme de génération des séances selon la périodicité
- Valide les règles métier (email unique, taux horaire positif, etc.)

### `PointageService.java`
**Rôle :** Service de gestion des pointages (RG-01 à RG-05)  
**Fonctionnement :** 
- `pointer()` : Enregistre un pointage avec validation des règles
- `peutPointer()` : Vérifie si un professeur peut pointer (fenêtre temporelle)
- `getCouleurStatut()` : Retourne la couleur CSS selon le statut
- Implémente les règles :
  - RG-01 : Fenêtre de pointage ±15 minutes
  - RG-02 : Pointage début obligatoire avant fin
  - RG-03 : Un seul pointage par type par séance
  - RG-04 : Professeur doit être actif
  - RG-05 : Calcul automatique du retard

### `RapportService.java`
**Rôle :** Service de génération et validation des rapports mensuels (RG-06)  
**Fonctionnement :** 
- `genererRapportMensuel()` : Génère un rapport pour un prof/mois/année
- `validerRapport()` : Valide un rapport (ADMIN/SCOLARITE uniquement)
- `marquerPaye()` : Marque un rapport comme payé
- `exporterRapportTexte()` : Exporte un rapport en format texte
- RG-06 : Vérifie que toutes les séances sont réalisées avant génération
- Calcule les heures réalisées et le montant XOF

### `DashboardService.java`
**Rôle :** Service de statistiques pour le tableau de bord  
**Fonctionnement :** 
- `getSeancesDuJour()` : Nombre de séances du jour
- `getProfesseursPresentsAujourdhui()` : Professeurs présents
- `getProfesseursAbsentsAujourdhui()` : Professeurs absents
- `getTauxPresenceAujourdhui()` : Taux de présence en pourcentage
- `getNombreVacataires()` : Nombre de vacataires
- `getNombrePermanents()` : Nombre de permanents
- `getAlertesDuJour()` : Séances sans pointage du jour

### `PlanificateurService.java`
**Rôle :** Service bonus de planification automatique  
**Fonctionnement :** 
- `demarrer()` : Démarre un planificateur quotidien à 20h00
- `arreter()` : Arrête le planificateur
- `verifierSeancesEtNotifier()` : Vérifie les séances sans pointage et notifie
- `envoyerEmailScolarite()` : Envoie un email récapitulatif (simulé)
- Utilise ScheduledExecutorService pour l'exécution différée

---

## 7. Dossier `ui/`

Contient les contrôleurs JavaFX pour l'interface graphique.

### `LoginController.java`
**Rôle :** Contrôleur de la page de connexion  
**Fonctionnement :** 
- Gère le formulaire de connexion (login, mot de passe)
- Appelle AuthService pour l'authentification
- Affiche les messages d'erreur
- Navigue vers la page principale si succès
- Initialise le compte admin au premier lancement

### `MainController.java`
**Rôle :** Contrôleur principal de l'application  
**Fonctionnement :** 
- Gère la navigation entre les différentes vues
- Configure le menu latéral selon le rôle de l'utilisateur
- Masque/désactive les boutons selon les permissions
- Gère le timeout d'inactivité (30 min) avec Timeline
- Affiche les informations de l'utilisateur connecté
- Gère la déconnexion

### `DashboardController.java`
**Rôle :** Contrôleur du tableau de bord  
**Fonctionnement :** 
- Affiche les statistiques du jour (séances, présents, absents, taux)
- Affiche un LineChart des présences sur 6 mois
- Affiche un PieChart de la répartition des contrats
- Affiche un TableView des alertes (séances sans pointage)
- Rafraîchit automatiquement les données toutes les 30 secondes

### `ProfesseursController.java`
**Rôle :** Contrôleur de la gestion des professeurs  
**Fonctionnement :** 
- Affiche la liste des professeurs dans un TableView
- Recherche par nom, filtre par type de contrat et filière
- Boutons : Ajouter, Modifier, Désactiver
- Ouvre ProfFormController pour l'ajout/modification
- Applique le contrôle d'accès (ADMIN/SCOLARITE uniquement)

### `ProfFormController.java`
**Rôle :** Contrôleur du formulaire professeur  
**Fonctionnement :** 
- Formulaire d'ajout/modification de professeur
- Validation en temps réel (email, téléphone)
- Upload de photo
- Appelle EnrolementService pour la création/modification
- Génère automatiquement le matricule à la création

### `PlanningController.java`
**Rôle :** Contrôleur du planning des cours  
**Fonctionnement :** 
- Affiche le planning hebdomadaire des séances
- Navigation entre les semaines (précédente/suivante)
- Filtrage par professeur
- Bouton "Nouvelle assignation" (ADMIN/SCOLARITE)
- Ouvre AssignationFormController pour créer une assignation

### `AssignationFormController.java`
**Rôle :** Contrôleur du formulaire d'assignation  
**Fonctionnement :** 
- Formulaire de création d'assignation (prof, cours, salle)
- Définition de la périodicité (jour, heure, fréquence)
- Dates de début et fin de semestre
- Appelle EnrolementService.assignerCours()
- Génère automatiquement les séances du semestre

### `PointageController.java`
**Rôle :** Contrôleur de la page de pointage  
**Fonctionnement :** 
- Affiche les séances du jour du professeur connecté
- Boutons "Pointer DEBUT" et "Pointer FIN"
- Appelle PointageService.pointer()
- Affiche le résultat du pointage (succès, retard, etc.)
- Mise à jour en temps réel du statut des séances

### `RapportsController.java`
**Rôle :** Contrôleur des rapports mensuels  
**Fonctionnement :** 
- Sélection d'un professeur, mois et année
- Génère le rapport via RapportService
- Affiche les informations (heures, montant, statut)
- TableView détaillé des séances du rapport
- Boutons "Valider" (ADMIN/SCOLARITE) et "Exporter"

### Contrôleurs Secondaires

#### `CoursController.java` / `CoursFormController.java`
**Rôle :** Gestion des cours (bonus)  
**Fonctionnement :** CRUD complet pour les cours

#### `SallesController.java` / `SalleFormController.java`
**Rôle :** Gestion des salles (bonus)  
**Fonctionnement :** CRUD complet pour les salles

#### `AssignationsController.java`
**Rôle :** Liste des assignations (bonus)  
**Fonctionnement :** Affichage et gestion des assignations

#### `Toast.java`
**Rôle :** Classe utilitaire pour les notifications  
**Fonctionnement :** Affiche des messages temporaires (succès, erreur) dans l'interface

---

## 8. Dossier `src/main/resources/`

Contient les ressources non-Java de l'application.

### `hibernate.cfg.xml`
**Rôle :** Configuration Hibernate  
**Fonctionnement :** 
- Définit la connexion MySQL (URL, utilisateur, mot de passe)
- Configure le dialecte MySQL
- Définit le fuseau horaire Africa/Dakar
- `hbm2ddl.auto=update` : Met à jour automatiquement le schéma
- Liste toutes les classes d'entités mappées

### `init_db.sql`
**Rôle :** Script SQL d'initialisation de la base  
**Fonctionnement :** 
- Crée la base de données `epf_pointage`
- Configure le jeu de caractères UTF-8
- Configure le fuseau horaire Africa/Dakar
- À exécuter une seule fois avant le premier lancement

### Dossier `css/`

#### `styles.css`
**Rôle :** Feuille de style CSS pour JavaFX  
**Fonctionnement :** 
- Définit les couleurs EPF Africa (#1A2744, #2E75B6)
- Styles pour les composants (boutons, champs, tableaux)
- Styles pour les cartes statistiques
- Styles pour le menu latéral
- Classes utilitaires (stat-green, stat-red, stat-blue)

### Dossier `fxml/`

Contient toutes les vues JavaFX (interface utilisateur).

#### `login.fxml`
**Rôle :** Page de connexion  
**Fonctionnement :** Formulaire avec champ login, mot de passe et bouton de connexion

#### `main.fxml`
**Rôle :** Structure principale de l'application  
**Fonctionnement :** Menu latéral + zone de contenu centrale pour la navigation

#### `dashboard.fxml`
**Rôle :** Tableau de bord  
**Fonctionnement :** Cartes statistiques, graphiques LineChart/PieChart, tableau des alertes

#### `professeurs.fxml`
**Rôle :** Liste des professeurs  
**Fonctionnement :** TableView avec recherche, filtres et boutons d'action

#### `prof_form.fxml`
**Rôle :** Formulaire professeur  
**Fonctionnement :** Champs pour nom, prénom, email, téléphone, contrat, filière, etc.

#### `planning.fxml`
**Rôle :** Planning des cours  
**Fonctionnement :** TableView hebdomadaire avec navigation et filtres

#### `assignation_form.fxml`
**Rôle :** Formulaire d'assignation  
**Fonctionnement :** Sélection prof/cours/salle, définition périodicité, dates semestre

#### `pointage.fxml`
**Rôle :** Page de pointage  
**Fonctionnement :** TableView des séances du jour, boutons de pointage

#### `rapports.fxml`
**Rôle :** Rapports mensuels  
**Fonctionnement :** Sélecteurs prof/mois/année, informations rapport, tableau détaillé

#### Fichiers bonus
- `cours.fxml` / `cours_form.fxml` : Gestion des cours
- `salles.fxml` / `salle_form.fxml` : Gestion des salles
- `assignations.fxml` : Liste des assignations

---

## 9. Flux de Données Typique

### Enrôlement d'un professeur
1. UI : `ProfesseursController` → `ProfFormController`
2. Service : `EnrolementService.enrollerProfesseur()`
3. DAO : `ProfesseurDAO.save()`, `UtilisateurDAO.save()`
4. Base : Tables `professeurs`, `utilisateurs`

### Pointage d'une séance
1. UI : `PointageController`
2. Service : `PointageService.pointer()`
3. DAO : `PointageDAO.save()`, `SeanceDAO.update()`
4. Base : Tables `pointages`, `seances_planifiees`

### Génération d'un rapport
1. UI : `RapportsController`
2. Service : `RapportService.genererRapportMensuel()`
3. DAO : `SeanceDAO`, `PointageDAO`, `RapportDAO`
4. Base : Tables `seances_planifiees`, `pointages`, `rapports_mensuels`

---

## 10. Résumé de l'Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    COUCHE PRÉSENTATION (UI)                  │
│  Contrôleurs JavaFX + Vues FXML + CSS                       │
│  Gère l'interaction utilisateur, navigation, affichage      │
└─────────────────────────────────────────────────────────────┘
                           ↕ utilise
┌─────────────────────────────────────────────────────────────┐
│                      COUCHE MÉTIER (Service)                 │
│  Services : Enrolement, Pointage, Rapport, Auth, Dashboard │
│  Implémente les règles métier RG-01 à RG-06                 │
│  Gère les transactions et la logique d'entreprise            │
└─────────────────────────────────────────────────────────────┘
                           ↕ utilise
┌─────────────────────────────────────────────────────────────┐
│                    COUCHE PERSISTANCE (DAO)                   │
│  DAOs spécialisés + Entités JPA                              │
│  Encapsule l'accès aux données, requêtes JPQL               │
└─────────────────────────────────────────────────────────────┘
                           ↕ utilise
┌─────────────────────────────────────────────────────────────┐
│                      COUCHE CONFIGURATION                     │
│  HibernateConfig, SessionContext                             │
│  Gère SessionFactory et session utilisateur                  │
└─────────────────────────────────────────────────────────────┘
                           ↕ utilise
┌─────────────────────────────────────────────────────────────┐
│                      BASE DE DONNÉES                         │
│  MySQL 8.0 — Base epf_pointage                               │
└─────────────────────────────────────────────────────────────┘
```

---

## Conclusion

Ce projet suit une architecture en couches classique et bien structurée. Chaque dossier a une responsabilité claire :

- **model/** : Données du domaine (entités JPA)
- **dao/** : Accès aux données (CRUD + requêtes spécialisées)
- **service/** : Logique métier (règles de gestion)
- **ui/** : Interface utilisateur (contrôleurs JavaFX)
- **config/** : Configuration de l'application
- **resources/** : Fichiers de configuration, CSS, FXML

Cette séparation facilite la maintenance, les tests et l'évolutivité de l'application.
