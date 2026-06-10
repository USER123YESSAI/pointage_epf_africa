package sn.epf.pointage.ui;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import sn.epf.pointage.dao.CoursDAO;
import sn.epf.pointage.model.Cours;

public class CoursFormController {
    @FXML private TextField fieldCode;
    @FXML private TextField fieldIntitule;
    @FXML private TextField fieldFiliere;
    @FXML private TextField fieldNiveau;
    @FXML private TextField fieldVolume;
    @FXML private TextField fieldSemestre;
    @FXML private TextArea fieldDescription;

    private final CoursDAO coursDAO = new CoursDAO();
    private boolean saved = false;

    @FXML
    public void handleSave() {
        // validations simples
        String code = fieldCode.getText() == null ? "" : fieldCode.getText().trim();
        String intitule = fieldIntitule.getText() == null ? "" : fieldIntitule.getText().trim();
        if (code.isEmpty() || intitule.isEmpty()) {
            javafx.application.Platform.runLater(() -> {
                javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING,
                        "Veuillez renseigner au moins le code et l'intitulé du cours.");
                a.setHeaderText("Données manquantes");
                a.showAndWait();
            });
            return;
        }

        try {
            Cours c = new Cours();
            c.setCode(code);
            c.setIntitule(intitule);
            c.setFiliere(fieldFiliere.getText() == null ? "" : fieldFiliere.getText().trim());
            c.setNiveauEtude(fieldNiveau.getText() == null ? "" : fieldNiveau.getText().trim());
            try { c.setVolumeHoraireTotal(Integer.parseInt(fieldVolume.getText().trim())); } catch (Exception ignored) {}
            c.setSemestre(fieldSemestre.getText() == null ? "" : fieldSemestre.getText().trim());
            c.setDescription(fieldDescription.getText() == null ? "" : fieldDescription.getText().trim());

            coursDAO.save(c);
            this.saved = true;
            close();
        } catch (Exception e) {
            e.printStackTrace();
            javafx.application.Platform.runLater(() -> {
                javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR,
                        "Impossible d'enregistrer le cours : " + e.getMessage());
                a.setHeaderText("Erreur");
                a.showAndWait();
            });
        }
    }

    @FXML public void handleCancel() { close(); }

    private void close() {
        Stage s = (Stage) fieldCode.getScene().getWindow();
        s.close();
    }

    public boolean isSaved() { return saved; }
}
