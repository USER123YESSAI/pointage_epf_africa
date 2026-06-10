package sn.epf.pointage.ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import sn.epf.pointage.config.SessionContext;
import sn.epf.pointage.dao.ProfesseurDAO;
import sn.epf.pointage.dao.SeanceDAO;
import sn.epf.pointage.dao.PointageDAO;
import sn.epf.pointage.model.Professeur;
import sn.epf.pointage.model.RapportMensuel;
import sn.epf.pointage.model.SeancePlanifiee;
import sn.epf.pointage.model.enums.StatutSeance;
import sn.epf.pointage.model.enums.TypePointage;
import sn.epf.pointage.service.RapportService;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class RapportsController {

    @FXML private ComboBox<Professeur> comboProfesseur;
    @FXML private ComboBox<Integer> comboMois;
    @FXML private ComboBox<Integer> comboAnnee;
    @FXML private Label lblTotalHeures;
    @FXML private Label lblMontantXOF;
    @FXML private Label lblStatutRapport;
    @FXML private TableView<SeancePlanifiee> tableSeances;
    @FXML private TableColumn<SeancePlanifiee, String> colDate;
    @FXML private TableColumn<SeancePlanifiee, String> colCours;
    @FXML private TableColumn<SeancePlanifiee, String> colDuree;
    @FXML private TableColumn<SeancePlanifiee, String> colStatut;
    @FXML private TableColumn<SeancePlanifiee, String> colPointage;
    @FXML private Button btnValider;
    @FXML private Button btnExporter;

    private final RapportService rapportService = new RapportService();
    private final ProfesseurDAO professeurDAO = new ProfesseurDAO();
    private final SeanceDAO seanceDAO = new SeanceDAO();
    private final PointageDAO pointageDAO = new PointageDAO();
    private RapportMensuel rapportCourant;

    @FXML
    public void initialize() {
        configurerColonnes();
        chargerFiltres();
        configurerAcces();
    }

    private void chargerFiltres() {
        // Professeurs
        List<Professeur> profs = professeurDAO.findAllActifs();
        comboProfesseur.setItems(FXCollections.observableArrayList(profs));
        comboProfesseur.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Professeur p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? null : p.getNomComplet() + " (" + p.getMatricule() + ")");
            }
        });
        comboProfesseur.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Professeur p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? "Sélectionner..." : p.getNomComplet());
            }
        });

        // Si PROFESSEUR connecté, pré-sélectionner son profil
        if (SessionContext.getInstance().isProfesseur()) {
            Professeur profConnecte = SessionContext.getInstance().getUtilisateurConnecte().getProfesseurLie();
            if (profConnecte != null) {
                comboProfesseur.setValue(profs.stream()
                        .filter(p -> p.getId().equals(profConnecte.getId())).findFirst().orElse(null));
                comboProfesseur.setDisable(true);
            }
        }

        // Mois
        comboMois.setItems(FXCollections.observableArrayList(1,2,3,4,5,6,7,8,9,10,11,12));
        comboMois.setValue(java.time.LocalDate.now().getMonthValue());

        // Années
        int annee = java.time.LocalDate.now().getYear();
        comboAnnee.setItems(FXCollections.observableArrayList(annee-2, annee-1, annee, annee+1));
        comboAnnee.setValue(annee);
    }

    private void configurerColonnes() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        colDate.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getDateHeure() != null ? data.getValue().getDateHeure().format(fmt) : ""));

        colCours.setCellValueFactory(data -> {
            SeancePlanifiee s = data.getValue();
            String c = s.getAssignation() != null && s.getAssignation().getCours() != null
                    ? s.getAssignation().getCours().getIntitule() : "N/A";
            return new javafx.beans.property.SimpleStringProperty(c);
        });

        colDuree.setCellValueFactory(data -> {
            Integer d = data.getValue().getDureeMinutes();
            String txt = d != null ? String.format("%.2f h", d / 60.0) : "N/A";
            return new javafx.beans.property.SimpleStringProperty(txt);
        });

        colStatut.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getStatut().toString()));

        colPointage.setCellValueFactory(data -> {
            boolean ok = pointageDAO.findBySeanceAndType(data.getValue().getId(), TypePointage.DEBUT).isPresent();
            return new javafx.beans.property.SimpleStringProperty(ok ? "✅ Oui" : "❌ Non");
        });
    }

    private void configurerAcces() {
        boolean peutValider = SessionContext.getInstance().peutValiderRapports();
        btnValider.setVisible(peutValider);
    }

    @FXML
    public void handleGenerer() {
        Professeur prof = comboProfesseur.getValue();
        Integer mois = comboMois.getValue();
        Integer annee = comboAnnee.getValue();

        if (prof == null || mois == null || annee == null) {
            new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner un professeur, un mois et une année.", ButtonType.OK).showAndWait();
            return;
        }

        try {
            rapportCourant = rapportService.genererRapportMensuel(prof.getId(), mois, annee);

            // Mettre à jour les labels
            lblTotalHeures.setText(String.format("Total heures : %.2f h", rapportCourant.getHeuresRealisees()));
            lblMontantXOF.setText(String.format("Montant XOF : %,.0f XOF", rapportCourant.getMontantXOF()));
            lblStatutRapport.setText("Statut : " + rapportCourant.getStatut());

            // Charger les séances du mois
            List<SeancePlanifiee> seances = seanceDAO.findByProfesseurAndMois(prof.getId(), mois, annee);
            tableSeances.setItems(FXCollections.observableArrayList(seances));

            btnValider.setDisable(false);
            btnExporter.setDisable(false);

        } catch (IllegalStateException e) {
            new Alert(Alert.AlertType.WARNING, e.getMessage(), ButtonType.OK).showAndWait();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Erreur génération rapport : " + e.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    @FXML
    public void handleValider() {
        if (rapportCourant == null) return;
        try {
            rapportService.validerRapport(rapportCourant.getId());
            lblStatutRapport.setText("Statut : VALIDE");
            new Alert(Alert.AlertType.INFORMATION, "Rapport validé avec succès.", ButtonType.OK).showAndWait();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Erreur : " + e.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    @FXML
    public void handleExporter() {
        if (rapportCourant == null) return;
        try {
            String contenu = rapportService.exporterRapportTexte(rapportCourant.getId());

            // Afficher dans une fenêtre texte (PDF JasperReports en bonus)
            TextArea ta = new TextArea(contenu);
            ta.setEditable(false);
            ta.setStyle("-fx-font-family: monospace; -fx-font-size: 12;");
            ta.setPrefSize(600, 500);

            javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
            dialog.setTitle("Rapport mensuel — Aperçu");
            dialog.getDialogPane().setContent(ta);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.showAndWait();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Erreur export : " + e.getMessage(), ButtonType.OK).showAndWait();
        }
    }
}
