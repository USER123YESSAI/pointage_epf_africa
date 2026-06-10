package sn.epf.pointage.ui;

import java.util.List;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sn.epf.pointage.config.SessionContext;
import sn.epf.pointage.dao.ProfesseurDAO;
import sn.epf.pointage.model.Professeur;
import sn.epf.pointage.model.enums.TypeContrat;
import sn.epf.pointage.service.EnrolementService;

public class ProfesseursController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filtreContrat;
    @FXML private ComboBox<String> filtreFiliere;
    @FXML private TableView<Professeur> tableProfesseurs;
    @FXML private TableColumn<Professeur, String> colMatricule;
    @FXML private TableColumn<Professeur, String> colNom;
    @FXML private TableColumn<Professeur, String> colPrenom;
    @FXML private TableColumn<Professeur, String> colEmail;
    @FXML private TableColumn<Professeur, TypeContrat> colContrat;
    @FXML private TableColumn<Professeur, String> colFiliere;
    @FXML private TableColumn<Professeur, Boolean> colActif;
    @FXML private Button btnModifier;
    @FXML private Button btnDesactiver;
    @FXML private Button btnAjouter;

    private final ProfesseurDAO professeurDAO = new ProfesseurDAO();
    private final EnrolementService enrolementService = new EnrolementService();
    private List<Professeur> tousLesProfesseurs;

    @FXML
    public void initialize() {
        configurerColonnes();
        configurerFiltres();
        chargerProfesseurs();
        configurerSelection();
        configurerAcces();
    }

    private void configurerColonnes() {
        colMatricule.setCellValueFactory(new PropertyValueFactory<>("matricule"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colContrat.setCellValueFactory(new PropertyValueFactory<>("typeContrat"));
        colFiliere.setCellValueFactory(new PropertyValueFactory<>("filiere"));

        // Colonne statut colorée
        colActif.setCellValueFactory(new PropertyValueFactory<>("actif"));
        colActif.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean actif, boolean empty) {
                super.updateItem(actif, empty);
                if (empty || actif == null) { setText(null); setGraphic(null); return; }
                Label badge = new Label(actif ? "Actif" : "Inactif");
                badge.getStyleClass().add(actif ? "badge-ok" : "badge-absent");
                setGraphic(badge);
                setText(null);
            }
        });
    }

    private void configurerFiltres() {
        filtreContrat.setItems(FXCollections.observableArrayList("Tous", "VACATAIRE", "PERMANENT"));
        filtreContrat.setValue("Tous");
        filtreFiliere.setItems(FXCollections.observableArrayList("Toutes", "CSI", "GC", "GE", "GM", "GF"));
        filtreFiliere.setValue("Toutes");
    }

    private void chargerProfesseurs() {
        tousLesProfesseurs = professeurDAO.findAll();
        appliquerFiltres();
    }

    private void configurerSelection() {
        tableProfesseurs.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            boolean selected_ = selected != null;
            btnModifier.setDisable(!selected_);
            btnDesactiver.setDisable(!selected_ || (selected_ && !selected.getActif()));
        });
    }

   private void configurerAcces() {
    boolean peutGerer = SessionContext.getInstance().peutEnrolerProfesseurs();
    btnAjouter.setVisible(peutGerer);    
    btnAjouter.setManaged(peutGerer);    
    btnModifier.setVisible(peutGerer);
    btnModifier.setManaged(peutGerer);  
    btnDesactiver.setVisible(peutGerer);
    btnDesactiver.setManaged(peutGerer);
}
    @FXML
    public void handleRecherche() {
        appliquerFiltres();
    }

    @FXML
    public void handleFiltre() {
        appliquerFiltres();
    }

    @FXML
    public void handleReset() {
        searchField.clear();
        filtreContrat.setValue("Tous");
        filtreFiliere.setValue("Toutes");
        appliquerFiltres();
    }

    private void appliquerFiltres() {
        String recherche = searchField.getText().toLowerCase().trim();
        String contrat = filtreContrat.getValue();
        String filiere = filtreFiliere.getValue();

        List<Professeur> filtrés = tousLesProfesseurs.stream()
                .filter(p -> recherche.isEmpty()
                        || p.getNom().toLowerCase().contains(recherche)
                        || p.getPrenom().toLowerCase().contains(recherche)
                        || p.getMatricule().toLowerCase().contains(recherche))
                .filter(p -> "Tous".equals(contrat) || p.getTypeContrat().toString().equals(contrat))
                .filter(p -> "Toutes".equals(filiere) || filiere.equals(p.getFiliere()))
                .collect(Collectors.toList());

        tableProfesseurs.setItems(FXCollections.observableArrayList(filtrés));
    }

    @FXML
    public void handleAjouter() {
        ouvrirFormulaire(null);
    }

    @FXML
    public void handleModifier() {
        Professeur selected = tableProfesseurs.getSelectionModel().getSelectedItem();
        if (selected != null) ouvrirFormulaire(selected);
    }

    @FXML
    public void handleDesactiver() {
        Professeur selected = tableProfesseurs.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Désactiver " + selected.getNomComplet() + " ?\nSes séances futures seront annulées.",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation désactivation");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    enrolementService.desactiverProfesseur(selected.getId(), "Désactivation manuelle");
                    chargerProfesseurs();
                    afficherSucces("Professeur désactivé avec succès.");
                } catch (Exception e) {
                    afficherErreur("Erreur : " + e.getMessage());
                }
            }
        });
    }

    private void ouvrirFormulaire(Professeur professeur) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/prof_form.fxml"));
            Parent root = loader.load();
            ProfFormController ctrl = loader.getController();
            ctrl.setProfesseur(professeur);
            ctrl.setOnSaved(this::chargerProfesseurs);

            Stage stage = new Stage();
            stage.setTitle(professeur == null ? "Ajouter un professeur" : "Modifier le profil");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            afficherErreur("Impossible d'ouvrir le formulaire : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void afficherSucces(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }

    private void afficherErreur(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }
}
