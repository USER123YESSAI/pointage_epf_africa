package sn.epf.pointage.dao;
import java.util.List;
import java.util.Optional;

import org.hibernate.Session;

import sn.epf.pointage.model.RapportMensuel;
import sn.epf.pointage.model.enums.StatutRapport;

public class RapportDAO extends AbstractDAO<RapportMensuel, Long> {
    public RapportDAO() { super(RapportMensuel.class); }

    public Optional<RapportMensuel> findByProfesseurAndPeriode(Long professeurId, int mois, int annee) {
        try (Session session = openSession()) {
            return session.createQuery(
                "FROM RapportMensuel r WHERE r.professeur.id = :profId AND r.mois = :mois AND r.annee = :annee",
                RapportMensuel.class)
                .setParameter("profId", professeurId).setParameter("mois", mois).setParameter("annee", annee)
                .uniqueResultOptional();
        }
    }

    public Optional<RapportMensuel> findByIdWithProfesseur(Long id) {
        try (Session session = openSession()) {
            return session.createQuery(
                    "SELECT r FROM RapportMensuel r JOIN FETCH r.professeur WHERE r.id = :id",
                    RapportMensuel.class)
                    .setParameter("id", id)
                    .uniqueResultOptional();
        }
    }

    public List<RapportMensuel> findRapportsNonPayes() {
        try (Session session = openSession()) {
            return session.createQuery(
                "FROM RapportMensuel r WHERE r.statut != :statut ORDER BY r.annee DESC, r.mois DESC",
                RapportMensuel.class).setParameter("statut", StatutRapport.PAYE).list();
        }
    }

    public List<RapportMensuel> findByProfesseur(Long professeurId) {
        try (Session session = openSession()) {
            return session.createQuery(
                "FROM RapportMensuel r WHERE r.professeur.id = :profId ORDER BY r.annee DESC, r.mois DESC",
                RapportMensuel.class).setParameter("profId", professeurId).list();
        }
    }
}
