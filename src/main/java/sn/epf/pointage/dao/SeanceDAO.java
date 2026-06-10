package sn.epf.pointage.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.Session;

import sn.epf.pointage.model.SeancePlanifiee;
import sn.epf.pointage.model.enums.StatutSeance;

public class SeanceDAO extends AbstractDAO<SeancePlanifiee, Long> {
    public SeanceDAO() { super(SeancePlanifiee.class); }

    public List<SeancePlanifiee> findSeancesDuJour() {
        try (Session session = openSession()) {
            LocalDateTime debut = LocalDate.now().atStartOfDay();
            LocalDateTime fin = debut.plusDays(1);
            return session.createQuery(
                "SELECT DISTINCT s FROM SeancePlanifiee s " +
                "JOIN FETCH s.assignation a " +
                "JOIN FETCH a.cours c " +
                "LEFT JOIN FETCH a.salle sl " +
                "LEFT JOIN FETCH a.professeur p " +
                "WHERE s.dateHeure >= :debut AND s.dateHeure < :fin " +
                "ORDER BY s.dateHeure",
                SeancePlanifiee.class)
                .setParameter("debut", debut).setParameter("fin", fin).list();
        }
    }

    public List<SeancePlanifiee> findSeancesSansPointage() {
        try (Session session = openSession()) {
            LocalDateTime debut = LocalDate.now().atStartOfDay();
            LocalDateTime fin = debut.plusDays(1);
            return session.createQuery(
                "SELECT DISTINCT s FROM SeancePlanifiee s " +
                "JOIN FETCH s.assignation a " +
                "JOIN FETCH a.cours c " +
                "LEFT JOIN FETCH a.salle sl " +
                "LEFT JOIN FETCH a.professeur p " +
                "WHERE s.dateHeure >= :debut AND s.dateHeure < :fin " +
                "AND s.statut = :statut AND SIZE(s.pointages) = 0",
                SeancePlanifiee.class)
                .setParameter("debut", debut).setParameter("fin", fin)
                .setParameter("statut", StatutSeance.PLANIFIEE).list();
        }
    }

    public List<SeancePlanifiee> findByProfesseurAndMois(Long professeurId, int mois, int annee) {
        try (Session session = openSession()) {
            LocalDateTime debut = LocalDateTime.of(annee, mois, 1, 0, 0);
            LocalDateTime fin = debut.plusMonths(1);
            return session.createQuery(
                "SELECT DISTINCT s FROM SeancePlanifiee s " +
                "JOIN FETCH s.assignation a " +
                "JOIN FETCH a.cours c " +
                "LEFT JOIN FETCH a.salle sl " +
                "LEFT JOIN FETCH a.professeur p " +
                "WHERE a.professeur.id = :profId " +
                "AND s.dateHeure >= :debut AND s.dateHeure < :fin " +
                "ORDER BY s.dateHeure",
                SeancePlanifiee.class)
                .setParameter("profId", professeurId).setParameter("debut", debut).setParameter("fin", fin).list();
        }
    }

    public List<SeancePlanifiee> findSeancesDuJourParProfesseur(Long professeurId) {
        try (Session session = openSession()) {
            LocalDateTime debut = LocalDate.now().atStartOfDay();
            LocalDateTime fin = debut.plusDays(1);
            return session.createQuery(
                "SELECT DISTINCT s FROM SeancePlanifiee s " +
                "JOIN FETCH s.assignation a " +
                "JOIN FETCH a.cours c " +
                "LEFT JOIN FETCH a.salle sl " +
                "LEFT JOIN FETCH a.professeur p " +
                "WHERE a.professeur.id = :profId " +
                "AND s.dateHeure >= :debut AND s.dateHeure < :fin " +
                "ORDER BY s.dateHeure",
                SeancePlanifiee.class)
                .setParameter("profId", professeurId).setParameter("debut", debut).setParameter("fin", fin).list();
        }
    }

    public long countSeancesPlanifieesParMois(int mois, int annee) {
        try (Session session = openSession()) {
            LocalDateTime debut = LocalDateTime.of(annee, mois, 1, 0, 0);
            LocalDateTime fin = debut.plusMonths(1);
            return session.createQuery(
                "SELECT COUNT(s) FROM SeancePlanifiee s WHERE s.statut = :statut AND s.dateHeure >= :debut AND s.dateHeure < :fin",
                Long.class)
                .setParameter("statut", StatutSeance.PLANIFIEE).setParameter("debut", debut).setParameter("fin", fin)
                .uniqueResult();
        }
    }

    /**
     * Séances dans une période donnée (avec fetch des associations utiles).
     */
    public List<SeancePlanifiee> findByPeriode(LocalDateTime debut, LocalDateTime fin) {
        try (Session session = openSession()) {
            return session.createQuery(
                    "SELECT DISTINCT s FROM SeancePlanifiee s " +
                    "JOIN FETCH s.assignation a " +
                    "JOIN FETCH a.cours c " +
                    "LEFT JOIN FETCH a.salle sl " +
                    "LEFT JOIN FETCH a.professeur p " +
                    "WHERE s.dateHeure >= :debut AND s.dateHeure <= :fin " +
                    "ORDER BY s.dateHeure",
                    SeancePlanifiee.class)
                    .setParameter("debut", debut)
                    .setParameter("fin", fin)
                    .list();
        }
    }

    /**
     * Même que findByPeriode mais filtré par professeur.
     */
    public List<SeancePlanifiee> findByPeriodeEtProfesseur(LocalDateTime debut, LocalDateTime fin, Long professeurId) {
        try (Session session = openSession()) {
            return session.createQuery(
                    "SELECT s FROM SeancePlanifiee s " +
                    "JOIN FETCH s.assignation a " +
                    "JOIN FETCH a.cours c " +
                    "LEFT JOIN FETCH a.salle sl " +
                    "LEFT JOIN FETCH a.professeur p " +
                    "WHERE s.dateHeure >= :debut AND s.dateHeure <= :fin AND a.professeur.id = :profId " +
                    "ORDER BY s.dateHeure",
                    SeancePlanifiee.class)
                    .setParameter("debut", debut)
                    .setParameter("fin", fin)
                    .setParameter("profId", professeurId)
                    .list();
        }
    }
}
