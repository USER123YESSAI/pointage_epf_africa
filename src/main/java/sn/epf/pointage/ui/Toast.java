package sn.epf.pointage.ui;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

public final class Toast {
    private Toast() {}

    public static void show(Node ownerNode, String message) {
        if (ownerNode == null || ownerNode.getScene() == null) return;
        Parent root = ownerNode.getScene().getRoot();
        if (!(root instanceof Pane)) return;
        Pane pane = (Pane) root;

        javafx.application.Platform.runLater(() -> {
            Label toast = new Label(message);
            toast.setStyle("-fx-background-color: rgba(0,0,0,0.75); -fx-text-fill: white; -fx-padding: 8 12; -fx-background-radius: 6;");
            toast.setOpacity(0);
            toast.setManaged(false);

            // Position top center
            double x = (pane.getWidth() - toast.prefWidth(-1)) / 2;
            toast.setTranslateX(x);
            toast.setTranslateY(12);

            pane.getChildren().add(toast);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), toast);
            fadeIn.setFromValue(0); fadeIn.setToValue(1);
            PauseTransition wait = new PauseTransition(Duration.seconds(2));
            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), toast);
            fadeOut.setFromValue(1); fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> pane.getChildren().remove(toast));
            SequentialTransition seq = new SequentialTransition(fadeIn, wait, fadeOut);
            seq.play();
        });
    }
}
