package sn.epf.pointage.model;
import jakarta.persistence.*;
import sn.epf.pointage.model.enums.RoleUtilisateur;

@Entity
@Table(name = "utilisateurs")
public class Utilisateur {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "login", nullable = false, unique = true, length = 100) private String login;
    @Column(name = "mot_de_passe_hash", nullable = false, length = 200) private String motDePasseHash;
    @Enumerated(EnumType.STRING) @Column(name = "role", nullable = false) private RoleUtilisateur role;
    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "professeur_id") private Professeur professeurLie;
    @Column(name = "actif") private Boolean actif = true;

    public Utilisateur() {}
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getLogin() { return login; } public void setLogin(String l) { this.login = l; }
    public String getMotDePasseHash() { return motDePasseHash; } public void setMotDePasseHash(String m) { this.motDePasseHash = m; }
    public RoleUtilisateur getRole() { return role; } public void setRole(RoleUtilisateur r) { this.role = r; }
    public Professeur getProfesseurLie() { return professeurLie; } public void setProfesseurLie(Professeur p) { this.professeurLie = p; }
    public Boolean getActif() { return actif; } public void setActif(Boolean a) { this.actif = a; }
}
