package oopproject.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import oopproject.model.Income;
import oopproject.model.User;
import oopproject.service.FinanceService;
import oopproject.util.SceneManager;
import oopproject.util.SessionManager;
import oopproject.util.ThemeManager;

import java.time.LocalDate;

// Form used to save a new income transaction
public class AddIncomeView {

    private final Scene scene;
    private final FinanceService financeService = new FinanceService();

    public AddIncomeView() {
        double inputWidth = 300;

        Label title = new Label("Add Income");
        title.getStyleClass().add("section-title");

        TextField sourceField = new TextField();
        TextField titleField = new TextField();
        TextField amountField = new TextField();
        TextField noteField = new TextField();

        DatePicker datePicker = new DatePicker();
        datePicker.setValue(LocalDate.now());
        datePicker.getStyleClass().add("floating-input");

        VBox sourceBox = createFloatingField("Income Source", sourceField, "Example: Salary, Freelance, Scholarship", inputWidth);
        VBox titleBox = createFloatingField("Title / Description", titleField, "Example: Monthly salary", inputWidth);
        VBox amountBox = createFloatingField("Amount", amountField, "Enter amount in USD", inputWidth);
        VBox dateBox = createDateField("Date", datePicker, "Choose the transaction date", inputWidth);
        VBox noteBox = createFloatingField("Note", noteField, "Optional", inputWidth);

        Label messageLabel = new Label();
        messageLabel.getStyleClass().add("message-label");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(360);
        messageLabel.setAlignment(Pos.CENTER);

        // Allows only numbers with up to two decimal places
        amountField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d{0,2})?")) {
                amountField.setText(oldVal);
            }

            // Clears warning message while typing
            messageLabel.setText("");
        });

        Button saveButton = new Button("Save Income");
        saveButton.getStyleClass().add("primary-button");

        Button backButton = new Button("Back to Dashboard");
        backButton.getStyleClass().add("secondary-button");

        // Keyboard navigation
        sourceField.setOnAction(e -> titleField.requestFocus());
        titleField.setOnAction(e -> amountField.requestFocus());
        amountField.setOnAction(e -> datePicker.requestFocus());
        datePicker.setOnAction(e -> noteField.requestFocus());
        noteField.setOnAction(e -> saveButton.fire());

        saveButton.setOnAction(e -> {
            User currentUser = SessionManager.getCurrentUser();

            if (currentUser == null) {
                messageLabel.setText("No active user session.");
                return;
            }

            String source = sourceField.getText().trim();
            String transactionTitle = titleField.getText().trim();
            String amountText = amountField.getText().trim();
            String note = noteField.getText().trim();
            LocalDate date = datePicker.getValue();

            // Required field validation (no red borders, only message)
            if (source.isEmpty() || transactionTitle.isEmpty() || amountText.isEmpty()) {
                messageLabel.setText("Please fill in the required fields.");
                return;
            }

            try {
                double amount = Double.parseDouble(amountText);

                if (amount <= 0) {
                    messageLabel.setText("Amount must be greater than 0.");
                    return;
                }

                Income income = new Income(
                        0,
                        currentUser.getId(),
                        source,
                        transactionTitle,
                        amount,
                        date,
                        note
                );

                financeService.addIncome(income);
                SceneManager.switchTo(new DashboardView().getScene(), "CLO - Dashboard");

            } catch (NumberFormatException ex) {
                messageLabel.setText("Amount must be a valid number.");
            }
        });

        backButton.setOnAction(e ->
                SceneManager.switchTo(new DashboardView().getScene(), "CLO - Dashboard")
        );

        // Main form layout (same alignment style as SignupView)
        VBox form = new VBox(
                16,
                title,
                sourceBox,
                titleBox,
                amountBox,
                dateBox,
                noteBox,
                saveButton,
                backButton,
                messageLabel
        );

        form.setAlignment(Pos.CENTER);
        form.setMaxWidth(420);

        // Center everything on the page
        StackPane root = new StackPane(form);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.getStyleClass().add("root-pane");

        scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource(ThemeManager.getThemeStylesheet()).toExternalForm());
    }

    // Creates a text input block with helper text
    private VBox createFloatingField(String labelText, TextField field, String helperText, double width) {
        Label label = new Label(labelText);
        label.getStyleClass().add("floating-label");

        field.getStyleClass().add("floating-input");
        field.setPrefWidth(width);
        field.setMaxWidth(width);

        VBox fieldBox = new VBox(2, label, field);
        fieldBox.getStyleClass().add("floating-field-box");
        fieldBox.setAlignment(Pos.CENTER_LEFT);
        fieldBox.setPrefWidth(width);
        fieldBox.setMaxWidth(width);

        Label helper = new Label(helperText);
        helper.getStyleClass().add("helper-text");
        helper.setAlignment(Pos.CENTER_LEFT);
        helper.setPrefWidth(width);
        helper.setMaxWidth(width);

        // Center the whole block (this fixes left shift)
        VBox wrapper = new VBox(4, fieldBox, helper);
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setPrefWidth(width);
        wrapper.setMaxWidth(width);

        return wrapper;
    }

    // Creates the date picker block
    private VBox createDateField(String labelText, DatePicker picker, String helperText, double width) {
        Label label = new Label(labelText);
        label.getStyleClass().add("floating-label");

        picker.setPrefWidth(width);
        picker.setMaxWidth(width);

        VBox fieldBox = new VBox(2, label, picker);
        fieldBox.getStyleClass().add("floating-field-box");
        fieldBox.setAlignment(Pos.CENTER_LEFT);
        fieldBox.setPrefWidth(width);
        fieldBox.setMaxWidth(width);

        Label helper = new Label(helperText);
        helper.getStyleClass().add("helper-text");
        helper.setAlignment(Pos.CENTER_LEFT);
        helper.setPrefWidth(width);
        helper.setMaxWidth(width);

        // Center block
        VBox wrapper = new VBox(4, fieldBox, helper);
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setPrefWidth(width);
        wrapper.setMaxWidth(width);

        return wrapper;
    }

    public Scene getScene() {
        return scene;
    }
}