package sn.epf.pointage.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cours")
public class Cours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;

    @Column(name = "intitule", nullable = false, length = 200)
    private String intitule;

    @Column(name = "volume_horaire_total")
    private Integer volumeHoraireTotal;

    @Column(name = "niveau_etude", length = 50)
    private String niveauEtude;

    @Column(name = "filiere", length = 100)
    private String filiere;

    @Column(name = "semestre", length = 20)
    private String semestre;

    @Column(name = "description", length = 500)
    private String description;

    @OneToMany(mappedBy = "cours", fetch = FetchType.LAZY)
    private List<Assignation> assignations = new ArrayList<>();

    public Cours() {}

    public Cours(String code, String intitule, String filiere, String niveauEtude) {
        this.code = code;
        this.intitule = intitule;
        this.filiere = filiere;
        this.niveauEtude = niveauEtude;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getIntitule() { return intitule; }
    public void setIntitule(String intitule) { this.intitule = intitule; }
    public Integer getVolumeHoraireTotal() { return volumeHoraireTotal; }
    public void setVolumeHoraireTotal(Integer volumeHoraireTotal) { this.volumeHoraireTotal = volumeHoraireTotal; }
    public String getNiveauEtude() { return niveauEtude; }
    public void setNiveauEtude(String niveauEtude) { this.niveauEtude = niveauEtude; }
    public String getFiliere() { return filiere; }
    public void setFiliere(String filiere) { this.filiere = filiere; }
    public String getSemestre() { return semestre; }
    public void setSemestre(String semestre) { this.semestre = semestre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<Assignation> getAssignations() { return assignations; }

    @Override
    public String toString() { return code + " - " + intitule; }
}
