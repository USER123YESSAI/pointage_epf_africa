package sn.epf.pointage.ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sn.epf.pointage.dao.CoursDAO;
import sn.epf.pointage.model.Cours;

public class CoursController {

    @FXML private TableView<Cours> tableCours;
    @FXML private TableColumn<Cours, String> colCode;
    @FXML private TableColumn<Cours, String> colIntitule;
    @FXML private TableColumn<Cours, String> colFiliere;
    @FXML private TableColumn<Cours, String> colNiveau;
    @FXML private Button btnAdd;
    @FXML private Button btnRefresh;

    private final CoursDAO coursDAO = new CoursDAO();

    @FXML
    public void initialize() {
        colCode.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getCode()));
        colIntitule.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getIntitule()));
        colFiliere.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getFiliere()));
        colNiveau.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getNiveauEtude()));

        loadCours();
        // restreindre création aux ADMIN/SCOLARITE
        try {
            if (sn.epf.pointage.config.SessionContext.getInstance().isProfesseur()) {
                btnAdd.setVisible(false);
                btnAdd.setManaged(false);
            }
        } catch (Exception ignored) {}
    }

    private void loadCours() {
        tableCours.setItems(FXCollections.observableArrayList(coursDAO.findAllCours()));
    }

    @FXML
    public void handleRefresh() { loadCours(); }

    @FXML
    public void handleAdd() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/cours_form.fxml"));
            Parent root = loader.load();
            CoursFormController ctrl = loader.getController();

            Stage dlg = new Stage();
            dlg.initModality(Modality.APPLICATION_MODAL);
            dlg.initOwner(btnAdd.getScene().getWindow());
            dlg.setTitle("Nouveau cours");
            dlg.setScene(new Scene(root));
            dlg.showAndWait();

            if (ctrl != null && ctrl.isSaved()) loadCours();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
