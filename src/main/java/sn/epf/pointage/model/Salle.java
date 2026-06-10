package sn.epf.pointage.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "salles")
public class Salle {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 50) private String nom;
    @Column private Integer capacite;
    @Column(length = 100) private String batiment;
    @Column(length = 300) private String equipements;
    @OneToMany(mappedBy = "salle", fetch = FetchType.LAZY) private List<Assignation> assignations = new ArrayList<>();

    public Salle() {}
    public Salle(String nom, String batiment, Integer capacite) { this.nom=nom; this.batiment=batiment; this.capacite=capacite; }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getNom() { return nom; } public void setNom(String nom) { this.nom = nom; }
    public Integer getCapacite() { return capacite; } public void setCapacite(Integer c) { this.capacite = c; }
    public String getBatiment() { return batiment; } public void setBatiment(String b) { this.batiment = b; }
    public String getEquipements() { return equipements; } public void setEquipements(String e) { this.equipements = e; }
    public List<Assignation> getAssignations() { return assignations; }
    @Override public String toString() { return nom + " (" + batiment + ")"; }
}
