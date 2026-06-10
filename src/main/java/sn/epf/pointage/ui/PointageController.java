package sn.epf.pointage.ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import sn.epf.pointage.config.SessionContext;
import sn.epf.pointage.dao.ProfesseurDAO;
import sn.epf.pointage.dao.SeanceDAO;
import sn.epf.pointage.model.Professeur;
import sn.epf.pointage.model.SeancePlanifiee;
import sn.epf.pointage.model.enums.ResultatPointage;
import sn.epf.pointage.model.enums.TypePointage;
import sn.epf.pointage.service.PointageService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PointageController {

    @FXML private Label lblDate;
    @FXML private TableView<SeancePlanifiee> tableSeances;
    @FXML private TableColumn<SeancePlanifiee, String> colHeure;
    @FXML private TableColumn<SeancePlanifiee, String> colCours;
    @FXML private TableColumn<SeancePlanifiee, String> colSalle;
    @FXML private TableColumn<SeancePlanifiee, String> colStatut;
    @FXML private TableColumn<SeancePlanifiee, String> colDebut;
    @FXML private TableColumn<SeancePlanifiee, String> colFin;
    @FXML private Button btnPointerDebut;
    @FXML private Button btnPointerFin;
    @FXML private Label lblResultat;

    private final PointageService pointageService = new PointageService();
    private final SeanceDAO seanceDAO = new SeanceDAO();
    private final ProfesseurDAO professeurDAO = new ProfesseurDAO();

    @FXML
    public void initialize() {
        lblDate.setText("Aujourd'hui : " + LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy",
                java.util.Locale.FRENCH)));
        configurerColonnes();
        chargerSeances();
        configurerSelection();
    }

    private void configurerColonnes() {
        colHeure.setCellValueFactory(data -> {
            String h = data.getValue().getDateHeure() != null
                    ? data.getValue().getDateHeure().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "";
            return new javafx.beans.property.SimpleStringProperty(h);
        });

        colCours.setCellValueFactory(data -> {
            SeancePlanifiee s = data.getValue();
            String cours = s.getAssignation() != null && s.getAssignation().getCours() != null
                    ? s.getAssignation().getCours().getIntitule() : "N/A";
            return new javafx.beans.property.SimpleStringProperty(cours);
        });

        colSalle.setCellValueFactory(data -> {
            SeancePlanifiee s = data.getValue();
            String salle = s.getAssignation() != null && s.getAssignation().getSalle() != null
                    ? s.getAssignation().getSalle().getNom() : "N/A";
            return new javafx.beans.property.SimpleStringProperty(salle);
        });

        // Statut avec badge coloré
        colStatut.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getStatut().toString()));
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(statut);
                switch (statut) {
                    case "REALISEE" -> badge.getStyleClass().add("badge-ok");
                    case "PLANIFIEE" -> badge.getStyleClass().add("badge-retard");
                    case "ANNULEE"  -> badge.getStyleClass().add("badge-absent");
                    default -> badge.getStyleClass().add("badge-retard");
                }
                setGraphic(badge);
                setText(null);
            }
        });

        colDebut.setCellValueFactory(data -> {
            String couleur = pointageService.getCouleurStatut(data.getValue());
            String txt = switch(couleur) {
                case "vert"   -> "✅ Pointé";
                case "orange" -> "⚠ Retard";
                case "rouge"  -> "❌ Absent";
                default       -> "— En attente";
            };
            return new javafx.beans.property.SimpleStringProperty(txt);
        });

        colFin.setCellValueFactory(data -> {
            boolean hasFin = new sn.epf.pointage.dao.PointageDAO()
                    .findBySeanceAndType(data.getValue().getId(), TypePointage.FIN).isPresent();
            return new javafx.beans.property.SimpleStringProperty(hasFin ? "✅ Pointé" : "—");
        });
    }

    private void chargerSeances() {
        SessionContext ctx = SessionContext.getInstance();
        List<SeancePlanifiee> seances;

        if (ctx.isProfesseur() && ctx.getUtilisateurConnecte().getProfesseurLie() != null) {
            Long profId = ctx.getUtilisateurConnecte().getProfesseurLie().getId();
            seances = seanceDAO.findSeancesDuJourParProfesseur(profId);
        } else {
            seances = seanceDAO.findSeancesDuJour();
        }

        tableSeances.setItems(FXCollections.observableArrayList(seances));
    }

    private void configurerSelection() {
        tableSeances.getSelectionModel().selectedItemProperty().addListener((obs, old, seance) -> {
            if (seance == null) {
                btnPointerDebut.setDisable(true);
                btnPointerFin.setDisable(true);
                return;
            }
            Professeur prof = getProfesseurConnecte();
            if (prof == null) { btnPointerDebut.setDisable(true); btnPointerFin.setDisable(true); return; }

            btnPointerDebut.setDisable(!pointageService.peutPointer(seance, prof, TypePointage.DEBUT));
            btnPointerFin.setDisable(!pointageService.peutPointer(seance, prof, TypePointage.FIN));
        });
    }

    @FXML
    public void handlePointerDebut() {
        pointer(TypePointage.DEBUT);
    }

    @FXML
    public void handlePointerFin() {
        pointer(TypePointage.FIN);
    }

    private void pointer(TypePointage type) {
        SeancePlanifiee seance = tableSeances.getSelectionModel().getSelectedItem();
        Professeur prof = getProfesseurConnecte();
        if (seance == null || prof == null) return;

        ResultatPointage resultat = pointageService.pointer(seance.getId(), prof.getId(), type);

        String msg;
        String style;
        switch (resultat) {
            case SUCCES -> { msg = "✅ Pointage enregistré avec succès !"; style = "status-ok"; }
            case EN_RETARD -> { msg = "⚠ Pointage enregistré — Retard signalé à la scolarité."; style = "status-retard"; }
            case TROP_TOT -> { msg = "⏰ Trop tôt ! La fenêtre de pointage n'est pas encore ouverte."; style = "status-absent"; }
            case PROF_INACTIF -> { msg = "🚫 Compte inactif. Contactez l'administration."; style = "status-absent"; }
            case DEJA_POINTE -> { msg = "⚠ Vous avez déjà pointé pour cette séance."; style = "status-retard"; }
            default -> { msg = "Erreur inconnue."; style = "status-absent"; }
        }

        lblResultat.setText(msg);
        lblResultat.getStyleClass().removeAll("status-ok", "status-retard", "status-absent");
        lblResultat.getStyleClass().add(style);

        // Rafraîchir le tableau
        chargerSeances();
        SessionContext.getInstance().rafraichirActivite();
    }

    private Professeur getProfesseurConnecte() {
        SessionContext ctx = SessionContext.getInstance();
        if (ctx.getUtilisateurConnecte() != null && ctx.getUtilisateurConnecte().getProfesseurLie() != null) {
            return ctx.getUtilisateurConnecte().getProfesseurLie();
        }
        // Pour ADMIN/SCOLARITE qui veulent tester : utiliser le professeur sélectionné
        SeancePlanifiee seance = tableSeances.getSelectionModel().getSelectedItem();
        if (seance != null && seance.getAssignation() != null) {
            return seance.getAssignation().getProfesseur();
        }
        return null;
    }
}
