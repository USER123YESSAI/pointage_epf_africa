package sn.epf.pointage.service;

import sn.epf.pointage.dao.PointageDAO;
import sn.epf.pointage.dao.SeanceDAO;
import sn.epf.pointage.dao.ProfesseurDAO;
import sn.epf.pointage.model.*;
import sn.epf.pointage.model.enums.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Service de pointage — applique les règles RG-01 à RG-05.
 * Ne dépend jamais de l'interface JavaFX.
 */
public class PointageService {

    private final PointageDAO pointageDAO = new PointageDAO();
    private final SeanceDAO seanceDAO = new SeanceDAO();
    private final ProfesseurDAO professeurDAO = new ProfesseurDAO();

    // Constantes RG
    private static final int FENETRE_AVANT_MINUTES = 15;  // RG-01 : on peut pointer 15 min avant
    private static final int FENETRE_APRES_MINUTES = 5;   // RG-01 : fenêtre ferme 5 min après
    private static final int SEUIL_RETARD_MINUTES = 5;    // RG-03 : retard si > 5 min

    /**
     * Enregistre un pointage en appliquant toutes les règles métier.
     *
     * @return ResultatPointage : SUCCES, EN_RETARD, TROP_TOT, PROF_INACTIF, DEJA_POINTE
     */
    public ResultatPointage pointer(Long seanceId, Long professeurId, TypePointage typePointage) {

        // 1. Récupérer les entités
        SeancePlanifiee seance = seanceDAO.findById(seanceId)
                .orElseThrow(() -> new IllegalArgumentException("Séance introuvable : " + seanceId));

        Professeur professeur = professeurDAO.findById(professeurId)
                .orElseThrow(() -> new IllegalArgumentException("Professeur introuvable : " + professeurId));

        LocalDateTime maintenant = LocalDateTime.now();
        LocalDateTime heureSeance = seance.getDateHeure();

        // 2. RG-05 : Refuser si le professeur est INACTIF
        if (!professeur.getActif()) {
            System.out.println("🚫 RG-05: Professeur inactif — pointage refusé.");
            return ResultatPointage.PROF_INACTIF;
        }

        // 3. Vérifier si déjà pointé pour ce type
        Optional<Pointage> existant = pointageDAO.findBySeanceAndType(seanceId, typePointage);
        if (existant.isPresent()) {
            System.out.println("⚠️ Pointage " + typePointage + " déjà enregistré pour cette séance.");
            return ResultatPointage.DEJA_POINTE;
        }

        // 4. Calculer l'écart en minutes
        long ecart = ChronoUnit.MINUTES.between(heureSeance, maintenant);
        // ecart < 0 = avant l'heure prévue, ecart > 0 = en retard

        // 5. RG-01 : Fenêtre de pointage [-15min, +5min] uniquement pour DEBUT
        if (typePointage == TypePointage.DEBUT) {
            if (ecart < -FENETRE_AVANT_MINUTES) {
                System.out.println("⏰ RG-01: Trop tôt pour pointer (" + Math.abs(ecart) + " min avant).");
                return ResultatPointage.TROP_TOT;
            }
        }

        // 6. Créer le pointage
        Pointage pointage = new Pointage(seance, professeur, typePointage);
        pointage.setHeurePointage(maintenant);
        pointage.setEcartMinutes((int) ecart);

        ResultatPointage resultat;

        // 7. RG-03 : Marquer EN_RETARD si écart > SEUIL_RETARD
        if (typePointage == TypePointage.DEBUT && ecart > SEUIL_RETARD_MINUTES) {
            pointage.setStatut(StatutPointage.EN_RETARD);
            pointage.setObservations("Retard de " + ecart + " minutes.");
            alerterScolarite(professeur, seance, ecart);
            resultat = ResultatPointage.EN_RETARD;
            System.out.println("⚠️ RG-03: Pointage EN_RETARD de " + ecart + " minutes.");
        } else {
            pointage.setStatut(StatutPointage.A_LHEURE);
            resultat = ResultatPointage.SUCCES;
            System.out.println("✅ Pointage SUCCES : " + professeur.getNomComplet() + " | " + typePointage);
        }

        // 8. Sauvegarder le pointage
        pointageDAO.save(pointage);

        // 9. RG-02 : Si DEBUT → mettre la séance à REALISEE
        if (typePointage == TypePointage.DEBUT) {
            seance.setStatut(StatutSeance.REALISEE);
            seanceDAO.update(seance);
            System.out.println("📗 RG-02: Séance " + seanceId + " marquée REALISEE.");
        }

        return resultat;
    }

    /**
     * Vérifie si un professeur peut pointer maintenant (pour affichage bouton actif/inactif).
     */
    public boolean peutPointer(SeancePlanifiee seance, Professeur professeur, TypePointage type) {
        if (!professeur.getActif()) return false;

        // Vérifier déjà pointé
        if (pointageDAO.findBySeanceAndType(seance.getId(), type).isPresent()) return false;

        if (type == TypePointage.DEBUT) {
            long ecart = ChronoUnit.MINUTES.between(seance.getDateHeure(), LocalDateTime.now());
            return ecart >= -FENETRE_AVANT_MINUTES;
        }

        // Pour FIN : vérifier que DEBUT est déjà fait
        return pointageDAO.findBySeanceAndType(seance.getId(), TypePointage.DEBUT).isPresent();
    }

    /**
     * Retourne l'état d'affichage (vert/orange/rouge) pour l'interface.
     */
    public String getCouleurStatut(SeancePlanifiee seance) {
        boolean hasDebut = pointageDAO.findBySeanceAndType(seance.getId(), TypePointage.DEBUT).isPresent();
        if (hasDebut) {
            Optional<Pointage> p = pointageDAO.findBySeanceAndType(seance.getId(), TypePointage.DEBUT);
            if (p.isPresent() && p.get().getStatut() == StatutPointage.EN_RETARD) return "orange";
            return "vert";
        }
        long ecart = ChronoUnit.MINUTES.between(seance.getDateHeure(), LocalDateTime.now());
        if (ecart > FENETRE_APRES_MINUTES) return "rouge"; // absent
        return "gris"; // pas encore l'heure
    }

    // ─── Méthode privée ───────────────────────────────────────────────────────

    private void alerterScolarite(Professeur professeur, SeancePlanifiee seance, long ecartMinutes) {
        // Notification console — en production, envoyer un email via JavaMail
        System.out.println("📧 ALERTE SCOLARITÉ : " + professeur.getNomComplet()
                + " est en retard de " + ecartMinutes + " minutes"
                + " pour la séance du " + seance.getDateHeure());
    }
}
