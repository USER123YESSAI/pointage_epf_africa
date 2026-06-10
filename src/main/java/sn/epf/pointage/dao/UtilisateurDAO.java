package sn.epf.pointage.dao;
import org.hibernate.Session;
import sn.epf.pointage.model.Utilisateur;
import java.util.Optional;

public class UtilisateurDAO extends AbstractDAO<Utilisateur, Long> {
    public UtilisateurDAO() { super(Utilisateur.class); }

    public Optional<Utilisateur> findByLogin(String login) {
        try (Session session = openSession()) {
            return session.createQuery(
                    "FROM Utilisateur u LEFT JOIN FETCH u.professeurLie WHERE u.login = :login AND u.actif = true",
                    Utilisateur.class)
                .setParameter("login", login).uniqueResultOptional();
        }
    }
}
