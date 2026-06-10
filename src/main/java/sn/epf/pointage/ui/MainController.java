package sn.epf.pointage.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import sn.epf.pointage.config.SessionContext;
import sn.epf.pointage.model.enums.RoleUtilisateur;
import sn.epf.pointage.service.AuthService;

public class MainController {

    @FXML private StackPane contentArea;
    @FXML private Label labelUtilisateur;
    @FXML private Label labelRole;
    @FXML private Button btnDashboard;
    @FXML private Button btnProfesseurs;
    @FXML private Button btnCours;
    @FXML private Button btnSalles;
    @FXML private Button btnPlanning;
    @FXML private Button btnAssignations;
    @FXML private Button btnPointage;
    @FXML private Button btnRapports;

    private final AuthService authService = new AuthService();
    private Timeline sessionTimer;
    private Button activeButton;

    @FXML
    public void initialize() {
        configurerSession();
        configurerAccesParRole();
        demarrerTimerSession();
        showDashboard();
    }

    private void configurerSession() {
        SessionContext ctx = SessionContext.getInstance();
        if (ctx.getUtilisateurConnecte() != null) {
            String nom = ctx.getUtilisateurConnecte().getLogin();
            try {
                if (ctx.getUtilisateurConnecte().getProfesseurLie() != null) {
                    // Problème courant : LazyInitializationException si le proxy Professeur est détaché
                    // On protège l'affichage pour ne pas casser le chargement de la vue.
                    nom = ctx.getUtilisateurConnecte().getProfesseurLie().getNomComplet();
                }
            } catch (org.hibernate.LazyInitializationException ex) {
                // Fallback : garder le login
            }
            labelUtilisateur.setText(nom);
            labelRole.setText(ctx.getRole().toString());

        }
    }

  private void configurerAccesParRole() {
    SessionContext ctx = SessionContext.getInstance();
    RoleUtilisateur role = ctx.getRole();

    if (role == RoleUtilisateur.PROFESSEUR) {
        btnProfesseurs.setVisible(false);
        btnProfesseurs.setManaged(false);
        btnCours.setVisible(false);
        btnCours.setManaged(false);
        btnSalles.setVisible(false);
        btnSalles.setManaged(false);

        // Enlever le menu Assignations dans l'espace professeur
        btnAssignations.setVisible(false);
        btnAssignations.setManaged(false);
    }

    
}

    /** Déconnexion automatique après 30 min d'inactivité */
    private void demarrerTimerSession() {
        sessionTimer = new Timeline(new KeyFrame(Duration.minutes(1), e -> {
            if (SessionContext.getInstance().estSessionExpiree()) {
                sessionTimer.stop();
                javafx.application.Platform.runLater(this::deconnexionAutomatique);
            }
        }));
        sessionTimer.setCycleCount(Timeline.INDEFINITE);
        sessionTimer.play();
    }

    private void deconnexionAutomatique() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION,
                "Votre session a expiré. Vous allez être déconnecté.", ButtonType.OK);
        alert.setTitle("Session expirée");
        alert.showAndWait();
        handleDeconnexion();
    }

    @FXML public void showDashboard() {
        chargerVue("/fxml/dashboard.fxml", btnDashboard);
    }

    @FXML public void showProfesseurs() {
        SessionContext.getInstance().rafraichirActivite();
        chargerVue("/fxml/professeurs.fxml", btnProfesseurs);
    }

    @FXML public void showCours() {
        SessionContext.getInstance().rafraichirActivite();
        chargerVue("/fxml/cours.fxml", btnCours);
    }

    @FXML public void showSalles() {
        SessionContext.getInstance().rafraichirActivite();
        chargerVue("/fxml/salles.fxml", btnSalles);
    }

    @FXML public void showPlanning() {
        SessionContext.getInstance().rafraichirActivite();
        chargerVue("/fxml/planning.fxml", btnPlanning);
    }

    @FXML public void showAssignations() {
        SessionContext.getInstance().rafraichirActivite();
        chargerVue("/fxml/assignations.fxml", btnAssignations);
    }

    @FXML public void showPointage() {
        SessionContext.getInstance().rafraichirActivite();
        chargerVue("/fxml/pointage.fxml", btnPointage);
    }

    @FXML public void showRapports() {
        SessionContext.getInstance().rafraichirActivite();
        chargerVue("/fxml/rapports.fxml", btnRapports);
    }

    @FXML public void handleDeconnexion() {
        if (sessionTimer != null) sessionTimer.stop();
        authService.deconnecter();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(false);
            stage.setWidth(480);
            stage.setHeight(520);
            stage.centerOnScreen();
            stage.setTitle("EPF Africa — Connexion");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void chargerVue(String fxmlPath, Button bouton) {
        try {
            // Rafraîchir l'activité de session
            SessionContext.getInstance().rafraichirActivite();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent vue = loader.load();
            contentArea.getChildren().setAll(vue);

            // Mettre en surbrillance le bouton actif
            if (activeButton != null) {
                activeButton.getStyleClass().remove("menu-item-active");
            }
            if (bouton != null) {
                bouton.getStyleClass().add("menu-item-active");
                activeButton = bouton;
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement vue " + fxmlPath + " : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
