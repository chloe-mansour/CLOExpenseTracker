package oopproject.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
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
        double inputWidth = 300;

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
        categoryBox.setPrefWidth(inputWidth);
        categoryBox.setMaxWidth(inputWidth);

        TextField titleField = new TextField();
        TextField amountField = new TextField();
        TextField noteField = new TextField();

        DatePicker datePicker = new DatePicker();
        datePicker.setValue(LocalDate.now());
        datePicker.getStyleClass().add("floating-input");

        VBox categoryBoxWrapper = createCategoryField(categoryBox, inputWidth);
        VBox titleBox = createFloatingField("Title / Description", titleField, "Example: Rent, groceries, taxi", inputWidth);
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

            // Clears warning text while the user edits
            messageLabel.setText("");
        });

        Button saveButton = new Button("Save Expense");
        saveButton.getStyleClass().add("primary-button");

        Button backButton = new Button("Back to Dashboard");
        backButton.getStyleClass().add("secondary-button");

        // Keyboard navigation makes the form easier to use
        categoryBox.setOnAction(e -> titleField.requestFocus());
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

            String category = categoryBox.getValue();
            String transactionTitle = titleField.getText().trim();
            String amountText = amountField.getText().trim();
            String note = noteField.getText().trim();
            LocalDate date = datePicker.getValue();

            // Required field validation
            if (category == null || transactionTitle.isEmpty() || amountText.isEmpty()) {
                messageLabel.setText("Please fill in the required fields.");
                return;
            }

            try {
                double amount = Double.parseDouble(amountText);

                if (amount <= 0) {
                    messageLabel.setText("Amount must be greater than 0.");
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
            }
        });

        backButton.setOnAction(e ->
                SceneManager.switchTo(new DashboardView().getScene(), "CLO - Dashboard")
        );

        // Main form layout, same style as SignupView
        VBox form = new VBox(
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

        // Keeps the form centered without stretching buttons
        form.setAlignment(Pos.CENTER);
        form.setMaxWidth(420);

        // StackPane centers the form on the page
        StackPane root = new StackPane(form);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.getStyleClass().add("root-pane");

        scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource(ThemeManager.getThemeStylesheet()).toExternalForm());
    }

    // Creates the category selector block
    private VBox createCategoryField(ComboBox<String> categoryBox, double width) {
        Label categoryLabel = new Label("Expense Category");
        categoryLabel.getStyleClass().add("floating-label");

        VBox categoryFieldBox = new VBox(2, categoryLabel, categoryBox);
        categoryFieldBox.getStyleClass().add("floating-field-box");
        categoryFieldBox.setAlignment(Pos.CENTER_LEFT);
        categoryFieldBox.setMinWidth(width);
        categoryFieldBox.setPrefWidth(width);
        categoryFieldBox.setMaxWidth(width);

        Label categoryHelper = new Label("Choose where this expense belongs");
        categoryHelper.getStyleClass().add("helper-text");
        categoryHelper.setAlignment(Pos.CENTER_LEFT);
        categoryHelper.setMinWidth(width);
        categoryHelper.setPrefWidth(width);
        categoryHelper.setMaxWidth(width);

        // Container is centered like SignupView fields
        VBox container = new VBox(4, categoryFieldBox, categoryHelper);
        container.setAlignment(Pos.CENTER);
        container.setMinWidth(width);
        container.setPrefWidth(width);
        container.setMaxWidth(width);

        return container;
    }

    // Creates a normal text input block
    private VBox createFloatingField(String labelText, TextField field, String helperText, double width) {
        Label label = new Label(labelText);
        label.getStyleClass().add("floating-label");

        field.getStyleClass().add("floating-input");
        field.setMinWidth(width);
        field.setPrefWidth(width);
        field.setMaxWidth(width);

        VBox fieldBox = new VBox(2, label, field);
        fieldBox.getStyleClass().add("floating-field-box");
        fieldBox.setAlignment(Pos.CENTER_LEFT);
        fieldBox.setMinWidth(width);
        fieldBox.setPrefWidth(width);
        fieldBox.setMaxWidth(width);

        Label helper = new Label(helperText);
        helper.getStyleClass().add("helper-text");
        helper.setAlignment(Pos.CENTER_LEFT);
        helper.setMinWidth(width);
        helper.setPrefWidth(width);
        helper.setMaxWidth(width);

        // Container keeps helper text aligned under the box
        VBox container = new VBox(4, fieldBox, helper);
        container.setAlignment(Pos.CENTER);
        container.setMinWidth(width);
        container.setPrefWidth(width);
        container.setMaxWidth(width);

        return container;
    }

    // Creates the date picker block with helper text
    private VBox createDateField(String labelText, DatePicker picker, String helperText, double width) {
        Label label = new Label(labelText);
        label.getStyleClass().add("floating-label");

        // Slightly smaller than the outer box so the calendar icon stays inside
        picker.setMinWidth(width - 30);
        picker.setPrefWidth(width - 30);
        picker.setMaxWidth(width - 30);

        VBox fieldBox = new VBox(2, label, picker);
        fieldBox.getStyleClass().add("floating-field-box");
        fieldBox.setAlignment(Pos.CENTER_LEFT);
        fieldBox.setMinWidth(width);
        fieldBox.setPrefWidth(width);
        fieldBox.setMaxWidth(width);

        Label helper = new Label(helperText);
        helper.getStyleClass().add("helper-text");
        helper.setAlignment(Pos.CENTER_LEFT);
        helper.setMinWidth(width);
        helper.setPrefWidth(width);
        helper.setMaxWidth(width);

        // Centers the whole date block like the other inputs
        VBox wrapper = new VBox(4, fieldBox, helper);
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setMinWidth(width);
        wrapper.setPrefWidth(width);
        wrapper.setMaxWidth(width);

        return wrapper;
    }

    public Scene getScene() {
        return scene;
    }
}