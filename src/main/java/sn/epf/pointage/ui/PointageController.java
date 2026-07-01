package sn.epf.pointage.ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import sn.epf.pointage.config.SessionContext;
import sn.epf.pointage.dao.PointageDAO;
import sn.epf.pointage.dao.SeanceDAO;
import sn.epf.pointage.dao.ProfesseurDAO;
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
    private final PointageDAO pointageDAO = new PointageDAO();

    @FXML
    public void initialize() {
        lblDate.setText("Aujourd'hui : " + LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", java.util.Locale.FRENCH)));
        configurerColonnes();
        chargerSeances();
        configurerSelection();
    }

    private void configurerColonnes() {
        DateTimeFormatter heureFmt = DateTimeFormatter.ofPattern("HH:mm");

        colHeure.setCellValueFactory(data -> {
            String h = data.getValue().getDateHeure() != null
                    ? data.getValue().getDateHeure().toLocalTime().format(heureFmt) : "";
            return new javafx.beans.property.SimpleStringProperty(h);
        });

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

        colStatut.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getStatut().toString()));
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(statut);
                badge.getStyleClass().add(switch (statut) {
                    case "REALISEE" -> "badge-ok";
                    case "ANNULEE"  -> "badge-absent";
                    default         -> "badge-retard";
                });
                setGraphic(badge);
                setText(null);
            }
        });

        colDebut.setCellValueFactory(data -> {
            try {
                String couleur = pointageService.getCouleurStatut(data.getValue());
                String txt = switch (couleur) {
                    case "vert"   -> "✅ Pointé";
                    case "orange" -> "⚠ Retard";
                    case "rouge"  -> "❌ Absent";
                    default       -> "— En attente";
                };
                return new javafx.beans.property.SimpleStringProperty(txt);
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("—");
            }
        });

        colFin.setCellValueFactory(data -> {
            try {
                boolean hasFin = pointageDAO
                        .findBySeanceAndType(data.getValue().getId(), TypePointage.FIN).isPresent();
                return new javafx.beans.property.SimpleStringProperty(hasFin ? "✅ Pointé" : "—");
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("—");
            }
        });
    }

    private void chargerSeances() {
        SessionContext ctx = SessionContext.getInstance();
        List<SeancePlanifiee> seances;

        if (ctx.isProfesseur() && ctx.getUtilisateurConnecte() != null
                && ctx.getUtilisateurConnecte().getProfesseurLie() != null) {
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
            if (prof == null) {
                btnPointerDebut.setDisable(true);
                btnPointerFin.setDisable(true);
                return;
            }
            btnPointerDebut.setDisable(!pointageService.peutPointer(seance, prof, TypePointage.DEBUT));
            btnPointerFin.setDisable(!pointageService.peutPointer(seance, prof, TypePointage.FIN));
        });
    }

    @FXML public void handlePointerDebut() { pointer(TypePointage.DEBUT); }
    @FXML public void handlePointerFin()   { pointer(TypePointage.FIN); }

    private void pointer(TypePointage type) {
        SeancePlanifiee seance = tableSeances.getSelectionModel().getSelectedItem();
        Professeur prof = getProfesseurConnecte();
        if (seance == null || prof == null) return;

        ResultatPointage resultat = pointageService.pointer(seance.getId(), prof.getId(), type);

        String msg;
        String style;
        switch (resultat) {
            case SUCCES        -> { msg = "✅ Pointage enregistré avec succès !";                     style = "status-ok"; }
            case EN_RETARD     -> { msg = "⚠ Pointage enregistré — Retard signalé à la scolarité."; style = "status-retard"; }
            case TROP_TOT      -> { msg = "⏰ Trop tôt ! Fenêtre pas encore ouverte (-15 min).";    style = "status-absent"; }
            case PROF_INACTIF  -> { msg = "🚫 Compte inactif. Contactez l'administration.";          style = "status-absent"; }
            case DEJA_POINTE   -> { msg = "⚠ Déjà pointé pour cette séance.";                      style = "status-retard"; }
            case SEANCE_INVALIDE -> { msg = "⏰ Trop tard ! Fenêtre fermée (+5 min dépassées).";    style = "status-absent"; }
            default            -> { msg = "Erreur inconnue.";                                         style = "status-absent"; }
        }

        lblResultat.setText(msg);
        lblResultat.getStyleClass().removeAll("status-ok", "status-retard", "status-absent");
        lblResultat.getStyleClass().add(style);

        chargerSeances();
        SessionContext.getInstance().rafraichirActivite();
    }

    private Professeur getProfesseurConnecte() {
        SessionContext ctx = SessionContext.getInstance();
        // Si professeur connecté → son propre profil
        if (ctx.getUtilisateurConnecte() != null
                && ctx.getUtilisateurConnecte().getProfesseurLie() != null) {
            return ctx.getUtilisateurConnecte().getProfesseurLie();
        }
        // Pour ADMIN/SCOLARITE testant : professeur de la séance sélectionnée
        SeancePlanifiee seance = tableSeances.getSelectionModel().getSelectedItem();
        if (seance != null && seance.getAssignation() != null) {
            try { return seance.getAssignation().getProfesseur(); }
            catch (Exception e) { return null; }
        }
        return null;
    }
}
