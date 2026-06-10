package sn.epf.pointage.dao;

import java.util.List;

import org.hibernate.Session;

import sn.epf.pointage.model.Cours;

public class CoursDAO extends AbstractDAO<Cours, Long> {

    public CoursDAO() {
        super(Cours.class);
    }

    @Override
    public List<Cours> findAll() {
        // limiter à 100 comme le generic
        return super.findAll();
    }

    public List<Cours> findAllCours() {
        try (Session session = openSession()) {
            return session.createQuery("FROM Cours c ORDER BY c.code", Cours.class).list();
        }
    }

    @Override
    public List<Cours> findAll(int offset, int limit) {
        return super.findAll(offset, limit);
    }
}

