package sn.epf.pointage;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sn.epf.pointage.config.HibernateConfig;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialiser Hibernate
        HibernateConfig.getSessionFactory();

        // Charger la vue login
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 480, 520);
        primaryStage.setScene(scene);
        primaryStage.setTitle("EPF Africa — Système de Pointage");
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    @Override
    public void stop() {
        HibernateConfig.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
