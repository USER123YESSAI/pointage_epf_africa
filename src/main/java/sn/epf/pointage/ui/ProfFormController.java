package sn.epf.pointage.ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import sn.epf.pointage.model.Professeur;
import sn.epf.pointage.model.enums.TypeContrat;
import sn.epf.pointage.service.EnrolementService;

import java.io.File;
import java.time.LocalDate;

public class ProfFormController {

    @FXML private TextField fieldNom;
    @FXML private TextField fieldPrenom;
    @FXML private TextField fieldEmail;
    @FXML private TextField fieldTelephone;
    @FXML private ComboBox<TypeContrat> comboContrat;
    @FXML private TextField fieldTauxHoraire;
    @FXML private DatePicker dateEmbauche;
    @FXML private ComboBox<String> comboFiliere;
    @FXML private TextField fieldSpecialite;
    @FXML private PasswordField fieldMotDePasse;
    @FXML private ImageView imagePhoto;
    @FXML private Label lblErreur;
    @FXML private Button btnSauvegarder;
    @FXML private Label lblTitre;

    private Professeur professeur;
    private Runnable onSaved;
    private final EnrolementService enrolementService = new EnrolementService();
    private String cheminPhoto;

    @FXML
    public void initialize() {
        comboContrat.setItems(FXCollections.observableArrayList(TypeContrat.values()));
        comboFiliere.setItems(FXCollections.observableArrayList("CSI", "GC", "GE", "GM", "GF", "MASTER"));
        dateEmbauche.setValue(LocalDate.now());

        // Validation en temps réel
        fieldEmail.textProperty().addListener((obs, old, val) -> validerEmail(val));
        fieldTelephone.textProperty().addListener((obs, old, val) -> validerTelephone(val));
        fieldNom.textProperty().addListener((obs, old, val) -> clearError());
        fieldPrenom.textProperty().addListener((obs, old, val) -> clearError());
    }

    public void setProfesseur(Professeur prof) {
        this.professeur = prof;
        if (prof != null) {
            lblTitre.setText("Modifier le profil");
            fieldNom.setText(prof.getNom());
            fieldPrenom.setText(prof.getPrenom());
            fieldEmail.setText(prof.getEmail());
            fieldTelephone.setText(prof.getTelephone());
            comboContrat.setValue(prof.getTypeContrat());
            comboFiliere.setValue(prof.getFiliere());
            fieldSpecialite.setText(prof.getSpecialite());
            if (prof.getTauxHoraireXOF() != null)
                fieldTauxHoraire.setText(String.valueOf(prof.getTauxHoraireXOF().intValue()));
            if (prof.getDateEmbauche() != null)
                dateEmbauche.setValue(prof.getDateEmbauche());
            fieldMotDePasse.setPromptText("Laisser vide pour ne pas changer");
        } else {
            lblTitre.setText("Enrôler un nouveau professeur");
        }
    }

    public void setOnSaved(Runnable callback) { this.onSaved = callback; }

    @FXML
    public void handleUploadPhoto() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir une photo");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File file = fc.showOpenDialog(imagePhoto.getScene().getWindow());
        if (file != null) {
            cheminPhoto = file.getAbsolutePath();
            imagePhoto.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    public void handleSauvegarder() {
        if (!validerFormulaire()) return;

        try {
            if (professeur == null) {
                // Création
                Professeur nouveau = new Professeur();
                remplirDepuisFormulaire(nouveau);
                String mdp = fieldMotDePasse.getText().trim();
                if (mdp.isEmpty()) mdp = "Epf@2024";
                enrolementService.enrollerProfesseur(nouveau, mdp);
            } else {
                // Modification
                remplirDepuisFormulaire(professeur);
                enrolementService.mettreAJourProfil(professeur);
            }

            if (onSaved != null) onSaved.run();
            fermer();

        } catch (Exception e) {
            showError("Erreur : " + e.getMessage());
        }
    }

    @FXML
    public void handleAnnuler() { fermer(); }

    // ─── Méthodes privées ─────────────────────────────────────────────────────

    private void remplirDepuisFormulaire(Professeur p) {
        p.setNom(fieldNom.getText().trim());
        p.setPrenom(fieldPrenom.getText().trim());
        p.setEmail(fieldEmail.getText().trim());
        p.setTelephone(fieldTelephone.getText().trim());
        p.setTypeContrat(comboContrat.getValue());
        p.setFiliere(comboFiliere.getValue());
        p.setSpecialite(fieldSpecialite.getText().trim());
        p.setDateEmbauche(dateEmbauche.getValue());
        if (cheminPhoto != null) p.setPhoto(cheminPhoto);
        if (!fieldTauxHoraire.getText().isBlank()) {
            try { p.setTauxHoraireXOF(Double.parseDouble(fieldTauxHoraire.getText().trim())); }
            catch (NumberFormatException ignored) {}
        }
    }

    private boolean validerFormulaire() {
        if (fieldNom.getText().isBlank()) { showError("Le nom est obligatoire."); fieldNom.requestFocus(); return false; }
        if (fieldPrenom.getText().isBlank()) { showError("Le prénom est obligatoire."); fieldPrenom.requestFocus(); return false; }
        if (!fieldEmail.getText().matches("^[\\w.+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            showError("Adresse email invalide."); fieldEmail.requestFocus(); return false;
        }
        if (comboContrat.getValue() == null) { showError("Sélectionnez un type de contrat."); return false; }
        return true;
    }

    private void validerEmail(String val) {
        if (!val.isEmpty() && !val.matches("^[\\w.+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            fieldEmail.getStyleClass().add("form-input-error");
        } else {
            fieldEmail.getStyleClass().remove("form-input-error");
        }
    }

    private void validerTelephone(String val) {
        if (!val.isEmpty() && !val.matches("^[\\d\\s+\\-()]{7,20}$")) {
            fieldTelephone.getStyleClass().add("form-input-error");
        } else {
            fieldTelephone.getStyleClass().remove("form-input-error");
        }
    }

    private void clearError() { lblErreur.setVisible(false); }

    private void showError(String msg) {
        lblErreur.setText(msg);
        lblErreur.setVisible(true);
        lblErreur.setManaged(true);
    }

    private void fermer() { ((Stage) fieldNom.getScene().getWindow()).close(); }
}
