package sn.epf.pointage.model;

import jakarta.persistence.*;
import sn.epf.pointage.model.enums.TypeContrat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "professeurs")
public class Professeur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "matricule", nullable = false, unique = true, length = 30)
    private String matricule;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "prenom", nullable = false, length = 100)
    private String prenom;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "telephone", length = 20)
    private String telephone;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_contrat", nullable = false)
    private TypeContrat typeContrat;

    @Column(name = "taux_horaire_xof")
    private Double tauxHoraireXOF;

    @Column(name = "date_embauche")
    private LocalDate dateEmbauche;

    @Column(name = "photo", length = 500)
    private String photo; // chemin vers le fichier photo

    @Column(name = "actif")
    private Boolean actif = true;

    @Column(name = "filiere", length = 100)
    private String filiere;

    @Column(name = "specialite", length = 200)
    private String specialite;

    @OneToMany(mappedBy = "professeur", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Assignation> assignations = new ArrayList<>();

    @OneToMany(mappedBy = "professeur", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<RapportMensuel> rapports = new ArrayList<>();

    // Constructeurs
    public Professeur() {}

    public Professeur(String nom, String prenom, String email, TypeContrat typeContrat) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.typeContrat = typeContrat;
        this.actif = true;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMatricule() { return matricule; }
    public void setMatricule(String matricule) { this.matricule = matricule; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public TypeContrat getTypeContrat() { return typeContrat; }
    public void setTypeContrat(TypeContrat typeContrat) { this.typeContrat = typeContrat; }
    public Double getTauxHoraireXOF() { return tauxHoraireXOF; }
    public void setTauxHoraireXOF(Double tauxHoraireXOF) { this.tauxHoraireXOF = tauxHoraireXOF; }
    public LocalDate getDateEmbauche() { return dateEmbauche; }
    public void setDateEmbauche(LocalDate dateEmbauche) { this.dateEmbauche = dateEmbauche; }
    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }
    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }
    public String getFiliere() { return filiere; }
    public void setFiliere(String filiere) { this.filiere = filiere; }
    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }
    public List<Assignation> getAssignations() { return assignations; }
    public List<RapportMensuel> getRapports() { return rapports; }

    public String getNomComplet() { return prenom + " " + nom; }

    @Override
    public String toString() {
        return matricule + " - " + getNomComplet();
    }
}
