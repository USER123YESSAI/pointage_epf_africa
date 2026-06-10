package sn.epf.pointage.dao;
import org.hibernate.Session;
import sn.epf.pointage.model.Pointage;
import sn.epf.pointage.model.enums.TypePointage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class PointageDAO extends AbstractDAO<Pointage, Long> {
    public PointageDAO() { super(Pointage.class); }

    public Optional<Pointage> findBySeanceAndType(Long seanceId, TypePointage type) {
        try (Session session = openSession()) {
            return session.createQuery(
                "FROM Pointage p WHERE p.seance.id = :seanceId AND p.typePointage = :type", Pointage.class)
                .setParameter("seanceId", seanceId).setParameter("type", type).uniqueResultOptional();
        }
    }

    public List<Pointage> findByProfesseurAndMois(Long professeurId, int mois, int annee) {
        try (Session session = openSession()) {
            LocalDateTime debut = LocalDateTime.of(annee, mois, 1, 0, 0);
            LocalDateTime fin = debut.plusMonths(1);
            return session.createQuery(
                "FROM Pointage p WHERE p.professeur.id = :profId AND p.heurePointage >= :debut AND p.heurePointage < :fin",
                Pointage.class)
                .setParameter("profId", professeurId).setParameter("debut", debut).setParameter("fin", fin).list();
        }
    }

    public long countBySeance(Long seanceId) {
        try (Session session = openSession()) {
            return session.createQuery("SELECT COUNT(p) FROM Pointage p WHERE p.seance.id = :id", Long.class)
                .setParameter("id", seanceId).uniqueResult();
        }
    }

    public long countByProfesseurAndMois(Long professeurId, int mois, int annee) {
        try (Session session = openSession()) {
            LocalDateTime debut = LocalDateTime.of(annee, mois, 1, 0, 0);
            LocalDateTime fin = debut.plusMonths(1);
            return session.createQuery(
                "SELECT COUNT(p) FROM Pointage p WHERE p.professeur.id = :profId AND p.heurePointage >= :debut AND p.heurePointage < :fin",
                Long.class)
                .setParameter("profId", professeurId).setParameter("debut", debut).setParameter("fin", fin).uniqueResult();
        }
    }
}
