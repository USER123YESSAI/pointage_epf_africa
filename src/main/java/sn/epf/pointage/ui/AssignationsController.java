package sn.epf.pointage.ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCodeCombination;
import sn.epf.pointage.config.SessionContext;
import sn.epf.pointage.dao.AssignationDAO;
import sn.epf.pointage.model.Assignation;
import sn.epf.pointage.model.Professeur;
import sn.epf.pointage.ui.AssignationFormController;
import sn.epf.pointage.ui.Toast;

public class AssignationsController {

    @FXML private TableView<Assignation> tableAssignations;
    @FXML private TableColumn<Assignation, Long> colId;
    @FXML private TableColumn<Assignation, String> colProfesseur;
    @FXML private TableColumn<Assignation, String> colCours;
    @FXML private TableColumn<Assignation, String> colSalle;
    @FXML private TableColumn<Assignation, String> colAnnee;
    @FXML private TableColumn<Assignation, Integer> colHeures;
    @FXML private Button btnNew;
    @FXML private Button btnRefresh;

    private final AssignationDAO assignationDAO = new AssignationDAO();

    @FXML
    public void initialize() {
        try {
            System.out.println("AssignationsController.initialize()");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colProfesseur.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getProfesseur() != null ? cell.getValue().getProfesseur().getNomComplet() : "N/A"));
        colCours.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getCours() != null ? cell.getValue().getCours().getIntitule() : "N/A"));
        colSalle.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getSalle() != null ? cell.getValue().getSalle().getNom() : "N/A"));
        colAnnee.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getAnneeAcademique() != null ? cell.getValue().getAnneeAcademique() : ""));
        colHeures.setCellValueFactory(new PropertyValueFactory<>("heuresPrevues"));
        // placeholder when table empty
        tableAssignations.setPlaceholder(new javafx.scene.control.Label("Aucune assignation trouvée"));

        try {
            configureAccess();
            loadAssignations();

            // Ajouter raccourci Ctrl+N pour ouvrir le formulaire
            if (btnNew != null) {
                btnNew.sceneProperty().addListener((obs, oldScene, newScene) -> {
                    if (newScene != null) {
                        KeyCombination kc = new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN);
                        newScene.getAccelerators().put(kc, () -> handleNewAssignation());
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            javafx.application.Platform.runLater(() -> {
                Alert a = new Alert(Alert.AlertType.ERROR, "Erreur initialisation Assignations : " + e.getMessage(), ButtonType.OK);
                a.setTitle("Erreur");
                a.showAndWait();
            });
        }
    }

    @FXML
    public void handleRefresh() {
        loadAssignations();
    }

    @FXML
    public void handleNewAssignation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/assignation_form.fxml"));
            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(btnNew.getScene().getWindow());
            dialog.setTitle("Nouvelle assignation");
            dialog.setScene(new Scene(root, 760, 720));
            // Récupérer le controller pour savoir si sauvegarde effectuée
            AssignationFormController formCtrl = loader.getController();
            dialog.showAndWait();

            boolean wasSaved = formCtrl != null && formCtrl.isSaved();
            if (wasSaved) {
                Toast.show(btnNew, "Assignation créée avec succès.");
            }

            // Rafraîchir la liste après fermeture
            loadAssignations();
        } catch (Exception e) {
            Alert err = new Alert(Alert.AlertType.ERROR, "Erreur ouverture formulaire : " + e.getMessage(), ButtonType.OK);
            err.setTitle("Erreur");
            err.showAndWait();
            e.printStackTrace();
        }
    }

    

    private void loadAssignations() {
        try {
            java.util.List<Assignation> list;
            if (SessionContext.getInstance().isProfesseur()) {
                Professeur prof = SessionContext.getInstance().getUtilisateurConnecte().getProfesseurLie();
                if (prof != null) {
                    list = assignationDAO.findByProfesseur(prof.getId());
                } else {
                    list = java.util.Collections.emptyList();
                }
            } else {
                list = assignationDAO.findAllWithAssociations();
            }
            tableAssignations.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void configureAccess() {
        boolean peutGerer = SessionContext.getInstance().peutEnrolerProfesseurs();
        btnNew.setVisible(peutGerer);
        btnNew.setManaged(peutGerer);
    }
}
