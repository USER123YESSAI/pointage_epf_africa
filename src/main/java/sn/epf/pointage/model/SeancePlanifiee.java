package sn.epf.pointage.model;

import jakarta.persistence.*;
import sn.epf.pointage.model.enums.StatutSeance;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "seances_planifiees")
public class SeancePlanifiee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignation_id", nullable = false)
    private Assignation assignation;

    @Column(name = "date_heure", nullable = false)
    private LocalDateTime dateHeure;

    @Column(name = "duree_minutes")
    private Integer dureeMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutSeance statut = StatutSeance.PLANIFIEE;

    @Column(name = "observations", length = 500)
    private String observations;

    @OneToMany(mappedBy = "seance", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Pointage> pointages = new ArrayList<>();

    public SeancePlanifiee() {}

    public SeancePlanifiee(Assignation assignation, LocalDateTime dateHeure, Integer dureeMinutes) {
        this.assignation = assignation;
        this.dateHeure = dateHeure;
        this.dureeMinutes = dureeMinutes;
        this.statut = StatutSeance.PLANIFIEE;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Assignation getAssignation() { return assignation; }
    public void setAssignation(Assignation assignation) { this.assignation = assignation; }
    public LocalDateTime getDateHeure() { return dateHeure; }
    public void setDateHeure(LocalDateTime dateHeure) { this.dateHeure = dateHeure; }
    public Integer getDureeMinutes() { return dureeMinutes; }
    public void setDureeMinutes(Integer dureeMinutes) { this.dureeMinutes = dureeMinutes; }
    public StatutSeance getStatut() { return statut; }
    public void setStatut(StatutSeance statut) { this.statut = statut; }
    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }
    public List<Pointage> getPointages() { return pointages; }
}
