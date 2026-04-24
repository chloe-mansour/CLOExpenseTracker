package oopproject.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import oopproject.model.User;
import oopproject.service.AuthService;
import oopproject.util.SceneManager;
import oopproject.util.SessionManager;
import oopproject.util.ThemeManager;

// Login screen with validation and image-based password visibility toggle
public class LoginView {

    private final Scene scene;
    private final AuthService authService = new AuthService();

    public LoginView() {

        Label title = new Label("CLO");
        title.getStyleClass().add("app-title");

        Label subtitle = new Label("My Expense Tracker");
        subtitle.getStyleClass().add("subtitle");

        Label loginLabel = new Label("Login");
        loginLabel.getStyleClass().add("section-title");

        TextField emailField = new TextField();
        VBox emailBox = createFloatingField("Email", emailField, "Enter your registered email");

        PasswordField passwordField = new PasswordField();
        TextField visiblePasswordField = new TextField();

        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());
        visiblePasswordField.setVisible(false);
        visiblePasswordField.setManaged(false);

        VBox passwordBox = createPasswordField(
                "Password",
                passwordField,
                visiblePasswordField,
                "Enter your password"
        );

        Button loginButton = new Button("Login");
        loginButton.getStyleClass().add("primary-button");

        // Keyboard navigation and submit behavior
        emailField.setOnAction(e -> passwordField.requestFocus());

        passwordField.setOnAction(e -> loginButton.fire());
        visiblePasswordField.setOnAction(e -> loginButton.fire());

        Button signupButton = new Button("Create Account");
        signupButton.getStyleClass().add("secondary-button");

        Label messageLabel = new Label();
        messageLabel.getStyleClass().add("message-label");

        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            emailField.getParent().getStyleClass().remove("input-error");
            messageLabel.setText("");
        });

        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            passwordField.getParent().getStyleClass().remove("input-error");
            messageLabel.setText("");
        });

        visiblePasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            visiblePasswordField.getParent().getStyleClass().remove("input-error");
            messageLabel.setText("");
        });

        loginButton.setOnAction(e -> {
            String email = emailField.getText().trim();
            String password = passwordField.isVisible()
                    ? passwordField.getText().trim()
                    : visiblePasswordField.getText().trim();

            if (email.isEmpty() && password.isEmpty()) {
                messageLabel.setText("Please enter your email and password.");
                emailField.getParent().getStyleClass().add("input-error");
                getActivePasswordField(passwordField, visiblePasswordField)
                        .getParent().getStyleClass().add("input-error");
                return;
            }

            if (email.isEmpty()) {
                messageLabel.setText("Email is required.");
                emailField.getParent().getStyleClass().add("input-error");
                return;
            }

            if (!isValidEmail(email)) {
                messageLabel.setText("Invalid email format (example: user@gmail.com)");
                emailField.getParent().getStyleClass().add("input-error");
                return;
            }

            if (password.isEmpty()) {
                messageLabel.setText("Password is required.");
                getActivePasswordField(passwordField, visiblePasswordField)
                        .getParent().getStyleClass().add("input-error");
                return;
            }

            User user = authService.loginUser(email, password);

            if (user != null) {
                SessionManager.setCurrentUser(user);
                SessionManager.setFirstLoginAfterSignup(false);
                SceneManager.switchTo(new DashboardView().getScene(), "CLO - Dashboard");
            } else {
                messageLabel.setText("Incorrect email or password.");
            }
        });

        signupButton.setOnAction(e ->
                SceneManager.switchTo(new SignupView().getScene(), "CLO - Sign Up")
        );

        VBox root = new VBox(
                16,
                title,
                subtitle,
                loginLabel,
                emailBox,
                passwordBox,
                loginButton,
                signupButton,
                messageLabel
        );

        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.getStyleClass().add("root-pane");

        scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource(ThemeManager.getThemeStylesheet()).toExternalForm());
    }

    private VBox createFloatingField(String labelText, TextField field, String helperText) {
        Label label = new Label(labelText);
        label.getStyleClass().add("floating-label");

        field.getStyleClass().add("floating-input");

        VBox fieldBox = new VBox(2, label, field);
        fieldBox.getStyleClass().add("floating-field-box");

        Label helper = new Label(helperText);
        helper.getStyleClass().add("helper-text");

        return new VBox(4, fieldBox, helper);
    }

    private VBox createPasswordField(String labelText, PasswordField hiddenField, TextField visibleField, String helperText) {
        Label label = new Label(labelText);
        label.getStyleClass().add("floating-label");

        hiddenField.getStyleClass().add("floating-input");
        visibleField.getStyleClass().add("floating-input");

        Image eyeOpen = new Image(getClass().getResourceAsStream("/icons/eye.png"));
        Image eyeClosed = new Image(getClass().getResourceAsStream("/icons/eye-off.png"));

        ImageView eyeIcon = new ImageView(eyeClosed);
        eyeIcon.setFitWidth(18);
        eyeIcon.setFitHeight(18);

        Button toggleButton = new Button();
        toggleButton.setGraphic(eyeIcon);
        toggleButton.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        toggleButton.setCursor(Cursor.HAND);
        toggleButton.setFocusTraversable(false);
        toggleButton.setPrefWidth(35);

        toggleButton.setOnAction(e -> {
            if (hiddenField.isVisible()) {
                hiddenField.setVisible(false);
                hiddenField.setManaged(false);

                visibleField.setVisible(true);
                visibleField.setManaged(true);

                eyeIcon.setImage(eyeOpen);
            } else {
                visibleField.setVisible(false);
                visibleField.setManaged(false);

                hiddenField.setVisible(true);
                hiddenField.setManaged(true);

                eyeIcon.setImage(eyeClosed);
            }
        });

        VBox fieldStack = new VBox(2, label, hiddenField, visibleField);

        HBox wrapper = new HBox(10, fieldStack, toggleButton);
        wrapper.setAlignment(Pos.CENTER_LEFT);

        VBox fieldBox = new VBox(2, wrapper);
        fieldBox.getStyleClass().add("floating-field-box");

        Label helper = new Label(helperText);
        helper.getStyleClass().add("helper-text");

        return new VBox(4, fieldBox, helper);
    }

    private TextField getActivePasswordField(PasswordField hiddenField, TextField visibleField) {
        return hiddenField.isVisible() ? hiddenField : visibleField;
    }

    private boolean isValidEmail(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(regex);
    }

    public Scene getScene() {
        return scene;
    }
}