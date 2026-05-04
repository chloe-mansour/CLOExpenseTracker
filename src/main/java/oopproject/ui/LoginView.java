package oopproject.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.text.TextAlignment;
import oopproject.model.User;
import oopproject.service.AuthService;
import oopproject.util.SceneManager;
import oopproject.util.SessionManager;
import oopproject.util.ThemeManager;

// Login screen with validation and password visibility toggle
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

        // Both password fields share the same text
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

        Button signupButton = new Button("Create Account");
        signupButton.getStyleClass().add("secondary-button");

        Label messageLabel = new Label();
        messageLabel.getStyleClass().add("message-label");
        messageLabel.setWrapText(true);
        messageLabel.setAlignment(Pos.CENTER);
        messageLabel.setTextAlignment(TextAlignment.CENTER);
        messageLabel.setMaxWidth(360);
        messageLabel.setMinHeight(24);

        // Keyboard navigation
        emailField.setOnAction(e -> passwordField.requestFocus());
        passwordField.setOnAction(e -> loginButton.fire());
        visiblePasswordField.setOnAction(e -> loginButton.fire());

        // Clear warning message when the user edits the form
        emailField.textProperty().addListener((obs, oldVal, newVal) -> messageLabel.setText(""));
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> messageLabel.setText(""));
        visiblePasswordField.textProperty().addListener((obs, oldVal, newVal) -> messageLabel.setText(""));

        loginButton.setOnAction(e -> {
            String email = emailField.getText().trim();

            String password = passwordField.isVisible()
                    ? passwordField.getText().trim()
                    : visiblePasswordField.getText().trim();

            // Validation uses only warning messages, without red borders
            if (email.isEmpty() && password.isEmpty()) {
                messageLabel.setText("Please enter your email and password.");
                return;
            }

            if (email.isEmpty()) {
                messageLabel.setText("Email is required.");
                return;
            }

            if (!isValidEmail(email)) {
                messageLabel.setText("Please enter the correct email format, such as name@gmail.com or name@outlook.com.");
                return;
            }

            if (password.isEmpty()) {
                messageLabel.setText("Password is required.");
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

        VBox form = new VBox(
                18,
                title,
                subtitle,
                loginLabel,
                emailBox,
                passwordBox,
                loginButton,
                signupButton,
                messageLabel
        );

        form.setAlignment(Pos.CENTER);
        form.setMaxWidth(420);

        // Decorative background lines
        Pane background = new Pane();
        background.setMouseTransparent(true);

        Line topLine = new Line();
        Line bottomLine = new Line();

        topLine.setStrokeWidth(2);
        bottomLine.setStrokeWidth(2);

        topLine.setStyle("-fx-stroke: #6d5dfc;");
        bottomLine.setStyle("-fx-stroke: #6d5dfc;");

        topLine.startXProperty().bind(background.widthProperty().multiply(0.05));
        topLine.endXProperty().bind(background.widthProperty().multiply(0.75));
        topLine.setStartY(50);
        topLine.setEndY(50);

        bottomLine.startXProperty().bind(background.widthProperty().multiply(0.25));
        bottomLine.endXProperty().bind(background.widthProperty().multiply(0.95));
        bottomLine.startYProperty().bind(background.heightProperty().subtract(40));
        bottomLine.endYProperty().bind(background.heightProperty().subtract(40));

        background.getChildren().addAll(topLine, bottomLine);

        StackPane root = new StackPane(background, form);
        root.getStyleClass().add("root-pane");
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));

        scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource(ThemeManager.getThemeStylesheet()).toExternalForm());
    }

    // Creates a normal input block with helper text
    private VBox createFloatingField(String labelText, TextField field, String helperText) {
        double inputWidth = 360;

        Label label = new Label(labelText);
        label.getStyleClass().add("floating-label");

        field.getStyleClass().add("floating-input");

        VBox fieldBox = new VBox(2, label, field);
        fieldBox.getStyleClass().add("floating-field-box");
        fieldBox.setMinWidth(inputWidth);
        fieldBox.setPrefWidth(inputWidth);
        fieldBox.setMaxWidth(inputWidth);

        Label helper = new Label(helperText);
        helper.getStyleClass().add("helper-text");
        helper.setMinWidth(inputWidth);
        helper.setPrefWidth(inputWidth);
        helper.setMaxWidth(inputWidth);
        helper.setAlignment(Pos.CENTER_LEFT);

        VBox container = new VBox(4, fieldBox, helper);
        container.setAlignment(Pos.CENTER);
        container.setMinWidth(inputWidth);
        container.setPrefWidth(inputWidth);
        container.setMaxWidth(inputWidth);

        return container;
    }

    // Creates the password field with an eye icon to show or hide the password
    private VBox createPasswordField(String labelText, PasswordField hiddenField, TextField visibleField, String helperText) {
        double inputWidth = 360;

        Label label = new Label(labelText);
        label.getStyleClass().add("floating-label");

        hiddenField.getStyleClass().add("floating-input");
        visibleField.getStyleClass().add("floating-input");

        hiddenField.setMinWidth(inputWidth);
        hiddenField.setPrefWidth(inputWidth);
        hiddenField.setMaxWidth(inputWidth);

        visibleField.setMinWidth(inputWidth);
        visibleField.setPrefWidth(inputWidth);
        visibleField.setMaxWidth(inputWidth);

        Image eyeOpen = new Image(getClass().getResourceAsStream("/icons/eye.png"));
        Image eyeClosed = new Image(getClass().getResourceAsStream("/icons/eye-off.png"));

        ImageView eyeIcon = new ImageView(eyeClosed);
        eyeIcon.setFitWidth(18);
        eyeIcon.setFitHeight(18);

        // Makes the black eye icon visible in dark mode
        if (ThemeManager.getThemeStylesheet().contains("dark")) {
            ColorAdjust whiteIcon = new ColorAdjust();
            whiteIcon.setBrightness(1.0);
            eyeIcon.setEffect(whiteIcon);
        }

        Button toggleButton = new Button();
        toggleButton.setGraphic(eyeIcon);
        toggleButton.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 0;");
        toggleButton.setCursor(Cursor.HAND);
        toggleButton.setFocusTraversable(false);

        StackPane inputStack = new StackPane(hiddenField, visibleField, toggleButton);
        inputStack.setMinWidth(inputWidth);
        inputStack.setPrefWidth(inputWidth);
        inputStack.setMaxWidth(inputWidth);

        StackPane.setAlignment(toggleButton, Pos.CENTER_RIGHT);
        StackPane.setMargin(toggleButton, new Insets(0, 14, 0, 0));
        toggleButton.setTranslateX(-13);
        toggleButton.setTranslateY(-10);

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

        VBox fieldBox = new VBox(2, label, inputStack);
        fieldBox.getStyleClass().add("floating-field-box");
        fieldBox.setMinWidth(inputWidth);
        fieldBox.setPrefWidth(inputWidth);
        fieldBox.setMaxWidth(inputWidth);

        Label helper = new Label(helperText);
        helper.getStyleClass().add("helper-text");
        helper.setMinWidth(inputWidth);
        helper.setPrefWidth(inputWidth);
        helper.setMaxWidth(inputWidth);
        helper.setAlignment(Pos.CENTER_LEFT);

        VBox container = new VBox(4, fieldBox, helper);
        container.setMinWidth(inputWidth);
        container.setPrefWidth(inputWidth);
        container.setMaxWidth(inputWidth);
        container.setAlignment(Pos.CENTER);

        return container;
    }

    // Checks if the email matches a normal email format
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    public Scene getScene() {
        return scene;
    }
}