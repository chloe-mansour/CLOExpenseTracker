package oopproject.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import oopproject.model.Expense;
import oopproject.model.User;
import oopproject.service.FinanceService;
import oopproject.util.SceneManager;
import oopproject.util.SessionManager;
import oopproject.util.ThemeManager;

import java.time.LocalDate;

// Form used to save a new expense transaction
public class AddExpenseView {

    private final Scene scene;
    private final FinanceService financeService = new FinanceService();

    public AddExpenseView() {
        Label title = new Label("Add Expense");
        title.getStyleClass().add("section-title");

        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll(
                "Essentials",
                "Education & Work",
                "Savings & Investments",
                "Lifestyle & Fun",
                "Health & Emergencies"
        );
        categoryBox.setPromptText("Select Category");
        categoryBox.setPrefWidth(300);

        TextField titleField = new TextField();
        TextField amountField = new TextField();
        TextField noteField = new TextField();

        DatePicker datePicker = new DatePicker();
        datePicker.setValue(LocalDate.now());
        datePicker.getStyleClass().add("floating-input");

        VBox titleBox = createFloatingField("Title / Description", titleField, "Example: Rent, groceries, taxi");
        VBox amountBox = createFloatingField("Amount", amountField, "Enter amount in USD");
        VBox noteBox = createFloatingField("Note", noteField, "Optional");
        VBox dateBox = createDateField("Date", datePicker, "Choose the transaction date");

        Label categoryLabel = new Label("Expense Category");
        categoryLabel.getStyleClass().add("floating-label");

        VBox categoryFieldBox = new VBox(2, categoryLabel, categoryBox);
        categoryFieldBox.getStyleClass().add("floating-field-box");

        Label categoryHelper = new Label("Choose where this expense belongs");
        categoryHelper.getStyleClass().add("helper-text");

        VBox categoryBoxWrapper = new VBox(4, categoryFieldBox, categoryHelper);

        Label messageLabel = new Label();
        messageLabel.getStyleClass().add("message-label");

        // Restricts amount input to numbers with up to 2 decimals
        amountField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d{0,2})?")) {
                amountField.setText(oldVal);
            }
            amountField.getParent().getStyleClass().remove("input-error");
            messageLabel.setText("");
        });

        Button saveButton = new Button("Save Expense");
        saveButton.getStyleClass().add("primary-button");

        // Keyboard navigation between expense fields
        categoryBox.setOnAction(e -> titleField.requestFocus());
        titleField.setOnAction(e -> amountField.requestFocus());
        amountField.setOnAction(e -> datePicker.requestFocus());
        datePicker.setOnAction(e -> noteField.requestFocus());
        noteField.setOnAction(e -> saveButton.fire());

        Button backButton = new Button("Back to Dashboard");
        backButton.getStyleClass().add("secondary-button");

        saveButton.setOnAction(e -> {
            User currentUser = SessionManager.getCurrentUser();

            if (currentUser == null) {
                messageLabel.setText("No active user session.");
                return;
            }

            String category = categoryBox.getValue();
            String transactionTitle = titleField.getText().trim();
            String amountText = amountField.getText().trim();
            String note = noteField.getText().trim();
            LocalDate date = datePicker.getValue();

            if (category == null || transactionTitle.isEmpty() || amountText.isEmpty()) {
                messageLabel.setText("Please fill in the required fields.");

                if (amountText.isEmpty()) {
                    amountField.getParent().getStyleClass().add("input-error");
                }
                return;
            }

            try {
                double amount = Double.parseDouble(amountText);

                if (amount <= 0) {
                    messageLabel.setText("Amount must be greater than 0.");
                    amountField.getParent().getStyleClass().add("input-error");
                    return;
                }

                Expense expense = new Expense(
                        0,
                        currentUser.getId(),
                        category,
                        transactionTitle,
                        amount,
                        date,
                        note
                );

                financeService.addExpense(expense);
                SceneManager.switchTo(new DashboardView().getScene(), "CLO - Dashboard");

            } catch (NumberFormatException ex) {
                messageLabel.setText("Amount must be a valid number.");
                amountField.getParent().getStyleClass().add("input-error");
            }
        });

        backButton.setOnAction(e ->
                SceneManager.switchTo(new DashboardView().getScene(), "CLO - Dashboard")
        );

        VBox root = new VBox(
                16,
                title,
                categoryBoxWrapper,
                titleBox,
                amountBox,
                dateBox,
                noteBox,
                saveButton,
                backButton,
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

    private VBox createDateField(String labelText, DatePicker picker, String helperText) {
        Label label = new Label(labelText);
        label.getStyleClass().add("floating-label");

        VBox fieldBox = new VBox(2, label, picker);
        fieldBox.getStyleClass().add("floating-field-box");

        Label helper = new Label(helperText);
        helper.getStyleClass().add("helper-text");

        return new VBox(4, fieldBox, helper);
    }

    public Scene getScene() {
        return scene;
    }
}