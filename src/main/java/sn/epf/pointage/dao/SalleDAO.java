package sn.epf.pointage.dao;

import java.util.List;

import org.hibernate.Session;

import sn.epf.pointage.model.Salle;

public class SalleDAO extends AbstractDAO<Salle, Long> {

    public SalleDAO() {
        super(Salle.class);
    }

    public List<Salle> findAllSalles() {
        try (Session session = openSession()) {
            return session.createQuery("FROM Salle s ORDER BY s.nom", Salle.class).list();
        }
    }

    @Override
    public List<Salle> findAll(int offset, int limit) {
        return super.findAll(offset, limit);
    }
}

