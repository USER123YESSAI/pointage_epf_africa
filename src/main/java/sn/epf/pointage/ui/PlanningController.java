package sn.epf.pointage.ui;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sn.epf.pointage.config.SessionContext;
import sn.epf.pointage.dao.ProfesseurDAO;
import sn.epf.pointage.dao.SeanceDAO;
import sn.epf.pointage.model.Professeur;
import sn.epf.pointage.model.SeancePlanifiee;

public class PlanningController {

    @FXML private ComboBox<Professeur> comboProfesseur;
    @FXML private DatePicker dateDebut;
    @FXML private TableView<SeancePlanifiee> tableSeances;
    @FXML private TableColumn<SeancePlanifiee, String> colJour;
    @FXML private TableColumn<SeancePlanifiee, String> colHeure;
    @FXML private TableColumn<SeancePlanifiee, String> colCours;
    @FXML private TableColumn<SeancePlanifiee, String> colSalle;
    @FXML private TableColumn<SeancePlanifiee, String> colDuree;
    @FXML private TableColumn<SeancePlanifiee, String> colStatut;
    @FXML private Button btnNouvellePlanification;

    private final ProfesseurDAO professeurDAO = new ProfesseurDAO();
    private final SeanceDAO seanceDAO = new SeanceDAO();

    @FXML
    public void initialize() {
        configurerColonnes();
        chargerProfesseurs();

        dateDebut.setValue(LocalDate.now());
        dateDebut.valueProperty().addListener((obs, old, val) -> chargerSeances());
        comboProfesseur.valueProperty().addListener((obs, old, val) -> chargerSeances());

        boolean peutGerer = SessionContext.getInstance().peutEnrolerProfesseurs();
        btnNouvellePlanification.setVisible(peutGerer);
        btnNouvellePlanification.setManaged(peutGerer);

        chargerSeances();
    }

    private void chargerProfesseurs() {
        List<Professeur> profs = professeurDAO.findAllActifs();
        comboProfesseur.setItems(FXCollections.observableArrayList(profs));
        comboProfesseur.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Professeur p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? null : p.getNomComplet());
            }
        });
        comboProfesseur.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Professeur p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? "Tous les professeurs" : p.getNomComplet());
            }
        });

        if (SessionContext.getInstance().isProfesseur()) {
            Professeur profConnecte = SessionContext.getInstance()
                    .getUtilisateurConnecte().getProfesseurLie();
            if (profConnecte != null) {
                comboProfesseur.setValue(profs.stream()
                        .filter(p -> p.getId().equals(profConnecte.getId()))
                        .findFirst().orElse(null));
                comboProfesseur.setDisable(true);
            }
        }
    }

    private void configurerColonnes() {
        DateTimeFormatter dateFmt  = DateTimeFormatter.ofPattern("EEEE dd/MM", java.util.Locale.FRENCH);
        DateTimeFormatter heureFmt = DateTimeFormatter.ofPattern("HH:mm");

        colJour.setCellValueFactory(data -> {
            String j = data.getValue().getDateHeure() != null
                    ? data.getValue().getDateHeure().format(dateFmt) : "";
            return new javafx.beans.property.SimpleStringProperty(j);
        });

        colHeure.setCellValueFactory(data -> {
            String h = data.getValue().getDateHeure() != null
                    ? data.getValue().getDateHeure().format(heureFmt) : "";
            return new javafx.beans.property.SimpleStringProperty(h);
        });

        // CORRECTION : protection LazyInitializationException sur toutes les colonnes
        colCours.setCellValueFactory(data -> {
            try {
                SeancePlanifiee s = data.getValue();
                if (s.getAssignation() == null || s.getAssignation().getCours() == null)
                    return new javafx.beans.property.SimpleStringProperty("N/A");
                return new javafx.beans.property.SimpleStringProperty(
                        s.getAssignation().getCours().getIntitule());
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("N/A");
            }
        });

        colSalle.setCellValueFactory(data -> {
            try {
                SeancePlanifiee s = data.getValue();
                if (s.getAssignation() == null || s.getAssignation().getSalle() == null)
                    return new javafx.beans.property.SimpleStringProperty("N/A");
                return new javafx.beans.property.SimpleStringProperty(
                        s.getAssignation().getSalle().getNom());
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("N/A");
            }
        });

        colDuree.setCellValueFactory(data -> {
            Integer d = data.getValue().getDureeMinutes();
            return new javafx.beans.property.SimpleStringProperty(d != null ? d + " min" : "N/A");
        });

        colStatut.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getStatut().toString()));
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(s);
                badge.getStyleClass().add(switch (s) {
                    case "REALISEE" -> "badge-ok";
                    case "ANNULEE"  -> "badge-absent";
                    default         -> "badge-retard";
                });
                setGraphic(badge);
                setText(null);
            }
        });
    }

    private void chargerSeances() {
        if (dateDebut.getValue() == null) return;

        LocalDate lundi    = dateDebut.getValue().with(DayOfWeek.MONDAY);
        LocalDate dimanche = lundi.plusDays(6);
        LocalDateTime debut = lundi.atStartOfDay();
        LocalDateTime fin   = dimanche.plusDays(1).atStartOfDay();

        Professeur filtre = comboProfesseur.getValue();
        List<SeancePlanifiee> seances;

        if (filtre == null) {
            seances = seanceDAO.findByPeriode(debut, fin);
        } else {
            seances = seanceDAO.findByPeriodeEtProfesseur(debut, fin, filtre.getId());
        }

        tableSeances.setItems(FXCollections.observableArrayList(seances));
    }

    @FXML
    public void handleNouvellePlanification() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/assignation_form.fxml"));
            Parent root = loader.load();
            AssignationFormController formCtrl = loader.getController();

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(btnNouvellePlanification.getScene().getWindow());
            dialog.setTitle("Nouvelle assignation de cours");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            if (formCtrl != null && formCtrl.isSaved()) {
                new Alert(Alert.AlertType.INFORMATION,
                        "Assignation créée avec succès ! Les séances ont été générées.",
                        ButtonType.OK).showAndWait();
            }
            chargerSeances();

        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR,
                    "Erreur ouverture formulaire : " + e.getMessage(),
                    ButtonType.OK).showAndWait();
            e.printStackTrace();
        }
    }

    @FXML public void handleSemainePrec() {
        if (dateDebut.getValue() != null)
            dateDebut.setValue(dateDebut.getValue().minusWeeks(1));
    }

    @FXML public void handleSemaineSuiv() {
        if (dateDebut.getValue() != null)
            dateDebut.setValue(dateDebut.getValue().plusWeeks(1));
    }
}
