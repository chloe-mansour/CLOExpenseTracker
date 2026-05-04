package oopproject.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
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

import java.time.LocalDate;

// Signup screen with validation, optional profile info, and password visibility toggle
public class SignupView {

    private final Scene scene;
    private final AuthService authService = new AuthService();

    public SignupView() {
        Label title = new Label("Create Your Account");
        title.getStyleClass().add("section-title");

        TextField fullNameField = new TextField();
        TextField emailField = new TextField();

        PasswordField passwordField = new PasswordField();
        TextField visiblePasswordField = new TextField();

        // Both password fields share the same text
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());
        visiblePasswordField.setVisible(false);
        visiblePasswordField.setManaged(false);

        TextField studyFieldField = new TextField();
        TextField jobTitleField = new TextField();

        DatePicker datePicker = new DatePicker();
        datePicker.getStyleClass().add("floating-input");

        VBox fullNameBox = createFloatingField("Full Name", fullNameField, "Enter your full name");
        VBox emailBox = createFloatingField("Email", emailField, "Use a valid email");

        VBox passwordBox = createPasswordField(
                "Password",
                passwordField,
                visiblePasswordField,
                "Use 8+ characters with uppercase, lowercase, number, and special symbol"
        );

        VBox dobBox = createDateField("Date of Birth", datePicker, "Optional");
        VBox studyBox = createFloatingField("Study Field", studyFieldField, "Optional");
        VBox jobBox = createFloatingField("Job Title", jobTitleField, "Required if working");

        CheckBox workingCheck = new CheckBox("Currently working");
        workingCheck.getStyleClass().add("settings-check");

        // Job title only appears if the user is currently working
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

        Button backBtn = new Button("Back to Login");
        backBtn.getStyleClass().add("secondary-button");

        Label message = new Label();
        message.getStyleClass().add("message-label");
        message.setWrapText(true);
        message.setMaxWidth(360);
        message.setAlignment(Pos.CENTER);
        message.setTextAlignment(TextAlignment.CENTER);
        message.setVisible(false);
        message.setManaged(false);

        // The warning message only takes space when there is text
        message.textProperty().addListener((obs, oldText, newText) -> {
            boolean hasMessage = newText != null && !newText.isBlank();
            message.setVisible(hasMessage);
            message.setManaged(hasMessage);
        });

        // Keyboard navigation between fields
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

        // Clear warning message when the user edits the form
        fullNameField.textProperty().addListener((obs, oldVal, newVal) -> message.setText(""));
        emailField.textProperty().addListener((obs, oldVal, newVal) -> message.setText(""));
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> message.setText(""));
        visiblePasswordField.textProperty().addListener((obs, oldVal, newVal) -> message.setText(""));
        jobTitleField.textProperty().addListener((obs, oldVal, newVal) -> message.setText(""));

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

            if (!isValidFullName(fullName)) {
                message.setText("Full name must contain at least 2 letters.");
                return;
            }

            if (!isValidEmail(email)) {
                message.setText("Please enter the correct email format, such as name@gmail.com or name@outlook.com.");
                return;
            }

            if (!isStrongPassword(password)) {
                message.setText("Password must include uppercase, lowercase, number, and special character.");
                return;
            }

            if (dob != null && dob.isAfter(LocalDate.now())) {
                message.setText("Date of birth cannot be in the future.");
                return;
            }

            if (working && job.isEmpty()) {
                message.setText("Job title is required if you are currently working.");
                return;
            }

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

        VBox form = new VBox(
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

        form.setAlignment(Pos.CENTER);
        form.setMaxWidth(420);

        // Decorative background lines
        Pane background = new Pane();
        background.setMouseTransparent(true);

        Line topLine = new Line();
        Line bottomLine = new Line();

        topLine.setStrokeWidth(1);
        bottomLine.setStrokeWidth(1);

        topLine.setStyle("-fx-stroke: #6d5dfc;");
        bottomLine.setStyle("-fx-stroke: #6d5dfc;");

        topLine.startXProperty().bind(background.widthProperty().multiply(0.05));
        topLine.endXProperty().bind(background.widthProperty().multiply(0.75));
        topLine.setStartY(30);
        topLine.setEndY(30);

        bottomLine.startXProperty().bind(background.widthProperty().multiply(0.25));
        bottomLine.endXProperty().bind(background.widthProperty().multiply(0.95));
        bottomLine.startYProperty().bind(background.heightProperty().subtract(20));
        bottomLine.endYProperty().bind(background.heightProperty().subtract(20));

        background.getChildren().addAll(topLine, bottomLine);

        StackPane root = new StackPane(background, form);
        root.getStyleClass().add("root-pane");
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));

        scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource(ThemeManager.getThemeStylesheet()).toExternalForm());
    }

    // Creates a normal input block with helper text
    private VBox createFloatingField(String labelText, TextField field, String helper) {
        double inputWidth = 360;

        Label label = new Label(labelText);
        label.getStyleClass().add("floating-label");

        field.getStyleClass().add("floating-input");

        VBox box = new VBox(2, label, field);
        box.getStyleClass().add("floating-field-box");
        box.setMinWidth(inputWidth);
        box.setPrefWidth(inputWidth);
        box.setMaxWidth(inputWidth);

        Label help = new Label(helper);
        help.getStyleClass().add("helper-text");
        help.setMinWidth(inputWidth);
        help.setPrefWidth(inputWidth);
        help.setMaxWidth(inputWidth);
        help.setAlignment(Pos.CENTER_LEFT);

        VBox container = new VBox(4, box, help);
        container.setAlignment(Pos.CENTER);
        container.setMinWidth(inputWidth);
        container.setPrefWidth(inputWidth);
        container.setMaxWidth(inputWidth);

        return container;
    }

    // Creates a date picker input block with helper text
    private VBox createDateField(String labelText, DatePicker picker, String helper) {
        double inputWidth = 360;

        Label label = new Label(labelText);
        label.getStyleClass().add("floating-label");

        picker.getStyleClass().add("floating-input");
        picker.setMinWidth(inputWidth - 30);
        picker.setPrefWidth(inputWidth - 30);
        picker.setMaxWidth(inputWidth - 30);

        VBox box = new VBox(2, label, picker);
        box.getStyleClass().add("floating-field-box");
        box.setMinWidth(inputWidth);
        box.setPrefWidth(inputWidth);
        box.setMaxWidth(inputWidth);

        Label help = new Label(helper);
        help.getStyleClass().add("helper-text");
        help.setMinWidth(inputWidth);
        help.setPrefWidth(inputWidth);
        help.setMaxWidth(inputWidth);
        help.setAlignment(Pos.CENTER_LEFT);

        VBox container = new VBox(4, box, help);
        container.setAlignment(Pos.CENTER);
        container.setMinWidth(inputWidth);
        container.setPrefWidth(inputWidth);
        container.setMaxWidth(inputWidth);

        return container;
    }

    // Creates the password field with an eye icon to show or hide the password
    private VBox createPasswordField(String labelText, PasswordField hidden, TextField visible, String helperText) {
        double inputWidth = 360;

        Label label = new Label(labelText);
        label.getStyleClass().add("floating-label");

        hidden.getStyleClass().add("floating-input");
        visible.getStyleClass().add("floating-input");

        hidden.setMinWidth(inputWidth);
        hidden.setPrefWidth(inputWidth);
        hidden.setMaxWidth(inputWidth);

        visible.setMinWidth(inputWidth);
        visible.setPrefWidth(inputWidth);
        visible.setMaxWidth(inputWidth);

        Image eyeOpen = new Image(getClass().getResourceAsStream("/icons/eye.png"));
        Image eyeClosed = new Image(getClass().getResourceAsStream("/icons/eye-off.png"));

        ImageView icon = new ImageView(eyeClosed);
        icon.setFitWidth(18);
        icon.setFitHeight(18);

        // Makes the black eye icon visible in dark mode
        if (ThemeManager.getThemeStylesheet().contains("dark")) {
            ColorAdjust whiteIcon = new ColorAdjust();
            whiteIcon.setBrightness(1.0);
            icon.setEffect(whiteIcon);
        }

        Button toggle = new Button();
        toggle.setGraphic(icon);
        toggle.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 0;");
        toggle.setCursor(Cursor.HAND);
        toggle.setFocusTraversable(false);

        StackPane inputStack = new StackPane(hidden, visible, toggle);
        inputStack.setMinWidth(inputWidth);
        inputStack.setPrefWidth(inputWidth);
        inputStack.setMaxWidth(inputWidth);

        StackPane.setAlignment(toggle, Pos.CENTER_RIGHT);
        StackPane.setMargin(toggle, new Insets(0, 14, 0, 0));
        toggle.setTranslateX(-13);
        toggle.setTranslateY(-10);

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

        VBox fieldBox = new VBox(2, label, inputStack);
        fieldBox.getStyleClass().add("floating-field-box");
        fieldBox.setMinWidth(inputWidth);
        fieldBox.setPrefWidth(inputWidth);
        fieldBox.setMaxWidth(inputWidth);

        Label help = new Label(helperText);
        help.getStyleClass().add("helper-text");
        help.setMinWidth(inputWidth);
        help.setPrefWidth(inputWidth);
        help.setMaxWidth(inputWidth);
        help.setAlignment(Pos.CENTER_LEFT);

        VBox container = new VBox(4, fieldBox, help);
        container.setAlignment(Pos.CENTER);
        container.setMinWidth(inputWidth);
        container.setPrefWidth(inputWidth);
        container.setMaxWidth(inputWidth);

        return container;
    }

    // Checks if the email follows a normal email format
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    // Checks password strength for better account security
    private boolean isStrongPassword(String password) {
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$");
    }

    // Makes sure the name has at least two letters and only normal name characters
    private boolean isValidFullName(String fullName) {
        return fullName.matches("^(?=.*[A-Za-z])[A-Za-z\\s]{2,}$");
    }

    public Scene getScene() {
        return scene;
    }
}