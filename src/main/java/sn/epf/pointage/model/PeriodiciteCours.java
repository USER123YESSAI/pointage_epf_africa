package sn.epf.pointage.model;
import jakarta.persistence.*;
import sn.epf.pointage.model.enums.FrequenceCours;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "periodicite_cours")
public class PeriodiciteCours {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "assignation_id", nullable = false) private Assignation assignation;
    @Enumerated(EnumType.STRING) @Column(name = "jour_semaine") private DayOfWeek jourSemaine;
    @Column(name = "heure_debut") private LocalTime heureDebut;
    @Column(name = "heure_fin") private LocalTime heureFin;
    @Enumerated(EnumType.STRING) @Column(name = "frequence") private FrequenceCours frequence = FrequenceCours.HEBDO;
    @Column(name = "duree_minutes") private Integer dureeMinutes;

    public PeriodiciteCours() {}
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Assignation getAssignation() { return assignation; } public void setAssignation(Assignation a) { this.assignation = a; }
    public DayOfWeek getJourSemaine() { return jourSemaine; } public void setJourSemaine(DayOfWeek j) { this.jourSemaine = j; }
    public LocalTime getHeureDebut() { return heureDebut; } public void setHeureDebut(LocalTime h) { this.heureDebut = h; }
    public LocalTime getHeureFin() { return heureFin; } public void setHeureFin(LocalTime h) { this.heureFin = h; }
    public FrequenceCours getFrequence() { return frequence; } public void setFrequence(FrequenceCours f) { this.frequence = f; }
    public Integer getDureeMinutes() { return dureeMinutes; } public void setDureeMinutes(Integer d) { this.dureeMinutes = d; }
}
