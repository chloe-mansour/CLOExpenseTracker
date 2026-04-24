package oopproject.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import oopproject.model.User;
import oopproject.service.AuthService;
import oopproject.util.SceneManager;
import oopproject.util.SessionManager;
import oopproject.util.ThemeManager;

import java.time.LocalDate;

// Signup screen with validation, date picker, and password visibility toggle
public class SignupView {

    private final Scene scene;
    private final AuthService authService = new AuthService();

    public SignupView() {
        Label title = new Label("Create Your Account");
        title.getStyleClass().add("section-title");

        // User input fields
        TextField fullNameField = new TextField();
        TextField emailField = new TextField();

        PasswordField passwordField = new PasswordField();
        TextField visiblePasswordField = new TextField();

        // Keeps hidden and visible password fields synchronized
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());
        visiblePasswordField.setVisible(false);
        visiblePasswordField.setManaged(false);

        TextField studyFieldField = new TextField();
        TextField jobTitleField = new TextField();

        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Select your date of birth");
        datePicker.getStyleClass().add("floating-input");

        // Reusable form sections
        VBox fullNameBox = createFloatingField("Full Name", fullNameField, "Enter your full name");
        VBox emailBox = createFloatingField("Email", emailField, "Use a valid email");
        VBox passwordBox = createPasswordField(
                "Password",
                passwordField,
                visiblePasswordField,
                "8+ chars, upper, lower, number, special"
        );
        VBox dobBox = createDateField("Date of Birth", datePicker, "Optional");
        VBox studyBox = createFloatingField("Study Field", studyFieldField, "Optional");
        VBox jobBox = createFloatingField("Job Title", jobTitleField, "Required if working");

        CheckBox workingCheck = new CheckBox("Currently working");

        // Job title is only visible when the user is working
        jobBox.setVisible(false);
        jobBox.setManaged(false);

        workingCheck.selectedProperty().addListener((obs, oldV, newV) -> {
            jobBox.setVisible(newV);
            jobBox.setManaged(newV);

            if (!newV) {
                jobTitleField.clear();
            }
        });

        Button createBtn = new Button("Create Account");
        createBtn.getStyleClass().add("primary-button");

        // Keyboard navigation between fields using Enter
        fullNameField.setOnAction(e -> emailField.requestFocus());

        emailField.setOnAction(e -> passwordField.requestFocus());

        passwordField.setOnAction(e -> datePicker.requestFocus());
        visiblePasswordField.setOnAction(e -> datePicker.requestFocus());

        datePicker.setOnAction(e -> studyFieldField.requestFocus());

        studyFieldField.setOnAction(e -> {
            if (workingCheck.isSelected()) {
                jobTitleField.requestFocus();
            } else {
                createBtn.fire();
            }
        });

        jobTitleField.setOnAction(e -> createBtn.fire());

        Button backBtn = new Button("Back to Login");
        backBtn.getStyleClass().add("secondary-button");

        // Validation message shown near the main required fields
        Label message = new Label();
        message.getStyleClass().add("message-label");
        message.setWrapText(true);
        message.setMaxWidth(430);
        message.setMinHeight(24);

        createBtn.setOnAction(e -> {
            String fullName = fullNameField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.isVisible()
                    ? passwordField.getText().trim()
                    : visiblePasswordField.getText().trim();

            boolean working = workingCheck.isSelected();
            String job = jobTitleField.getText().trim();
            LocalDate dob = datePicker.getValue();

            // Required field validation
            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                message.setText("Please fill in all required fields.");
                return;
            }

            // Email format validation
            if (!isValidEmail(email)) {
                message.setText("Invalid email format. Example: user@gmail.com");
                return;
            }

            // Password strength validation
            if (!isStrongPassword(password)) {
                message.setText("Password must include upper, lower, number, and special character.");
                return;
            }

            // Date validation
            if (dob != null && dob.isAfter(LocalDate.now())) {
                message.setText("Date of birth cannot be in the future.");
                return;
            }

            // Conditional job title validation
            if (working && job.isEmpty()) {
                message.setText("Job title is required if you are working.");
                return;
            }

            // Prevents duplicate accounts
            if (authService.emailExists(email)) {
                message.setText("Email already used.");
                return;
            }

            User user = new User(
                    0,
                    fullName,
                    email,
                    password,
                    dob,
                    studyFieldField.getText().trim(),
                    working,
                    working ? job : ""
            );

            boolean success = authService.registerUser(user);

            if (success) {
                User loggedUser = authService.loginUser(email, password);
                SessionManager.setCurrentUser(loggedUser);
                SessionManager.setFirstLoginAfterSignup(true);
                SceneManager.switchTo(new DashboardView().getScene(), "CLO - Dashboard");
            } else {
                message.setText("Signup failed.");
            }
        });

        backBtn.setOnAction(e ->
                SceneManager.switchTo(new LoginView().getScene(), "CLO - Login")
        );

        VBox root = new VBox(
                12,
                title,
                fullNameBox,
                emailBox,
                passwordBox,
                message,
                dobBox,
                studyBox,
                workingCheck,
                jobBox,
                createBtn,
                backBtn
        );

        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(28));
        root.getStyleClass().add("root-pane");

        scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource(ThemeManager.getThemeStylesheet()).toExternalForm());
    }

    // Creates a reusable floating text field block
    private VBox createFloatingField(String labelText, TextField field, String helper) {
        Label label = new Label(labelText);
        label.getStyleClass().add("floating-label");

        field.getStyleClass().add("floating-input");

        VBox box = new VBox(2, label, field);
        box.getStyleClass().add("floating-field-box");

        Label help = new Label(helper);
        help.getStyleClass().add("helper-text");

        return new VBox(4, box, help);
    }

    // Creates a reusable date picker block
    private VBox createDateField(String labelText, DatePicker picker, String helper) {
        Label label = new Label(labelText);
        label.getStyleClass().add("floating-label");

        VBox box = new VBox(2, label, picker);
        box.getStyleClass().add("floating-field-box");

        Label help = new Label(helper);
        help.getStyleClass().add("helper-text");

        return new VBox(4, box, help);
    }

    // Creates a password field with icon-based visibility control
    private VBox createPasswordField(String labelText, PasswordField hidden, TextField visible, String helper) {
        Label label = new Label(labelText);
        label.getStyleClass().add("floating-label");

        hidden.getStyleClass().add("floating-input");
        visible.getStyleClass().add("floating-input");

        Image eyeOpen = new Image(getClass().getResourceAsStream("/icons/eye.png"));
        Image eyeClosed = new Image(getClass().getResourceAsStream("/icons/eye-off.png"));

        ImageView icon = new ImageView(eyeClosed);
        icon.setFitWidth(18);
        icon.setFitHeight(18);

        Button toggle = new Button();
        toggle.setGraphic(icon);
        toggle.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 0;");
        toggle.setCursor(Cursor.HAND);
        toggle.setFocusTraversable(false);
        toggle.setPrefWidth(35);

        toggle.setOnAction(e -> {
            if (hidden.isVisible()) {
                hidden.setVisible(false);
                hidden.setManaged(false);

                visible.setVisible(true);
                visible.setManaged(true);

                icon.setImage(eyeOpen);
            } else {
                visible.setVisible(false);
                visible.setManaged(false);

                hidden.setVisible(true);
                hidden.setManaged(true);

                icon.setImage(eyeClosed);
            }
        });

        VBox fieldStack = new VBox(2, label, hidden, visible);

        HBox wrapper = new HBox(10, fieldStack, toggle);
        wrapper.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(2, wrapper);
        box.getStyleClass().add("floating-field-box");

        Label help = new Label(helper);
        help.getStyleClass().add("helper-text");

        return new VBox(4, box, help);
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private boolean isStrongPassword(String password) {
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$");
    }

    public Scene getScene() {
        return scene;
    }
}