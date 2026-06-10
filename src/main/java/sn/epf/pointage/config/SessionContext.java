package sn.epf.pointage.config;

import sn.epf.pointage.model.Utilisateur;
import sn.epf.pointage.model.enums.RoleUtilisateur;

import java.time.LocalDateTime;

/**
 * Singleton qui maintient la session de l'utilisateur connecté.
 * Accessible depuis tous les contrôleurs JavaFX.
 */
public class SessionContext {

    private static SessionContext instance;

    private Utilisateur utilisateurConnecte;
    private LocalDateTime derniereActivite;
    private static final int TIMEOUT_MINUTES = 30;

    private SessionContext() {}

    public static synchronized SessionContext getInstance() {
        if (instance == null) {
            instance = new SessionContext();
        }
        return instance;
    }

    public void connecter(Utilisateur utilisateur) {
        this.utilisateurConnecte = utilisateur;
        this.derniereActivite = LocalDateTime.now();
    }

    public void deconnecter() {
        this.utilisateurConnecte = null;
        this.derniereActivite = null;
    }

    public boolean estConnecte() {
        if (utilisateurConnecte == null) return false;
        if (estSessionExpiree()) {
            deconnecter();
            return false;
        }
        return true;
    }

    public boolean estSessionExpiree() {
        if (derniereActivite == null) return true;
        return LocalDateTime.now().isAfter(derniereActivite.plusMinutes(TIMEOUT_MINUTES));
    }

    public void rafraichirActivite() {
        this.derniereActivite = LocalDateTime.now();
    }

    public Utilisateur getUtilisateurConnecte() { return utilisateurConnecte; }

    public RoleUtilisateur getRole() {
        return utilisateurConnecte != null ? utilisateurConnecte.getRole() : null;
    }

    public boolean isAdmin() { return RoleUtilisateur.ADMIN.equals(getRole()); }
    public boolean isScolarite() { return RoleUtilisateur.SCOLARITE.equals(getRole()); }
    public boolean isProfesseur() { return RoleUtilisateur.PROFESSEUR.equals(getRole()); }

    public boolean peutGererUtilisateurs() { return isAdmin(); }
    public boolean peutEnrolerProfesseurs() { return isAdmin() || isScolarite(); }
    public boolean peutValiderRapports() { return isAdmin() || isScolarite(); }
    public boolean peutPointer() { return isProfesseur(); }
}
