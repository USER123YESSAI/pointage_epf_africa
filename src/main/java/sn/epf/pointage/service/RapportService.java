package sn.epf.pointage.service;

import java.util.List;
import java.util.Optional;

import sn.epf.pointage.dao.PointageDAO;
import sn.epf.pointage.dao.ProfesseurDAO;
import sn.epf.pointage.dao.RapportDAO;
import sn.epf.pointage.dao.SeanceDAO;
import sn.epf.pointage.model.Professeur;
import sn.epf.pointage.model.RapportMensuel;
import sn.epf.pointage.model.SeancePlanifiee;
import sn.epf.pointage.model.enums.StatutRapport;
import sn.epf.pointage.model.enums.StatutSeance;
import sn.epf.pointage.model.enums.TypePointage;

/**
 * Service de génération et validation des rapports mensuels.
 * Sert de base pour la comptabilité et la paie.
 */

public class RapportService {



    private final RapportDAO rapportDAO = new RapportDAO();
    private final SeanceDAO seanceDAO = new SeanceDAO();
    private final ProfesseurDAO professeurDAO = new ProfesseurDAO();
    private final PointageDAO pointageDAO = new PointageDAO();

    /**
     * Génère le rapport mensuel d'un professeur.
     * RG-06 : bloqué si des séances restent en statut PLANIFIEE pour ce mois.
     */
    public RapportMensuel genererRapportMensuel(Long professeurId, int mois, int annee) {

        Professeur professeur = professeurDAO.findById(professeurId)
                .orElseThrow(() -> new IllegalArgumentException("Professeur introuvable : " + professeurId));

        // RG-06 : Vérifier qu'aucune séance du mois n'est encore PLANIFIEE
        long seancesPlanifiees = seanceDAO.countSeancesPlanifieesParMois(mois, annee);
        if (seancesPlanifiees > 0) {
            throw new IllegalStateException("RG-06 : " + seancesPlanifiees +
                    " séance(s) encore en statut PLANIFIEE ce mois. Impossible de générer le rapport.");
        }

        // Récupérer les séances REALISEES du mois pour ce professeur
        List<SeancePlanifiee> seances = seanceDAO.findByProfesseurAndMois(professeurId, mois, annee);
        List<SeancePlanifiee> seancesRealisees = seances.stream()
                .filter(s -> s.getStatut() == StatutSeance.REALISEE)
                .toList();

        // Calculer les heures effectuées (RG-04 : uniquement les séances REALISEES avec pointage DEBUT)
        double totalMinutes = 0;
        for (SeancePlanifiee seance : seancesRealisees) {
            boolean hasDebut = pointageDAO.findBySeanceAndType(seance.getId(), TypePointage.DEBUT).isPresent();
            if (hasDebut && seance.getDureeMinutes() != null) {
                totalMinutes += seance.getDureeMinutes();
            }
        }

        // Arrondir au quart d'heure supérieur
        double totalHeures = Math.ceil(totalMinutes / 15.0) * 0.25;

        // Calculer le montant XOF
        double tauxHoraire = professeur.getTauxHoraireXOF() != null ? professeur.getTauxHoraireXOF() : 0;
        double montant = totalHeures * tauxHoraire;

        // Créer ou mettre à jour le rapport
        Optional<RapportMensuel> existing = rapportDAO.findByProfesseurAndPeriode(professeurId, mois, annee);
        RapportMensuel rapport = existing.orElse(new RapportMensuel());
        rapport.setProfesseur(professeur);
        rapport.setMois(mois);
        rapport.setAnnee(annee);
        rapport.setHeuresRealisees(totalHeures);
        rapport.setMontantXOF(montant);
        rapport.setStatut(StatutRapport.EN_ATTENTE);

        if (existing.isPresent()) {
            rapportDAO.update(rapport);
        } else {
            rapportDAO.save(rapport);
        }

        System.out.printf("📊 Rapport généré : %s | %02d/%d | %.2f h | %.0f XOF%n",
                professeur.getNomComplet(), mois, annee, totalHeures, montant);
        return rapport;
    }

    /**
     * Valide un rapport — réservé au rôle SCOLARITE ou ADMIN.
     */
    public RapportMensuel validerRapport(Long rapportId) {
        // Contrôle d'accès côté service (ne jamais se fier uniquement à l'UI)
        if (!(sn.epf.pointage.config.SessionContext.getInstance().isScolarite()
                || sn.epf.pointage.config.SessionContext.getInstance().isAdmin())) {
            throw new SecurityException("Accès refusé : seuls ADMIN/SCOLARITÉ peuvent valider un rapport.");
        }

        RapportMensuel rapport = rapportDAO.findById(rapportId)
                .orElseThrow(() -> new IllegalArgumentException("Rapport introuvable : " + rapportId));

        rapport.setStatut(StatutRapport.VALIDE);
        rapportDAO.update(rapport);

        System.out.println("✅ Rapport validé : " + rapportId);
        return rapport;
    }

    /**
     * Marque un rapport comme PAYE.
     */
    public RapportMensuel marquerPaye(Long rapportId) {
        if (!sn.epf.pointage.config.SessionContext.getInstance().isAdmin()) {
            throw new SecurityException("Accès refusé : seul ADMIN peut marquer un rapport comme PAYE.");
        }

        RapportMensuel rapport = rapportDAO.findById(rapportId)
                .orElseThrow(() -> new IllegalArgumentException("Rapport introuvable : " + rapportId));

        if (rapport.getStatut() != StatutRapport.VALIDE) {
            throw new IllegalStateException("Le rapport doit être VALIDE avant d'être marqué PAYE.");
        }

        rapport.setStatut(StatutRapport.PAYE);
        rapportDAO.update(rapport);
        return rapport;
    }

    /**
     * Génère un résumé PDF du rapport (contenu textuel formaté).
     * À coupler avec JasperReports pour un vrai PDF.
     */
    public String exporterRapportTexte(Long rapportId) {
        RapportMensuel rapport = rapportDAO.findById(rapportId)
                .orElseThrow(() -> new IllegalArgumentException("Rapport introuvable : " + rapportId));

        Professeur prof = rapport.getProfesseur();
        List<SeancePlanifiee> seances = seanceDAO.findByProfesseurAndMois(
                prof.getId(), rapport.getMois(), rapport.getAnnee());

        StringBuilder sb = new StringBuilder();
        sb.append("====================================================\n");
        sb.append("           EPF AFRICA — RAPPORT MENSUEL\n");
        sb.append("====================================================\n");
        sb.append(String.format("Professeur : %s (%s)\n", prof.getNomComplet(), prof.getMatricule()));
        sb.append(String.format("Contrat    : %s\n", prof.getTypeContrat()));
        sb.append(String.format("Période    : %02d/%d\n", rapport.getMois(), rapport.getAnnee()));
        sb.append("----------------------------------------------------\n");
        sb.append(String.format("%-20s %-10s %-10s\n", "Date/Heure", "Cours", "Durée (h)"));
        sb.append("----------------------------------------------------\n");

        for (SeancePlanifiee seance : seances) {
            if (seance.getStatut() == StatutSeance.REALISEE) {
                boolean hasDebut = pointageDAO.findBySeanceAndType(seance.getId(), TypePointage.DEBUT).isPresent();
                if (hasDebut) {
                    double dureeH = seance.getDureeMinutes() != null ? seance.getDureeMinutes() / 60.0 : 0;
                    sb.append(String.format("%-20s %-10s %.2f h\n",
                            seance.getDateHeure().toString().replace("T", " "),
                            seance.getAssignation() != null ? seance.getAssignation().getCours().getCode() : "N/A",
                            dureeH));
                }
            }
        }

        sb.append("----------------------------------------------------\n");
        sb.append(String.format("Total heures réalisées : %.2f h\n", rapport.getHeuresRealisees()));
        sb.append(String.format("Taux horaire           : %.0f XOF/h\n", prof.getTauxHoraireXOF() != null ? prof.getTauxHoraireXOF() : 0));
        sb.append(String.format("MONTANT TOTAL          : %.0f XOF\n", rapport.getMontantXOF()));
        sb.append(String.format("Statut                 : %s\n", rapport.getStatut()));
        sb.append("====================================================\n");

        return sb.toString();
    }

    // Accesseurs pour l'UI
    public List<RapportMensuel> getRapportsNonPayes() { return rapportDAO.findRapportsNonPayes(); }
    public List<RapportMensuel> getRapportsByProfesseur(Long profId) { return rapportDAO.findByProfesseur(profId); }
}
