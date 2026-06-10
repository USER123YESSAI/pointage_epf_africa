package sn.epf.pointage.model;
import jakarta.persistence.*;
import sn.epf.pointage.model.enums.StatutRapport;

@Entity
@Table(name = "rapports_mensuels")
public class RapportMensuel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "professeur_id", nullable = false) private Professeur professeur;
    @Column(name = "mois", nullable = false) private Integer mois;
    @Column(name = "annee", nullable = false) private Integer annee;
    @Column(name = "heures_realisees") private Double heuresRealisees;
    @Column(name = "montant_xof") private Double montantXOF;
    @Enumerated(EnumType.STRING) @Column(name = "statut") private StatutRapport statut = StatutRapport.EN_ATTENTE;
    @Column(name = "observations", length = 500) private String observations;

    public RapportMensuel() {}
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Professeur getProfesseur() { return professeur; } public void setProfesseur(Professeur p) { this.professeur = p; }
    public Integer getMois() { return mois; } public void setMois(Integer m) { this.mois = m; }
    public Integer getAnnee() { return annee; } public void setAnnee(Integer a) { this.annee = a; }
    public Double getHeuresRealisees() { return heuresRealisees; } public void setHeuresRealisees(Double h) { this.heuresRealisees = h; }
    public Double getMontantXOF() { return montantXOF; } public void setMontantXOF(Double m) { this.montantXOF = m; }
    public StatutRapport getStatut() { return statut; } public void setStatut(StatutRapport s) { this.statut = s; }
    public String getObservations() { return observations; } public void setObservations(String o) { this.observations = o; }
}
