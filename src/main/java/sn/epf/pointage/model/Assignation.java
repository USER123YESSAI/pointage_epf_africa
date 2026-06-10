package sn.epf.pointage.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assignations")
public class Assignation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professeur_id", nullable = false)
    private Professeur professeur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cours_id", nullable = false)
    private Cours cours;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salle_id")
    private Salle salle;

    @Column(name = "annee_academique", length = 20)
    private String anneeAcademique;

    @Column(name = "heures_prevues")
    private Integer heuresPrevues;

    @OneToMany(mappedBy = "assignation", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<SeancePlanifiee> seances = new ArrayList<>();

    @OneToMany(mappedBy = "assignation", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<PeriodiciteCours> periodicites = new ArrayList<>();

    public Assignation() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Professeur getProfesseur() { return professeur; }
    public void setProfesseur(Professeur professeur) { this.professeur = professeur; }
    public Cours getCours() { return cours; }
    public void setCours(Cours cours) { this.cours = cours; }
    public Salle getSalle() { return salle; }
    public void setSalle(Salle salle) { this.salle = salle; }
    public String getAnneeAcademique() { return anneeAcademique; }
    public void setAnneeAcademique(String anneeAcademique) { this.anneeAcademique = anneeAcademique; }
    public Integer getHeuresPrevues() { return heuresPrevues; }
    public void setHeuresPrevues(Integer heuresPrevues) { this.heuresPrevues = heuresPrevues; }
    public List<SeancePlanifiee> getSeances() { return seances; }
    public List<PeriodiciteCours> getPeriodicites() { return periodicites; }
}
