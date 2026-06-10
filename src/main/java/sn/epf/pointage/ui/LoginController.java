package sn.epf.pointage.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import sn.epf.pointage.service.AuthService;

public class LoginController {

    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        // Initialiser le compte admin par défaut si nécessaire
        authService.initialiserCompteAdmin();
        // Appuyer Entrée dans le champ mdp déclenche la connexion
        passwordField.setOnAction(e -> handleLogin());
    }

    @FXML
    public void handleLogin() {
        String login = loginField.getText().trim();
        String password = passwordField.getText();

        if (login.isEmpty() || password.isEmpty()) {
            showError("Veuillez saisir votre identifiant et mot de passe.");
            return;
        }

        boolean ok = authService.authentifier(login, password);

        if (ok) {
            ouvrirApplicationPrincipale();
        } else {
            showError("Identifiant ou mot de passe incorrect.");
            passwordField.clear();
            passwordField.requestFocus();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void ouvrirApplicationPrincipale() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) loginField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("EPF Africa — Système de Pointage");
            stage.setMaximized(true);
        } catch (Exception e) {
            showError("Erreur lors du chargement de l'interface : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
