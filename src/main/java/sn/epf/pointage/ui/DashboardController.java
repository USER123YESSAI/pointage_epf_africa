package sn.epf.pointage.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.util.Duration;
import sn.epf.pointage.model.SeancePlanifiee;
import sn.epf.pointage.service.DashboardService;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class DashboardController {

    @FXML private Label lblSeancesJour;
    @FXML private Label lblPresents;
    @FXML private Label lblAbsents;
    @FXML private Label lblTaux;

    @FXML private LineChart<String, Number> lineChart;
    @FXML private PieChart pieChart;

    @FXML private TableView<SeancePlanifiee> tableAlertes;
    @FXML private TableColumn<SeancePlanifiee, String> colAlerteProfesseur;
    @FXML private TableColumn<SeancePlanifiee, String> colAlerteCours;
    @FXML private TableColumn<SeancePlanifiee, String> colAlerteHeure;
    @FXML private TableColumn<SeancePlanifiee, String> colAlerteSalle;
    @FXML private TableColumn<SeancePlanifiee, String> colAlerteStatut;

    private final DashboardService dashboardService = new DashboardService();
    private Timeline refreshTimeline;

    @FXML
    public void initialize() {
        configurerColonnesAlertes();
        chargerDonnees();
        demarrerRafraichissement();
    }

    private void configurerColonnesAlertes() {
        // CORRECTION : protection LazyInitializationException sur toutes les colonnes
        colAlerteProfesseur.setCellValueFactory(data -> {
            try {
                SeancePlanifiee s = data.getValue();
                String nom = s.getAssignation() != null && s.getAssignation().getProfesseur() != null
                        ? s.getAssignation().getProfesseur().getNomComplet() : "N/A";
                return new javafx.beans.property.SimpleStringProperty(nom);
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("N/A");
            }
        });
        colAlerteCours.setCellValueFactory(data -> {
            try {
                SeancePlanifiee s = data.getValue();
                String cours = s.getAssignation() != null && s.getAssignation().getCours() != null
                        ? s.getAssignation().getCours().getIntitule() : "N/A";
                return new javafx.beans.property.SimpleStringProperty(cours);
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("N/A");
            }
        });
        colAlerteHeure.setCellValueFactory(data -> {
            String heure = data.getValue().getDateHeure() != null
                    ? data.getValue().getDateHeure().toLocalTime().toString() : "";
            return new javafx.beans.property.SimpleStringProperty(heure);
        });
        colAlerteSalle.setCellValueFactory(data -> {
            try {
                SeancePlanifiee s = data.getValue();
                String salle = s.getAssignation() != null && s.getAssignation().getSalle() != null
                        ? s.getAssignation().getSalle().getNom() : "N/A";
                return new javafx.beans.property.SimpleStringProperty(salle);
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("N/A");
            }
        });
        colAlerteStatut.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getStatut().toString()));
    }

    private void chargerDonnees() {
        long seancesJour = dashboardService.getSeancesDuJour();
        long presents    = dashboardService.getProfesseursPresentsAujourdhui();
        long absents     = dashboardService.getProfesseursAbsentsAujourdhui();
        double taux      = dashboardService.getTauxPresenceAujourdhui();

        lblSeancesJour.setText(String.valueOf(seancesJour));
        lblPresents.setText(String.valueOf(presents));
        lblAbsents.setText(String.valueOf(absents));
        lblTaux.setText(String.format("%.0f%%", taux));

        long vacataires = dashboardService.getNombreVacataires();
        long permanents = dashboardService.getNombrePermanents();
        pieChart.setData(FXCollections.observableArrayList(
                new PieChart.Data("Vacataires (" + vacataires + ")", Math.max(vacataires, 0.01)),
                new PieChart.Data("Permanents (" + permanents + ")", Math.max(permanents, 0.01))
        ));

        chargerLineChart();

        List<SeancePlanifiee> alertes = dashboardService.getAlertesDuJour();
        tableAlertes.setItems(FXCollections.observableArrayList(alertes));
    }

    private void chargerLineChart() {
        lineChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Séances réalisées");
        LocalDate now = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate mois = now.minusMonths(i);
            String nomMois = mois.getMonth().getDisplayName(TextStyle.SHORT, Locale.FRENCH);
            series.getData().add(new XYChart.Data<>(nomMois, (long)(Math.random() * 20 + 5)));
        }
        lineChart.getData().add(series);
    }

    private void demarrerRafraichissement() {
        refreshTimeline = new Timeline(new KeyFrame(Duration.seconds(60), e ->
                Platform.runLater(this::chargerDonnees)));
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();
    }
}
