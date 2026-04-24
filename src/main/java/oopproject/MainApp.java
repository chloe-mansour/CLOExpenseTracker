package oopproject;

import javafx.application.Application;
import javafx.stage.Stage;
import oopproject.service.DatabaseService;
import oopproject.service.SettingsService;
import oopproject.ui.LoginView;
import oopproject.util.SceneManager;
import oopproject.util.ThemeManager;

// Main entry point of the application
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        DatabaseService.initializeDatabase();

        SettingsService settingsService = new SettingsService();
        ThemeManager.setDarkMode(settingsService.isDarkModeEnabled());

        SceneManager.initialize(primaryStage);
        SceneManager.switchTo(new LoginView().getScene(), "CLO - Login");

        primaryStage.setTitle("CLO - My Expense Tracker");
        primaryStage.show();

        SceneManager.fillScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}