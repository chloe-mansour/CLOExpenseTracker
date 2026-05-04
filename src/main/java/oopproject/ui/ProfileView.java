package oopproject.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import oopproject.model.User;
import oopproject.service.ProfileService;
import oopproject.util.SceneManager;
import oopproject.util.SessionManager;
import oopproject.util.ThemeManager;

import java.time.LocalDate;
import java.time.Period;

// Lets the user view and edit profile information
public class ProfileView {

    private final Scene scene;
    private final ProfileService profileService = new ProfileService();

    public ProfileView() {
        User currentUser = SessionManager.getCurrentUser();

        Label title = new Label("My Profile");
        title.getStyleClass().add("section-title");

        // Pre-fills editable fields with current user data
        TextField studyFieldField = new TextField();
        studyFieldField.setText(currentUser != null && currentUser.getStudyField() != null ? currentUser.getStudyField() : "");

        TextField jobTitleField = new TextField();
        jobTitleField.setText(currentUser != null && currentUser.getJobTitle() != null ? currentUser.getJobTitle() : "");

        DatePicker dateOfBirthPicker = new DatePicker();
        dateOfBirthPicker.setValue(currentUser != null ? currentUser.getDateOfBirth() : null);

        CheckBox employedCheckBox = new CheckBox("Currently working");
        employedCheckBox.setSelected(currentUser != null && currentUser.isEmployed());
        employedCheckBox.getStyleClass().add("settings-check");

        VBox studyFieldBox = createField("Study Field", studyFieldField);
        VBox dobBox = createDateField("Date of Birth", dateOfBirthPicker);
        VBox jobTitleBox = createField("Job Title", jobTitleField);

        // Shows job title only if the user is currently working
        jobTitleBox.setVisible(employedCheckBox.isSelected());
        jobTitleBox.setManaged(employedCheckBox.isSelected());

        employedCheckBox.selectedProperty().addListener((obs, oldValue, newValue) -> {
            jobTitleBox.setVisible(newValue);
            jobTitleBox.setManaged(newValue);

            if (!newValue) {
                jobTitleField.clear();
            }
        });

        HBox nameRow = createInfoRow(
                "Full Name:",
                currentUser != null ? currentUser.getFullName() : "Unknown"
        );

        HBox emailRow = createInfoRow(
                "Email:",
                currentUser != null ? currentUser.getEmail() : "Unknown"
        );

        HBox ageRow = createInfoRow(
                "Age:",
                calculateAgeText(currentUser != null ? currentUser.getDateOfBirth() : null)
        );

        Button saveButton = new Button("Save Changes");
        saveButton.getStyleClass().add("primary-button");

        Button backButton = new Button("Back to Dashboard");
        backButton.getStyleClass().add("secondary-button");

        Label messageLabel = new Label();
        messageLabel.getStyleClass().add("message-label");
        messageLabel.setWrapText(true);

        // Saves updated editable profile data
        saveButton.setOnAction(e -> {
            if (currentUser == null) {
                messageLabel.setText("No active user session.");
                return;
            }

            LocalDate dob = dateOfBirthPicker.getValue();
            String studyField = studyFieldField.getText().trim();
            boolean employed = employedCheckBox.isSelected();
            String jobTitle = jobTitleField.getText().trim();

            if (dob != null && dob.isAfter(LocalDate.now())) {
                messageLabel.setText("Date of birth cannot be in the future.");
                return;
            }

            if (employed && jobTitle.isEmpty()) {
                messageLabel.setText("Job title is required if you are currently working.");
                return;
            }

            boolean success = profileService.updateProfile(
                    currentUser.getId(),
                    dob,
                    studyField,
                    employed,
                    employed ? jobTitle : ""
            );

            if (success) {
                // Reloads the session user so the profile shows the latest data
                User refreshedUser = profileService.getUserById(currentUser.getId());
                SessionManager.setCurrentUser(refreshedUser);
                SceneManager.switchTo(new ProfileView().getScene(), "CLO - Profile");
            } else {
                messageLabel.setText("Could not update profile.");
            }
        });

        backButton.setOnAction(e ->
                SceneManager.switchTo(new DashboardView().getScene(), "CLO - Dashboard")
        );

        VBox profileCard = new VBox(
                12,
                nameRow,
                emailRow,
                ageRow,
                dobBox,
                studyFieldBox,
                employedCheckBox,
                jobTitleBox,
                saveButton,
                backButton,
                messageLabel
        );

        profileCard.getStyleClass().add("info-card");
        profileCard.setPadding(new Insets(24));
        profileCard.setMaxWidth(450);
        profileCard.setAlignment(Pos.CENTER_LEFT);

        VBox root = new VBox(20, title, profileCard);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.getStyleClass().add("root-pane");

        scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource(ThemeManager.getThemeStylesheet()).toExternalForm());
    }

    // Creates a profile info row where only the title is bold
    private HBox createInfoRow(String titleText, String valueText) {
        Label title = new Label(titleText + " ");
        title.getStyleClass().add("side-card-value");
        title.setStyle("-fx-font-weight: bold;");

        Label value = new Label(valueText);
        value.getStyleClass().add("side-card-value");

        HBox row = new HBox(4, title, value);
        row.setAlignment(Pos.CENTER_LEFT);

        return row;
    }

    // Creates a labeled text field block
    private VBox createField(String labelText, TextField field) {
        Label label = new Label(labelText);
        label.getStyleClass().add("floating-label");

        field.getStyleClass().add("floating-input");

        VBox fieldBox = new VBox(2, label, field);
        fieldBox.getStyleClass().add("floating-field-box");

        return new VBox(4, fieldBox);
    }

    // Creates a labeled date picker block
    private VBox createDateField(String labelText, DatePicker picker) {
        Label label = new Label(labelText);
        label.getStyleClass().add("floating-label");

        picker.getStyleClass().add("floating-input");

        VBox fieldBox = new VBox(2, label, picker);
        fieldBox.getStyleClass().add("floating-field-box");

        return new VBox(4, fieldBox);
    }

    // Calculates age display from date of birth
    private String calculateAgeText(LocalDate dob) {
        if (dob == null) {
            return "Not available";
        }

        return String.valueOf(Period.between(dob, LocalDate.now()).getYears());
    }

    public Scene getScene() {
        return scene;
    }
}