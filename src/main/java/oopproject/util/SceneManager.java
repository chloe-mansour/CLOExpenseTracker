package oopproject.util;

import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

// Manages scene switching and keeps the app filling the screen
public class SceneManager {

    private static Stage primaryStage;

    private SceneManager() {
    }

    public static void initialize(Stage stage) {
        primaryStage = stage;
        primaryStage.setResizable(true);
    }

    public static void switchTo(Scene scene, String title) {
        if (primaryStage != null) {
            primaryStage.setTitle(title);
            primaryStage.setScene(scene);

            Platform.runLater(SceneManager::fillScreen);
        }
    }

    // Forces the window to fill the available screen area
    public static void fillScreen() {
        if (primaryStage == null) {
            return;
        }

        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();

        primaryStage.setX(bounds.getMinX());
        primaryStage.setY(bounds.getMinY());
        primaryStage.setWidth(bounds.getWidth());
        primaryStage.setHeight(bounds.getHeight());
        primaryStage.setMaximized(true);
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}