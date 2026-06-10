package sn.epf.pointage.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;

import sn.epf.pointage.dao.AssignationDAO;
import sn.epf.pointage.dao.ProfesseurDAO;
import sn.epf.pointage.dao.SeanceDAO;
import sn.epf.pointage.dao.UtilisateurDAO;
import sn.epf.pointage.model.Assignation;
import sn.epf.pointage.model.Cours;
import sn.epf.pointage.model.PeriodiciteCours;
import sn.epf.pointage.model.Professeur;
import sn.epf.pointage.model.Salle;
import sn.epf.pointage.model.SeancePlanifiee;
import sn.epf.pointage.model.Utilisateur;
import sn.epf.pointage.model.enums.RoleUtilisateur;
import sn.epf.pointage.model.enums.StatutSeance;
/**
 * Service gérant le cycle de vie complet d'un professeur.
 * Ne dépend jamais de l'interface JavaFX.
 */
public class EnrolementService {

    private final ProfesseurDAO professeurDAO = new ProfesseurDAO();
    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private final AssignationDAO assignationDAO = new AssignationDAO();
    private final SeanceDAO seanceDAO = new SeanceDAO();

    /**
     * Enrôle un nouveau professeur.
     * Génère le matricule, hache le mot de passe, crée le compte Utilisateur.
     */
    public Professeur enrollerProfesseur(Professeur professeur, String motDePasseInitial) {
        // Validation champs obligatoires
        validerProfesseur(professeur);

        // Vérifier unicité email
        if (professeurDAO.findByEmail(professeur.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Un professeur avec cet email existe déjà : " + professeur.getEmail());
        }

        // Générer le matricule
        String matricule = genererMatricule(professeur);
        professeur.setMatricule(matricule);

        if (professeur.getDateEmbauche() == null) {
            professeur.setDateEmbauche(LocalDate.now());
        }
        professeur.setActif(true);

        // Sauvegarder le professeur
        professeurDAO.save(professeur);

        // Créer le compte Utilisateur
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setLogin(professeur.getEmail());
        utilisateur.setMotDePasseHash(BCrypt.hashpw(motDePasseInitial, BCrypt.gensalt()));
        utilisateur.setRole(RoleUtilisateur.PROFESSEUR);
        utilisateur.setProfesseurLie(professeur);
        utilisateur.setActif(true);
        utilisateurDAO.save(utilisateur);

        System.out.println("✅ Professeur enrôlé : " + matricule + " | " + professeur.getNomComplet());
        return professeur;
    }

    /**
     * Désactive un professeur (RG-05).
     * Annule ses séances futures, révoque son accès.
     */
    public void desactiverProfesseur(Long professeurId, String raison) {
        Professeur prof = professeurDAO.findById(professeurId)
                .orElseThrow(() -> new IllegalArgumentException("Professeur introuvable : " + professeurId));

        prof.setActif(false);
        professeurDAO.update(prof);
        
 seanceDAO.findAll().stream()
            .filter(s -> s.getAssignation() != null
                    && s.getAssignation().getProfesseur() != null
                    && s.getAssignation().getProfesseur().getId().equals(professeurId)
                    && s.getStatut() == StatutSeance.PLANIFIEE
                    && s.getDateHeure().isAfter(LocalDateTime.now()))
            .forEach(s -> {
                s.setStatut(StatutSeance.ANNULEE);
                s.setObservations("Annulée automatiquement — professeur désactivé.");
                seanceDAO.update(s);
            });
        // Désactiver son compte utilisateur
        utilisateurDAO.findAll().stream()
                .filter(u -> u.getProfesseurLie() != null && u.getProfesseurLie().getId().equals(professeurId))
                .findFirst()
                .ifPresent(u -> {
                    u.setActif(false);
                    utilisateurDAO.update(u);
                });

        System.out.println("🚫 Professeur désactivé : " + prof.getNomComplet() + " | Raison: " + raison);
    }

    /**
     * Met à jour le profil d'un professeur avec contrôles de validation.
     */
    public Professeur mettreAJourProfil(Professeur professeur) {
        validerProfesseur(professeur);

        // Vérifier unicité email (sauf si c'est le même)
        professeurDAO.findByEmail(professeur.getEmail()).ifPresent(existing -> {
            if (!existing.getId().equals(professeur.getId())) {
                throw new IllegalArgumentException("Email déjà utilisé par un autre professeur.");
            }
        });

        return professeurDAO.update(professeur);
    }

    /**
     * Crée une Assignation et génère toutes les SeancesPlanifiees du semestre.
     */
    public Assignation assignerCours(Professeur professeur, Cours cours, Salle salle,
                                      String anneeAcademique, int heuresPrevues,
                                      PeriodiciteCours periodicite,
                                      LocalDate dateDebutSemestre, LocalDate dateFinSemestre) {
        // Créer l'assignation
        Assignation assignation = new Assignation();
        assignation.setProfesseur(professeur);
        assignation.setCours(cours);
        assignation.setSalle(salle);
        assignation.setAnneeAcademique(anneeAcademique);
        assignation.setHeuresPrevues(heuresPrevues);
        assignationDAO.save(assignation);

        // Lier la périodicité à l'assignation
        periodicite.setAssignation(assignation);

        // Calculer la durée en minutes si non définie
        if (periodicite.getDureeMinutes() == null && periodicite.getHeureDebut() != null && periodicite.getHeureFin() != null) {
            int duree = (int) java.time.Duration.between(periodicite.getHeureDebut(), periodicite.getHeureFin()).toMinutes();
            periodicite.setDureeMinutes(duree);
        }

        // Générer les séances automatiquement
        List<SeancePlanifiee> seances = genererSeances(assignation, periodicite, dateDebutSemestre, dateFinSemestre);
        seances.forEach(seanceDAO::save);

        System.out.println("📅 Assignation créée : " + cours.getCode() + " | " + seances.size() + " séances générées.");
        return assignation;
    }

    // ─── Méthodes privées ─────────────────────────────────────────────────────

    private void validerProfesseur(Professeur p) {
        if (p.getNom() == null || p.getNom().isBlank()) throw new IllegalArgumentException("Le nom est obligatoire.");
        if (p.getPrenom() == null || p.getPrenom().isBlank()) throw new IllegalArgumentException("Le prénom est obligatoire.");
        if (p.getEmail() == null || !p.getEmail().matches("^[\\w.+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("Email invalide : " + p.getEmail());
        }
        if (p.getTypeContrat() == null) throw new IllegalArgumentException("Le type de contrat est obligatoire.");
        if (p.getTelephone() != null && !p.getTelephone().isBlank() && !p.getTelephone().matches("^[\\d\\s+\\-()]{7,20}$")) {
            throw new IllegalArgumentException("Format téléphone invalide.");
        }
    }

    private String genererMatricule(Professeur professeur) {
        int annee = LocalDate.now().getYear();
        String initiales = (professeur.getPrenom().substring(0, 1) + professeur.getNom().substring(0, 1)).toUpperCase();
        long count = professeurDAO.count() + 1;
        return String.format("EPF-%d-%s-%03d", annee, initiales, count);
    }

    private List<SeancePlanifiee> genererSeances(Assignation assignation, PeriodiciteCours periodicite,
                                                   LocalDate debut, LocalDate fin) {
        List<SeancePlanifiee> seances = new ArrayList<>();
        DayOfWeek jourCible = periodicite.getJourSemaine();

        // Trouver le premier jour correspondant à partir de la date de début
        LocalDate current = debut.with(TemporalAdjusters.nextOrSame(jourCible));

        int semaineSaut = switch (periodicite.getFrequence()) {
            case HEBDO -> 1;
            case BIMENSUEL -> 2;
            case MENSUEL -> 4;
        };

        while (!current.isAfter(fin)) {
            LocalDateTime dateHeure = current.atTime(periodicite.getHeureDebut());
            SeancePlanifiee seance = new SeancePlanifiee(assignation, dateHeure, periodicite.getDureeMinutes());
            seances.add(seance);
            current = current.plusWeeks(semaineSaut);
        }

        return seances;
    }
}
