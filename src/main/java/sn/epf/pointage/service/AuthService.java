package sn.epf.pointage.service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

import org.mindrot.jbcrypt.BCrypt;

import sn.epf.pointage.config.SessionContext;
import sn.epf.pointage.dao.AbstractDAO;
import sn.epf.pointage.dao.UtilisateurDAO;
import sn.epf.pointage.model.JournalConnexion;
import sn.epf.pointage.model.Utilisateur;
import sn.epf.pointage.model.enums.RoleUtilisateur;

public class AuthService {

    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private final JournalConnexionDAO journalDAO = new JournalConnexionDAO();

    /**
     * Authentifie un utilisateur.
     * @return true si succès, false sinon
     */
    public boolean authentifier(String login, String motDePasse) {
        Optional<Utilisateur> optUser = utilisateurDAO.findByLogin(login);

        if (optUser.isEmpty()) {
            journalDAO.save(withIp(new JournalConnexion(login, "CONNEXION", false)));
            return false;
        }

        Utilisateur utilisateur = optUser.get();

        if (!utilisateur.getActif()) {
            journalDAO.save(withIp(new JournalConnexion(login, "CONNEXION_REFUSEE_COMPTE_INACTIF", false)));
            return false;
        }

        boolean motDePasseValide = BCrypt.checkpw(motDePasse, utilisateur.getMotDePasseHash());

        if (motDePasseValide) {
            SessionContext.getInstance().connecter(utilisateur);
            journalDAO.save(withIp(new JournalConnexion(login, "CONNEXION", true)));
            System.out.println("✅ Connexion réussie : " + login + " [" + utilisateur.getRole() + "]");
            return true;
        } else {
            journalDAO.save(withIp(new JournalConnexion(login, "CONNEXION", false)));
            return false;
        }
    }

    public void deconnecter() {
        SessionContext ctx = SessionContext.getInstance();
        if (ctx.getUtilisateurConnecte() != null) {
            journalDAO.save(withIp(new JournalConnexion(ctx.getUtilisateurConnecte().getLogin(), "DECONNEXION", true)));
        }
        ctx.deconnecter();
    }

    /**
     * Crée un compte admin par défaut si aucun compte n'existe.
     */
  public void initialiserCompteAdmin() {

    // ── Compte ADMIN ──────────────────────────────────────────────────
    Optional<Utilisateur> admin = utilisateurDAO.findByLogin("admin@epf.sn");
    if (admin.isEmpty()) {
        Utilisateur u = new Utilisateur();
        u.setLogin("admin@epf.sn");
        u.setMotDePasseHash(BCrypt.hashpw("Admin@2024", BCrypt.gensalt()));
        u.setRole(RoleUtilisateur.ADMIN);
        u.setActif(true);
        utilisateurDAO.save(u);
        System.out.println("🔑 Compte ADMIN créé : admin@epf.sn / Admin@2024");
    }

    // ── Compte SCOLARITE (NOUVEAU) ────────────────────────────────────
    Optional<Utilisateur> scolarite = utilisateurDAO.findByLogin("scolarite@epf.sn");
    if (scolarite.isEmpty()) {
        Utilisateur u = new Utilisateur();
        u.setLogin("scolarite@epf.sn");
        u.setMotDePasseHash(BCrypt.hashpw("Scolarite@2024", BCrypt.gensalt()));
        u.setRole(RoleUtilisateur.SCOLARITE);
        u.setActif(true);
        utilisateurDAO.save(u);
        System.out.println("🔑 Compte SCOLARITE créé : scolarite@epf.sn / Scolarite@2024");
    }
}    private JournalConnexion withIp(JournalConnexion jc) {
        try {
            // IP locale (meilleur effort). En démo bureau, c'est suffisant pour respecter le champ.
            String ip = InetAddress.getLocalHost().getHostAddress();
            jc.setAdresseIp(ip);
        } catch (UnknownHostException e) {
            jc.setAdresseIp("unknown");
        }
        return jc;
    }

    // DAO interne pour le journal
    private static class JournalConnexionDAO extends AbstractDAO<JournalConnexion, Long> {
        public JournalConnexionDAO() { super(JournalConnexion.class); }
    }
}
