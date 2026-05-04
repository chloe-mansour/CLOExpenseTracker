package oopproject.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import oopproject.service.ExchangeRateService;
import oopproject.service.SettingsService;
import oopproject.util.SceneManager;
import oopproject.util.ThemeManager;

// This page lets the user manage important app settings
public class SettingsView {

    private final Scene scene;
    private final SettingsService settingsService = new SettingsService();
    private final ExchangeRateService exchangeRateService = new ExchangeRateService();

    public SettingsView() {
        Label title = new Label("Settings");
        title.getStyleClass().add("section-title");

        VBox rateCard = createRateCard();

        // Only keep the dark mode setting because it actually affects the whole app
        CheckBox darkModeCheck = new CheckBox("Enable dark mode");
        darkModeCheck.setSelected(ThemeManager.isDarkMode());
        darkModeCheck.getStyleClass().add("settings-check");

        VBox preferencesBox = new VBox(16, darkModeCheck);
        preferencesBox.setAlignment(Pos.CENTER_LEFT);

        Button saveButton = new Button("Save Settings");
        saveButton.getStyleClass().add("primary-button");

        Button backButton = new Button("Back to Dashboard");
        backButton.getStyleClass().add("secondary-button");

        HBox buttonsBox = new HBox(12, saveButton, backButton);
        buttonsBox.setAlignment(Pos.CENTER_LEFT);

        Label messageLabel = new Label();
        messageLabel.getStyleClass().add("message-label");
        messageLabel.setMinHeight(24);
        messageLabel.setWrapText(true);

        VBox rightSide = new VBox(20, preferencesBox, buttonsBox, messageLabel);
        rightSide.setAlignment(Pos.CENTER_LEFT);

        HBox settingsCard = new HBox(32, rateCard, rightSide);
        settingsCard.getStyleClass().add("info-card");
        settingsCard.setPadding(new Insets(28));
        settingsCard.setMaxWidth(780);
        settingsCard.setAlignment(Pos.CENTER_LEFT);

        // Save only the useful setting
        saveButton.setOnAction(e -> {
            boolean darkMode = darkModeCheck.isSelected();

            settingsService.setDarkModeEnabled(darkMode);
            ThemeManager.setDarkMode(darkMode);

            SceneManager.switchTo(new SettingsView().getScene(), "CLO - Settings");
        });

        backButton.setOnAction(e ->
                SceneManager.switchTo(new DashboardView().getScene(), "CLO - Dashboard")
        );

        VBox root = new VBox(24, title, settingsCard);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.getStyleClass().add("root-pane");

        scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource(ThemeManager.getThemeStylesheet()).toExternalForm());
    }

    private VBox createRateCard() {
        Label rateTitle = new Label("Daily USD to LBP Rate");
        rateTitle.getStyleClass().add("card-title");

        Label rateValue = new Label(formatRate(settingsService.getUsdToLbpRate()));
        rateValue.getStyleClass().add("settings-rate-value");

        Label rateHelper = new Label("Updates automatically when internet is available.");
        rateHelper.getStyleClass().add("helper-text");
        rateHelper.setWrapText(true);

        VBox rateCard = new VBox(10, rateTitle, rateValue, rateHelper);
        rateCard.getStyleClass().add("settings-rate-card");
        rateCard.setPadding(new Insets(22));
        rateCard.setPrefWidth(320);
        rateCard.setMaxWidth(320);

        updateRateLabelInBackground(rateValue);

        return rateCard;
    }

    private void updateRateLabelInBackground(Label rateValue) {
        Thread thread = new Thread(() -> {
            try {
                double latestRate = exchangeRateService.fetchUsdToLbpRate();
                settingsService.setUsdToLbpRate(latestRate);

                Platform.runLater(() ->
                        rateValue.setText(formatRate(latestRate))
                );

            } catch (Exception ignored) {
                // If internet is unavailable, the app keeps the last saved rate
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    private String formatRate(double rate) {
        return "1 USD = " + String.format("%,.0f", rate) + " LBP";
    }

    public Scene getScene() {
        return scene;
    }
}