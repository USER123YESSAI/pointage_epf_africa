package sn.epf.pointage.dao;
import org.hibernate.Session;
import sn.epf.pointage.model.Assignation;
import java.util.List;

public class AssignationDAO extends AbstractDAO<Assignation, Long> {
    public AssignationDAO() { super(Assignation.class); }
    public List<Assignation> findByProfesseur(Long professeurId) {
        try (Session session = openSession()) {
            return session.createQuery("FROM Assignation a WHERE a.professeur.id = :profId", Assignation.class)
                .setParameter("profId", professeurId).list();
        }
    }

    public List<Assignation> findAllWithAssociations() {
        try (Session session = openSession()) {
            return session.createQuery(
                    "SELECT a FROM Assignation a " +
                    "JOIN FETCH a.professeur p " +
                    "JOIN FETCH a.cours c " +
                    "LEFT JOIN FETCH a.salle s",
                    Assignation.class).list();
        }
    }
}
