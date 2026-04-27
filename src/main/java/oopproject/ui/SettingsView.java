package oopproject.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import oopproject.service.SettingsService;
import oopproject.util.SceneManager;
import oopproject.util.ThemeManager;

// Lets the user update app settings such as theme and currency rate
public class SettingsView {

    private final Scene scene;
    private final SettingsService settingsService = new SettingsService();

    public SettingsView() {
        Label title = new Label("Settings");
        title.getStyleClass().add("section-title");

        TextField lbpRateField = new TextField();
        lbpRateField.setText(String.valueOf(settingsService.getUsdToLbpRate()));
        VBox rateBox = createField("USD to LBP Rate", lbpRateField, "Example: 89500");

        CheckBox darkModeCheck = new CheckBox("Enable dark mode");
        darkModeCheck.setSelected(ThemeManager.isDarkMode());
        darkModeCheck.getStyleClass().add("settings-check");

        CheckBox quotesPlaceholder = new CheckBox("Show motivational quotes");
        quotesPlaceholder.setSelected(true);
        quotesPlaceholder.getStyleClass().add("settings-check");

        Button saveButton = new Button("Save Settings");
        saveButton.getStyleClass().add("primary-button");

        Button backButton = new Button("Back to Dashboard");
        backButton.getStyleClass().add("secondary-button");

        Label messageLabel = new Label();
        messageLabel.getStyleClass().add("message-label");
        messageLabel.setMinHeight(24);

        saveButton.setOnAction(e -> {
            String rateText = lbpRateField.getText().trim();

            if (rateText.isEmpty()) {
                messageLabel.setText("Please enter a currency rate.");
                return;
            }

            try {
                double rate = Double.parseDouble(rateText);

                if (rate <= 0) {
                    messageLabel.setText("Rate must be greater than 0.");
                    return;
                }

                boolean darkMode = darkModeCheck.isSelected();

                settingsService.setUsdToLbpRate(rate);
                settingsService.setDarkModeEnabled(darkMode);
                ThemeManager.setDarkMode(darkMode);

                SceneManager.switchTo(new SettingsView().getScene(), "CLO - Settings");

            } catch (NumberFormatException ex) {
                messageLabel.setText("Rate must be a valid number.");
            }
        });

        backButton.setOnAction(e ->
                SceneManager.switchTo(new DashboardView().getScene(), "CLO - Dashboard")
        );

        VBox settingsCard = new VBox(
                14,
                rateBox,
                darkModeCheck,
                quotesPlaceholder,
                saveButton,
                backButton,
                messageLabel
        );

        settingsCard.getStyleClass().add("info-card");
        settingsCard.setPadding(new Insets(24));
        settingsCard.setMaxWidth(450);

        VBox root = new VBox(20, title, settingsCard);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.getStyleClass().add("root-pane");

        scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource(ThemeManager.getThemeStylesheet()).toExternalForm());
    }

    // Creates a reusable settings input field
    private VBox createField(String labelText, TextField field, String helperText) {
        Label label = new Label(labelText);
        label.getStyleClass().add("floating-label");

        field.getStyleClass().add("floating-input");

        VBox fieldBox = new VBox(2, label, field);
        fieldBox.getStyleClass().add("floating-field-box");

        Label helper = new Label(helperText);
        helper.getStyleClass().add("helper-text");

        return new VBox(4, fieldBox, helper);
    }

    public Scene getScene() {
        return scene;
    }
}