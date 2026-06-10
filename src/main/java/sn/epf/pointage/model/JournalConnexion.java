package sn.epf.pointage.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "journal_connexions")
public class JournalConnexion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "login", length = 100) private String login;
    @Column(name = "action", length = 50) private String action; // CONNEXION / DECONNEXION
    @Column(name = "horodatage") private LocalDateTime horodatage;
    @Column(name = "adresse_ip", length = 50) private String adresseIp;
    @Column(name = "succes") private Boolean succes;

    public JournalConnexion() {}
    public JournalConnexion(String login, String action, boolean succes) {
        this.login=login; this.action=action; this.succes=succes; this.horodatage=LocalDateTime.now();
    }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getLogin() { return login; } public void setLogin(String l) { this.login = l; }
    public String getAction() { return action; } public void setAction(String a) { this.action = a; }
    public LocalDateTime getHorodatage() { return horodatage; } public void setHorodatage(LocalDateTime h) { this.horodatage = h; }
    public String getAdresseIp() { return adresseIp; } public void setAdresseIp(String ip) { this.adresseIp = ip; }
    public Boolean getSucces() { return succes; } public void setSucces(Boolean s) { this.succes = s; }
}
