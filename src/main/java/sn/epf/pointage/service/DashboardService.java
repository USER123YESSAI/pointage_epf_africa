package sn.epf.pointage.service;

import sn.epf.pointage.dao.*;
import sn.epf.pointage.model.SeancePlanifiee;
import sn.epf.pointage.model.enums.StatutSeance;
import sn.epf.pointage.model.enums.TypeContrat;
import sn.epf.pointage.model.enums.TypePointage;

import java.time.LocalDate;
import java.util.List;

public class DashboardService {

    private final SeanceDAO seanceDAO = new SeanceDAO();
    private final ProfesseurDAO professeurDAO = new ProfesseurDAO();
    private final PointageDAO pointageDAO = new PointageDAO();

    public long getSeancesDuJour() {
        return seanceDAO.findSeancesDuJour().size();
    }

    public long getProfesseursPresentsAujourdhui() {
        List<SeancePlanifiee> seances = seanceDAO.findSeancesDuJour();
        return seances.stream()
                .filter(s -> s.getStatut() == StatutSeance.REALISEE)
                .count();
    }

    public long getProfesseursAbsentsAujourdhui() {
        List<SeancePlanifiee> seances = seanceDAO.findSeancesDuJour();
        long total = seances.size();
        long presents = seances.stream()
                .filter(s -> s.getStatut() == StatutSeance.REALISEE)
                .count();
        return Math.max(0, total - presents);
    }

    public double getTauxPresenceAujourdhui() {
        long total = getSeancesDuJour();
        if (total == 0) return 0.0;
        return (double) getProfesseursPresentsAujourdhui() / total * 100;
    }

    public long getNombreVacataires() {
        return professeurDAO.countByTypeContrat(TypeContrat.VACATAIRE);
    }

    public long getNombrePermanents() {
        return professeurDAO.countByTypeContrat(TypeContrat.PERMANENT);
    }

    public long getTotalProfesseurs() {
        return professeurDAO.count();
    }

    /** Retourne les séances du jour sans pointage (alertes) */
    public List<SeancePlanifiee> getAlertesDuJour() {
        return seanceDAO.findSeancesSansPointage();
    }
}
