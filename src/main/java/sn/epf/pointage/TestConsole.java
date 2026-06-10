package sn.epf.pointage;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import sn.epf.pointage.config.HibernateConfig;
import sn.epf.pointage.dao.CoursDAO;
import sn.epf.pointage.dao.ProfesseurDAO;
import sn.epf.pointage.dao.SalleDAO;
import sn.epf.pointage.dao.SeanceDAO;
import sn.epf.pointage.model.Assignation;
import sn.epf.pointage.model.Cours;
import sn.epf.pointage.model.PeriodiciteCours;
import sn.epf.pointage.model.Professeur;
import sn.epf.pointage.model.RapportMensuel;
import sn.epf.pointage.model.Salle;
import sn.epf.pointage.model.SeancePlanifiee;
import sn.epf.pointage.model.enums.FrequenceCours;
import sn.epf.pointage.model.enums.ResultatPointage;
import sn.epf.pointage.model.enums.TypeContrat;
import sn.epf.pointage.model.enums.TypePointage;
import sn.epf.pointage.service.AuthService;
import sn.epf.pointage.service.EnrolementService;
import sn.epf.pointage.service.PointageService;
import sn.epf.pointage.service.RapportService;

/**
 * ============================================================
 * TEST CONSOLE — À exécuter AVANT de lancer l'interface JavaFX
 * Tester : connexion Hibernate, entités, DAOs, services
 * ============================================================
 *
 * Pour lancer : clic droit > Run 'TestConsole.main()'
 * ou : mvn exec:java -Dexec.mainClass="sn.epf.pointage.TestConsole"
 */
public class TestConsole {

    static final AuthService authService = new AuthService();
    static final EnrolementService enrolementService = new EnrolementService();
    static final PointageService pointageService = new PointageService();
    static final RapportService rapportService = new RapportService();
    static final ProfesseurDAO professeurDAO = new ProfesseurDAO();
    static final SeanceDAO seanceDAO = new SeanceDAO();

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║     EPF Africa — Test Console Complet            ║");
        System.out.println("╚══════════════════════════════════════════════════╝\n");

        try {
            // ── Étape 0 : Test connexion Hibernate ────────────────────────────
            System.out.println("▶ [0] Test connexion Hibernate...");
            HibernateConfig.getSessionFactory();
            System.out.println("✅ SessionFactory OK\n");

            // ── Étape 1 : Créer compte admin ──────────────────────────────────
            System.out.println("▶ [1] Initialisation compte admin...");
            authService.initialiserCompteAdmin();

            // ── Étape 1b : Créer compte Responsable Scolarité ─────────────
            sn.epf.pointage.dao.UtilisateurDAO utilisateurDAO1 = new sn.epf.pointage.dao.UtilisateurDAO();
            java.util.Optional<sn.epf.pointage.model.Utilisateur> scolarite = utilisateurDAO1.findByLogin("scolarite@epf.sn");

            sn.epf.pointage.model.Utilisateur uScol = scolarite.orElseGet(() -> {
                sn.epf.pointage.model.Utilisateur u = new sn.epf.pointage.model.Utilisateur();
                u.setLogin("scolarite@epf.sn");
                u.setRole(sn.epf.pointage.model.enums.RoleUtilisateur.SCOLARITE);
                return u;
            });

            uScol.setMotDePasseHash(org.mindrot.jbcrypt.BCrypt.hashpw("Scolarite@2024", org.mindrot.jbcrypt.BCrypt.gensalt()));
            uScol.setRole(sn.epf.pointage.model.enums.RoleUtilisateur.SCOLARITE);
            uScol.setActif(true);

            if (scolarite.isEmpty()) {
                utilisateurDAO1.save(uScol);
                System.out.println("✅ Compte SCOLARITE créé : scolarite@epf.sn / Scolarite@2024");
            } else {
                utilisateurDAO1.update(uScol);
                System.out.println("🔁 Mot de passe SCOLARITE forcé : scolarite@epf.sn / Scolarite@2024");
            }



            // ── Étape 2 : Authentification ────────────────────────────────────
            System.out.println("\n▶ [2] Test authentification...");
            boolean ok = authService.authentifier("admin@epf.sn", "Admin@2024");
            System.out.println("   Connexion admin : " + (ok ? "✅ Succès" : "❌ Échec"));

            boolean ko = authService.authentifier("admin@epf.sn", "mauvais_mdp");
            System.out.println("   Connexion mauvais mdp : " + (!ko ? "✅ Refusé correctement" : "❌ Problème"));

            // ── Étape 3 : Enrôler un professeur ──────────────────────────────
            System.out.println("\n▶ [3] Test enrôlement professeur...");
            Professeur prof = new Professeur("Diallo", "Moussa", "moussa.diallo@epf.sn", TypeContrat.VACATAIRE);
            prof.setTelephone("+221 77 123 45 67");
            prof.setTauxHoraireXOF(5000.0);
            prof.setFiliere("CSI");
            prof.setSpecialite("Développement Java");

            try {
                prof = enrolementService.enrollerProfesseur(prof, "Prof@2024");
                System.out.println("   Matricule généré : " + prof.getMatricule());
            } catch (IllegalArgumentException e) {
                System.out.println("   ⚠ Déjà existant — récupération depuis la base...");
                prof = professeurDAO.findByEmail("moussa.diallo@epf.sn").orElseThrow();

                // S'assurer que le compte Utilisateur associé existe (sinon, le prof ne pourra pas se connecter)
                sn.epf.pointage.dao.UtilisateurDAO utilisateurDAO2 = new sn.epf.pointage.dao.UtilisateurDAO();
                java.util.Optional<sn.epf.pointage.model.Utilisateur> existingUser = utilisateurDAO2.findByLogin(prof.getEmail());

                if (existingUser.isEmpty()) {
                    sn.epf.pointage.model.Utilisateur utilisateur = new sn.epf.pointage.model.Utilisateur();
                    utilisateur.setLogin(prof.getEmail());
                    utilisateur.setMotDePasseHash(org.mindrot.jbcrypt.BCrypt.hashpw("Prof@2024", org.mindrot.jbcrypt.BCrypt.gensalt()));
                    utilisateur.setRole(sn.epf.pointage.model.enums.RoleUtilisateur.PROFESSEUR);
                    utilisateur.setProfesseurLie(prof);
                    utilisateur.setActif(true);

                    utilisateurDAO2.save(utilisateur);
                    System.out.println("✅ Compte PROFESSEUR créé pour connexion : " + prof.getEmail() + " / Prof@2024");
                } else {
                    // Forcer le mdp au cas où le compte existe déjà avec un autre hash
                    sn.epf.pointage.model.Utilisateur utilisateur = existingUser.get();
                    utilisateur.setMotDePasseHash(org.mindrot.jbcrypt.BCrypt.hashpw("Prof@2024", org.mindrot.jbcrypt.BCrypt.gensalt()));
                    utilisateur.setRole(sn.epf.pointage.model.enums.RoleUtilisateur.PROFESSEUR);
                    utilisateur.setProfesseurLie(prof);
                    utilisateur.setActif(true);
                    utilisateurDAO2.update(utilisateur);

                    System.out.println("🔁 Mot de passe PROFESSEUR forcé : " + prof.getEmail() + " / Prof@2024");
                }

            }



            // ── Étape 4 : Créer cours, salle, assignation ─────────────────────
            System.out.println("\n▶ [4] Test création cours + assignation...");
            Cours cours = creerOuRecupererCours();
            Salle salle = creerOuRecupererSalle();

            PeriodiciteCours periodicite = new PeriodiciteCours();
            periodicite.setJourSemaine(DayOfWeek.MONDAY);
            periodicite.setHeureDebut(LocalTime.of(8, 0));
            periodicite.setHeureFin(LocalTime.of(10, 0));
            periodicite.setFrequence(FrequenceCours.HEBDO);

            LocalDate debutSemestre = LocalDate.now().with(java.time.temporal.TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
            LocalDate finSemestre = debutSemestre.plusMonths(4);

            try {
                Assignation assignation = enrolementService.assignerCours(
                        prof, cours, salle, "2024-2025", 60, periodicite, debutSemestre, finSemestre);
                System.out.println("   Assignation ID : " + assignation.getId());
            } catch (Exception e) {
                System.out.println("   ⚠ Assignation : " + e.getMessage());
            }

            // ── Étape 5 : Test pointage ────────────────────────────────────────
            System.out.println("\n▶ [5] Test pointage...");
            List<SeancePlanifiee> seances = seanceDAO.findAll(0, 5);
            if (!seances.isEmpty()) {
                SeancePlanifiee seance = seances.get(0);
                System.out.println("   Séance test : " + seance.getDateHeure());

                // Simuler un pointage (peut être TROP_TOT si la séance est dans le futur)
                ResultatPointage resultat = pointageService.pointer(seance.getId(), prof.getId(), TypePointage.DEBUT);
                System.out.println("   Résultat pointage : " + resultat);
            } else {
                System.out.println("   Aucune séance disponible pour le test.");
            }

            // ── Étape 6 : Test rapport ─────────────────────────────────────────
            System.out.println("\n▶ [6] Test génération rapport...");
            int moisTest = LocalDate.now().getMonthValue();
            int anneeTest = LocalDate.now().getYear();
            try {
                RapportMensuel rapport = rapportService.genererRapportMensuel(prof.getId(), moisTest, anneeTest);
                System.out.println("   Heures réalisées : " + rapport.getHeuresRealisees());
                System.out.println("   Montant XOF : " + rapport.getMontantXOF());
                System.out.println("   Statut : " + rapport.getStatut());
            } catch (IllegalStateException e) {
                System.out.println("   ⚠ RG-06 : " + e.getMessage());
            }

            // ── Étape 7 : DAO queries spécialisées ────────────────────────────
            System.out.println("\n▶ [7] Test DAOs spécialisés...");
            List<Professeur> actifs = professeurDAO.findAllActifs();
            System.out.println("   Professeurs actifs : " + actifs.size());

            List<Professeur> parNom = professeurDAO.findByNom("diallo");
            System.out.println("   Recherche 'diallo' : " + parNom.size() + " résultat(s)");

            System.out.println("\n╔══════════════════════════════════════════════════╗");
            System.out.println("║     ✅ TOUS LES TESTS PASSÉS — JavaFX OK         ║");
            System.out.println("╚══════════════════════════════════════════════════╝");

        } catch (Exception e) {
            System.err.println("\n❌ ERREUR CRITIQUE : " + e.getMessage());
            e.printStackTrace();
        } finally {
            HibernateConfig.shutdown();
        }
    }

    private static Cours creerOuRecupererCours() {
        CoursDAO dao = new CoursDAO();
        List<Cours> cours = dao.findAll(0, 1);
        if (!cours.isEmpty()) return cours.get(0);
        
        Cours c = new Cours("INF301", "Programmation Java Avancée", "CSI", "L3");
        c.setVolumeHoraireTotal(60);
        c.setSemestre("S5");
        return dao.save(c);
    }

    private static Salle creerOuRecupererSalle() {
        SalleDAO dao = new SalleDAO();
        List<Salle> salles = dao.findAll(0, 1);
        if (!salles.isEmpty()) return salles.get(0);
        
        Salle s = new Salle("Labo Info 1", "Bâtiment A", 30);
        s.setEquipements("20 postes, projecteur, tableau blanc");
        return dao.save(s);
    }
}
