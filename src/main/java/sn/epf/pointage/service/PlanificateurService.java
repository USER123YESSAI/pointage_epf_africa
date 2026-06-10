package sn.epf.pointage.service;

import sn.epf.pointage.dao.SeanceDAO;
import sn.epf.pointage.model.SeancePlanifiee;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.time.LocalTime;
import java.time.LocalDate;

/**
 * BONUS : Planificateur automatique.
 * Vérifie chaque soir les séances sans pointage et envoie un récapitulatif.
 */
public class PlanificateurService {

    private final SeanceDAO seanceDAO = new SeanceDAO();
    private ScheduledExecutorService scheduler;

    public void demarrer() {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "planificateur-epf");
            t.setDaemon(true);
            return t;
        });

        // Calculer le délai jusqu'à 20h00 ce soir
        long delaiInitialSecondes = calculerDelaiJusquaHeure(20, 0);

        // Exécuter tous les jours à 20h00
        scheduler.scheduleAtFixedRate(
                this::verifierSeancesEtNotifier,
                delaiInitialSecondes,
                TimeUnit.DAYS.toSeconds(1),
                TimeUnit.SECONDS
        );

        System.out.println("⏰ Planificateur démarré — vérification quotidienne à 20h00.");
    }

    public void arreter() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            System.out.println("Planificateur arrêté.");
        }
    }

    private void verifierSeancesEtNotifier() {
        System.out.println("🔍 Vérification des séances sans pointage du " + LocalDate.now());

        List<SeancePlanifiee> seancesSansPointage = seanceDAO.findSeancesSansPointage();

        if (seancesSansPointage.isEmpty()) {
            System.out.println("✅ Aucune absence aujourd'hui.");
            return;
        }

        // Construire le récapitulatif
        StringBuilder recap = new StringBuilder();
        recap.append("=== RÉCAPITULATIF DES ABSENCES — ").append(LocalDate.now()).append(" ===\n\n");
        recap.append(seancesSansPointage.size()).append(" séance(s) sans pointage :\n\n");

        for (SeancePlanifiee s : seancesSansPointage) {
            String profNom = s.getAssignation() != null && s.getAssignation().getProfesseur() != null
                    ? s.getAssignation().getProfesseur().getNomComplet() : "N/A";
            String cours = s.getAssignation() != null && s.getAssignation().getCours() != null
                    ? s.getAssignation().getCours().getIntitule() : "N/A";
            recap.append(String.format("- %s | %s | %s%n",
                    profNom, cours, s.getDateHeure()));
        }

        System.out.println(recap);
        envoyerEmailScolarite(recap.toString());
    }

    private void envoyerEmailScolarite(String contenu) {
        // Configuration JavaMail — à compléter avec les vraies credentials SMTP
        // Exemple d'intégration JavaMail :
        /*
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session mailSession = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("noreply@epf.sn", "MOT_DE_PASSE");
            }
        });

        try {
            Message message = new MimeMessage(mailSession);
            message.setFrom(new InternetAddress("noreply@epf.sn"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse("scolarite@epf.sn"));
            message.setSubject("EPF Africa — Récapitulatif absences " + LocalDate.now());
            message.setText(contenu);
            Transport.send(message);
            System.out.println("📧 Email envoyé à la scolarité.");
        } catch (MessagingException e) {
            System.err.println("Erreur envoi email : " + e.getMessage());
        }
        */
        System.out.println("📧 [SIMULATION] Email récapitulatif prêt à être envoyé à scolarite@epf.sn");
    }

    private long calculerDelaiJusquaHeure(int heure, int minute) {
        LocalTime maintenant = LocalTime.now();
        LocalTime cible = LocalTime.of(heure, minute);
        long secondes = java.time.Duration.between(maintenant, cible).getSeconds();
        if (secondes < 0) secondes += TimeUnit.DAYS.toSeconds(1); // demain
        return secondes;
    }
}
