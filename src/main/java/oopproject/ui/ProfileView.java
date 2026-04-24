package oopproject.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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

        // Pre-fills fields with current user data
        TextField studyFieldField = new TextField();
        studyFieldField.setText(currentUser != null && currentUser.getStudyField() != null ? currentUser.getStudyField() : "");

        TextField jobTitleField = new TextField();
        jobTitleField.setText(currentUser != null && currentUser.getJobTitle() != null ? currentUser.getJobTitle() : "");

        DatePicker dateOfBirthPicker = new DatePicker();
        dateOfBirthPicker.setValue(currentUser != null ? currentUser.getDateOfBirth() : null);

        CheckBox employedCheckBox = new CheckBox("Currently working");
        employedCheckBox.setSelected(currentUser != null && currentUser.isEmployed());

        VBox studyFieldBox = createField("Study Field", studyFieldField);
        VBox dobBox = createDateField("Date of Birth", dateOfBirthPicker);
        VBox jobTitleBox = createField("Job Title", jobTitleField);

        // Shows job title only if working is checked
        jobTitleBox.setVisible(employedCheckBox.isSelected());
        jobTitleBox.setManaged(employedCheckBox.isSelected());

        employedCheckBox.selectedProperty().addListener((obs, oldValue, newValue) -> {
            jobTitleBox.setVisible(newValue);
            jobTitleBox.setManaged(newValue);

            if (!newValue) {
                jobTitleField.clear();
            }
        });

        Label fullNameLabel = new Label("Full Name: " + (currentUser != null ? currentUser.getFullName() : "Unknown"));
        fullNameLabel.getStyleClass().add("side-card-value");

        Label emailLabel = new Label("Email: " + (currentUser != null ? currentUser.getEmail() : "Unknown"));
        emailLabel.getStyleClass().add("side-card-value");

        Label ageLabel = new Label("Age: " + calculateAgeText(currentUser != null ? currentUser.getDateOfBirth() : null));
        ageLabel.getStyleClass().add("side-card-value");

        Button saveButton = new Button("Save Changes");
        saveButton.getStyleClass().add("primary-button");

        Button backButton = new Button("Back to Dashboard");
        backButton.getStyleClass().add("secondary-button");

        Label messageLabel = new Label();
        messageLabel.getStyleClass().add("message-label");

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
                // Refreshes the session user after profile update
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
                fullNameLabel,
                emailLabel,
                ageLabel,
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

        VBox root = new VBox(20, title, profileCard);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.getStyleClass().add("root-pane");

        scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource(ThemeManager.getThemeStylesheet()).toExternalForm());
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