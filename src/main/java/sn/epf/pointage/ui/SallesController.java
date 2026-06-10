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
import sn.epf.pointage.dao.SalleDAO;
import sn.epf.pointage.model.Salle;

public class SallesController {

    @FXML private TableView<Salle> tableSalles;
    @FXML private TableColumn<Salle, String> colNom;
    @FXML private TableColumn<Salle, String> colBatiment;
    @FXML private TableColumn<Salle, Integer> colCapacite;
    @FXML private TableColumn<Salle, String> colEquipements;
    @FXML private Button btnAdd;
    @FXML private Button btnRefresh;

    private final SalleDAO salleDAO = new SalleDAO();

    @FXML
    public void initialize() {
        colNom.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getNom()));
        colBatiment.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getBatiment()));
        colCapacite.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getCapacite()));
        colEquipements.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getEquipements()));

        loadSalles();
        // restreindre création aux ADMIN/SCOLARITE
        try {
            if (sn.epf.pointage.config.SessionContext.getInstance().isProfesseur()) {
                btnAdd.setVisible(false);
                btnAdd.setManaged(false);
            }
        } catch (Exception ignored) {}
    }

    private void loadSalles() { tableSalles.setItems(FXCollections.observableArrayList(salleDAO.findAllSalles())); }

    @FXML public void handleRefresh() { loadSalles(); }

    @FXML public void handleAdd() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/salle_form.fxml"));
            Parent root = loader.load();
            SalleFormController ctrl = loader.getController();

            Stage dlg = new Stage(); dlg.initModality(Modality.APPLICATION_MODAL);
            dlg.initOwner(btnAdd.getScene().getWindow()); dlg.setTitle("Nouvelle salle"); dlg.setScene(new Scene(root)); dlg.showAndWait();

            if (ctrl != null && ctrl.isSaved()) loadSalles();
        } catch (Exception e) { e.printStackTrace(); }
    }
}
