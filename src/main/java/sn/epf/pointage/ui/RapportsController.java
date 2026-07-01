package sn.epf.pointage.ui;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import sn.epf.pointage.config.SessionContext;
import sn.epf.pointage.dao.PointageDAO;
import sn.epf.pointage.dao.ProfesseurDAO;
import sn.epf.pointage.dao.SeanceDAO;
import sn.epf.pointage.model.Professeur;
import sn.epf.pointage.model.RapportMensuel;
import sn.epf.pointage.model.SeancePlanifiee;
import sn.epf.pointage.model.enums.TypePointage;
import sn.epf.pointage.service.RapportService;

public class RapportsController {

    @FXML private ComboBox<Professeur> comboProfesseur;
    @FXML private ComboBox<Integer>    comboMois;
    @FXML private ComboBox<Integer>    comboAnnee;
    @FXML private Label lblTotalHeures;
    @FXML private Label lblMontantXOF;
    @FXML private Label lblStatutRapport;
    @FXML private TableView<SeancePlanifiee>         tableSeances;
    @FXML private TableColumn<SeancePlanifiee, String> colDate;
    @FXML private TableColumn<SeancePlanifiee, String> colCours;
    @FXML private TableColumn<SeancePlanifiee, String> colDuree;
    @FXML private TableColumn<SeancePlanifiee, String> colStatut;
    @FXML private TableColumn<SeancePlanifiee, String> colPointage;
    @FXML private Button btnValider;
    @FXML private Button btnExporter;

    private final RapportService  rapportService  = new RapportService();
    private final ProfesseurDAO   professeurDAO   = new ProfesseurDAO();
    private final SeanceDAO       seanceDAO       = new SeanceDAO();
    private final PointageDAO     pointageDAO     = new PointageDAO();
    private RapportMensuel rapportCourant;

    @FXML
    public void initialize() {
        configurerColonnes();
        chargerFiltres();
        configurerAcces();
    }

    private void chargerFiltres() {
        List<Professeur> profs = professeurDAO.findAllActifs();
        comboProfesseur.setItems(FXCollections.observableArrayList(profs));
        comboProfesseur.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Professeur p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? null
                        : p.getNomComplet() + " (" + p.getMatricule() + ")");
            }
        });
        comboProfesseur.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Professeur p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? "Sélectionner..." : p.getNomComplet());
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

        comboMois.setItems(FXCollections.observableArrayList(1,2,3,4,5,6,7,8,9,10,11,12));
        comboMois.setValue(java.time.LocalDate.now().getMonthValue());

        int annee = java.time.LocalDate.now().getYear();
        comboAnnee.setItems(FXCollections.observableArrayList(annee-2, annee-1, annee, annee+1));
        comboAnnee.setValue(annee);
    }

    private void configurerColonnes() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        colDate.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getDateHeure() != null
                                ? data.getValue().getDateHeure().format(fmt) : ""));

        // CORRECTION : protection LazyInitializationException
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

        colDuree.setCellValueFactory(data -> {
            Integer d = data.getValue().getDureeMinutes();
            return new javafx.beans.property.SimpleStringProperty(
                    d != null ? String.format("%.2f h", d / 60.0) : "N/A");
        });

        colStatut.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getStatut().toString()));

        colPointage.setCellValueFactory(data -> {
            try {
                boolean ok = pointageDAO.findBySeanceAndType(
                        data.getValue().getId(), TypePointage.DEBUT).isPresent();
                return new javafx.beans.property.SimpleStringProperty(ok ? "✅ Oui" : "❌ Non");
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("—");
            }
        });
    }

    private void configurerAcces() {
        boolean peutValider = SessionContext.getInstance().peutValiderRapports();
        btnValider.setVisible(peutValider);
        btnValider.setManaged(peutValider);
    }

    @FXML
    public void handleGenerer() {
        Professeur prof  = comboProfesseur.getValue();
        Integer    mois  = comboMois.getValue();
        Integer    annee = comboAnnee.getValue();

        if (prof == null || mois == null || annee == null) {
            new Alert(Alert.AlertType.WARNING,
                    "Veuillez sélectionner un professeur, un mois et une année.",
                    ButtonType.OK).showAndWait();
            return;
        }

        try {
            rapportCourant = rapportService.genererRapportMensuel(prof.getId(), mois, annee);

            lblTotalHeures.setText(
                    String.format("Total heures : %.2f h", rapportCourant.getHeuresRealisees()));
            lblMontantXOF.setText(
                    String.format("Montant XOF : %,.0f XOF", rapportCourant.getMontantXOF()));
            lblStatutRapport.setText("Statut : " + rapportCourant.getStatut());

            List<SeancePlanifiee> seances =
                    seanceDAO.findByProfesseurAndMois(prof.getId(), mois, annee);
            tableSeances.setItems(FXCollections.observableArrayList(seances));

            btnValider.setDisable(false);
            btnExporter.setDisable(false);

        } catch (IllegalStateException e) {
            new Alert(Alert.AlertType.WARNING, e.getMessage(), ButtonType.OK).showAndWait();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR,
                    "Erreur génération rapport : " + e.getMessage(), ButtonType.OK).showAndWait();
            e.printStackTrace();
        }
    }

    @FXML
    public void handleValider() {
        if (rapportCourant == null) return;
        try {
            rapportService.validerRapport(rapportCourant.getId());
            lblStatutRapport.setText("Statut : VALIDE");
            new Alert(Alert.AlertType.INFORMATION,
                    "Rapport validé avec succès.", ButtonType.OK).showAndWait();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR,
                    "Erreur : " + e.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    @FXML
    public void handleExporter() {
        if (rapportCourant == null) return;

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exporter le rapport au format PDF");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichier PDF", "*.pdf"));
        chooser.setInitialFileName(String.format("rapport-%s-%02d-%d.pdf",
                rapportCourant.getProfesseur().getMatricule(),
                rapportCourant.getMois(),
                rapportCourant.getAnnee()));

        File fichier = chooser.showSaveDialog(btnExporter.getScene().getWindow());
        if (fichier == null) {
            return;
        }
        if (!fichier.getName().toLowerCase().endsWith(".pdf")) {
            fichier = new File(fichier.getAbsolutePath() + ".pdf");
        }

        try {
            rapportService.exporterRapportPdf(rapportCourant.getId(), Path.of(fichier.getAbsolutePath()));
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(fichier);
                } catch (IOException ex) {
                    System.err.println("Impossible d'ouvrir le PDF automatiquement : " + ex.getMessage());
                }
            }
            new Alert(Alert.AlertType.INFORMATION,
                    "Export PDF créé : " + fichier.getAbsolutePath(), ButtonType.OK).showAndWait();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR,
                    "Erreur export PDF : " + e.getMessage(), ButtonType.OK).showAndWait();
            e.printStackTrace();
        }
    }
}
