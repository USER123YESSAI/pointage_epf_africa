package sn.epf.pointage.ui;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import sn.epf.pointage.dao.SalleDAO;
import sn.epf.pointage.model.Salle;

public class SalleFormController {
    @FXML private TextField fieldNom;
    @FXML private TextField fieldBatiment;
    @FXML private TextField fieldCapacite;
    @FXML private TextArea fieldEquipements;

    private final SalleDAO salleDAO = new SalleDAO();
    private boolean saved = false;

    @FXML
    public void handleSave() {
        String nom = fieldNom.getText() == null ? "" : fieldNom.getText().trim();
        if (nom.isEmpty()) {
            javafx.application.Platform.runLater(() -> {
                javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING,
                        "Le nom de la salle est requis.");
                a.setHeaderText("Données manquantes");
                a.showAndWait();
            });
            return;
        }

        try {
            Salle s = new Salle();
            s.setNom(nom);
            s.setBatiment(fieldBatiment.getText() == null ? "" : fieldBatiment.getText().trim());
            try { s.setCapacite(Integer.parseInt(fieldCapacite.getText().trim())); } catch (Exception ignored) {}
            s.setEquipements(fieldEquipements.getText() == null ? "" : fieldEquipements.getText().trim());

            salleDAO.save(s);
            this.saved = true;
            close();
        } catch (Exception e) {
            e.printStackTrace();
            javafx.application.Platform.runLater(() -> {
                javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR,
                        "Impossible d'enregistrer la salle : " + e.getMessage());
                a.setHeaderText("Erreur");
                a.showAndWait();
            });
        }
    }

    @FXML public void handleCancel() { close(); }

    private void close() { Stage s = (Stage) fieldNom.getScene().getWindow(); s.close(); }

    public boolean isSaved() { return saved; }
}
