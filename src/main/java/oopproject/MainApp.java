package oopproject;

import javafx.application.Application;
import javafx.stage.Stage;
import oopproject.service.DatabaseService;
import oopproject.service.ExchangeRateService;
import oopproject.service.SettingsService;
import oopproject.ui.LoginView;
import oopproject.util.SceneManager;
import oopproject.util.ThemeManager;

// Main entry point of the application
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        DatabaseService.initializeDatabase();

        updateExchangeRateInBackground();

        SettingsService settingsService = new SettingsService();
        ThemeManager.setDarkMode(settingsService.isDarkModeEnabled());

        SceneManager.initialize(primaryStage);
        SceneManager.switchTo(new LoginView().getScene(), "CLO - Login");

        primaryStage.setTitle("CLO - My Expense Tracker");
        primaryStage.show();

        SceneManager.fillScreen();
    }

    // Updates the USD to LBP rate automatically without blocking the app
    private void updateExchangeRateInBackground() {
        Thread thread = new Thread(() -> {
            try {
                double latestRate = new ExchangeRateService().fetchUsdToLbpRate();
                new SettingsService().setUsdToLbpRate(latestRate);
            } catch (Exception ignored) {
                // If there is no internet, the app keeps the last saved rate.
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}