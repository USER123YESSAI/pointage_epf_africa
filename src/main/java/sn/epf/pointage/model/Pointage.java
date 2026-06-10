package sn.epf.pointage.model;

import jakarta.persistence.*;
import sn.epf.pointage.model.enums.StatutPointage;
import sn.epf.pointage.model.enums.TypePointage;

import java.time.LocalDateTime;

@Entity
@Table(name = "pointages")
public class Pointage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seance_id", nullable = false)
    private SeancePlanifiee seance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professeur_id", nullable = false)
    private Professeur professeur;

    @Column(name = "heure_pointage", nullable = false)
    private LocalDateTime heurePointage;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_pointage", nullable = false)
    private TypePointage typePointage;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut")
    private StatutPointage statut;

    @Column(name = "observations", length = 500)
    private String observations;

    @Column(name = "ecart_minutes")
    private Integer ecartMinutes; // écart par rapport à l'heure prévue

    public Pointage() {}

    public Pointage(SeancePlanifiee seance, Professeur professeur, TypePointage type) {
        this.seance = seance;
        this.professeur = professeur;
        this.typePointage = type;
        this.heurePointage = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public SeancePlanifiee getSeance() { return seance; }
    public void setSeance(SeancePlanifiee seance) { this.seance = seance; }
    public Professeur getProfesseur() { return professeur; }
    public void setProfesseur(Professeur professeur) { this.professeur = professeur; }
    public LocalDateTime getHeurePointage() { return heurePointage; }
    public void setHeurePointage(LocalDateTime heurePointage) { this.heurePointage = heurePointage; }
    public TypePointage getTypePointage() { return typePointage; }
    public void setTypePointage(TypePointage typePointage) { this.typePointage = typePointage; }
    public StatutPointage getStatut() { return statut; }
    public void setStatut(StatutPointage statut) { this.statut = statut; }
    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }
    public Integer getEcartMinutes() { return ecartMinutes; }
    public void setEcartMinutes(Integer ecartMinutes) { this.ecartMinutes = ecartMinutes; }
}
