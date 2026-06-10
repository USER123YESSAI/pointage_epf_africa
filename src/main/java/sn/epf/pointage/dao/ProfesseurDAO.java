package sn.epf.pointage.dao;

import org.hibernate.Session;
import sn.epf.pointage.model.Professeur;
import sn.epf.pointage.model.enums.TypeContrat;

import java.util.List;
import java.util.Optional;

public class ProfesseurDAO extends AbstractDAO<Professeur, Long> {

    public ProfesseurDAO() {
        super(Professeur.class);
    }

    public Optional<Professeur> findByMatricule(String matricule) {
        try (Session session = openSession()) {
            return session.createQuery(
                    "FROM Professeur p WHERE p.matricule = :matricule", Professeur.class)
                    .setParameter("matricule", matricule)
                    .uniqueResultOptional();
        }
    }

    public Optional<Professeur> findByEmail(String email) {
        try (Session session = openSession()) {
            return session.createQuery(
                    "FROM Professeur p WHERE LOWER(p.email) = LOWER(:email)", Professeur.class)
                    .setParameter("email", email)
                    .uniqueResultOptional();
        }
    }

    public List<Professeur> findByNom(String nom) {
        try (Session session = openSession()) {
            return session.createQuery(
                    "FROM Professeur p WHERE LOWER(p.nom) LIKE LOWER(:nom) OR LOWER(p.prenom) LIKE LOWER(:nom)",
                    Professeur.class)
                    .setParameter("nom", "%" + nom + "%")
                    .list();
        }
    }

    public List<Professeur> findByFiliere(String filiere) {
        try (Session session = openSession()) {
            return session.createQuery(
                    "FROM Professeur p WHERE p.filiere = :filiere", Professeur.class)
                    .setParameter("filiere", filiere)
                    .list();
        }
    }

    public List<Professeur> findByTypeContrat(TypeContrat typeContrat) {
        try (Session session = openSession()) {
            return session.createQuery(
                    "FROM Professeur p WHERE p.typeContrat = :type", Professeur.class)
                    .setParameter("type", typeContrat)
                    .list();
        }
    }

    public List<Professeur> findAllActifs() {
        try (Session session = openSession()) {
            return session.createQuery(
                    "FROM Professeur p WHERE p.actif = true ORDER BY p.nom, p.prenom", Professeur.class)
                    .list();
        }
    }

    public long countByTypeContrat(TypeContrat typeContrat) {
        try (Session session = openSession()) {
            return session.createQuery(
                    "SELECT COUNT(p) FROM Professeur p WHERE p.typeContrat = :type AND p.actif = true", Long.class)
                    .setParameter("type", typeContrat)
                    .uniqueResult();
        }
    }
}
