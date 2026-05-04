package oopproject.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import oopproject.model.Expense;
import oopproject.service.FinanceService;
import oopproject.util.SceneManager;
import oopproject.util.ThemeManager;

import java.time.LocalDate;

// Allows the user to edit an existing expense transaction
public class EditExpenseView {

    private final Scene scene;
    private final FinanceService financeService = new FinanceService();

    public EditExpenseView(Expense expense) {
        double inputWidth = 300;

        Label title = new Label("Edit Expense");
        title.getStyleClass().add("section-title");

        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll(
                "Essentials",
                "Education & Work",
                "Savings & Investments",
                "Lifestyle & Fun",
                "Health & Emergencies"
        );
        categoryBox.setValue(expense.getCategory());
        categoryBox.setPrefWidth(inputWidth);
        categoryBox.setMaxWidth(inputWidth);

        TextField titleField = new TextField(expense.getTitle());
        TextField amountField = new TextField(String.format("%.2f", expense.getAmount()));
        TextField noteField = new TextField(expense.getNote());

        DatePicker datePicker = new DatePicker();
        datePicker.setValue(expense.getDate() != null ? expense.getDate() : LocalDate.now());
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

        // Restricts amount input to numbers with up to 2 decimals
        amountField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d{0,2})?")) {
                amountField.setText(oldVal);
            }

            messageLabel.setText("");
        });

        Button saveButton = new Button("Save Changes");
        saveButton.getStyleClass().add("primary-button");

        Button backButton = new Button("Back to Category");
        backButton.getStyleClass().add("secondary-button");

        // Keyboard navigation
        categoryBox.setOnAction(e -> titleField.requestFocus());
        titleField.setOnAction(e -> amountField.requestFocus());
        amountField.setOnAction(e -> datePicker.requestFocus());
        datePicker.setOnAction(e -> noteField.requestFocus());
        noteField.setOnAction(e -> saveButton.fire());

        saveButton.setOnAction(e -> {
            String category = categoryBox.getValue();
            String transactionTitle = titleField.getText().trim();
            String amountText = amountField.getText().trim();
            String note = noteField.getText().trim();
            LocalDate date = datePicker.getValue();

            // Validation uses only warning messages, without red borders
            if (category == null || transactionTitle.isEmpty() || amountText.isEmpty()) {
                messageLabel.setText("Please fill in all required fields.");
                return;
            }

            try {
                double amount = Double.parseDouble(amountText);

                if (amount <= 0) {
                    messageLabel.setText("Amount must be greater than 0.");
                    return;
                }

                Expense updatedExpense = new Expense(
                        expense.getId(),
                        expense.getUserId(),
                        category,
                        transactionTitle,
                        amount,
                        date,
                        note
                );

                boolean success = financeService.updateExpense(updatedExpense);

                if (success) {
                    SceneManager.switchTo(new CategoryDetailsView(category).getScene(), "CLO - " + category);
                } else {
                    messageLabel.setText("Could not update expense.");
                }

            } catch (NumberFormatException ex) {
                messageLabel.setText("Amount must be a valid number.");
            }
        });

        backButton.setOnAction(e ->
                SceneManager.switchTo(new CategoryDetailsView(expense.getCategory()).getScene(), "CLO - " + expense.getCategory())
        );

        // Main form layout matching AddExpense, AddIncome, and Signup style
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

        form.setAlignment(Pos.CENTER);
        form.setMaxWidth(420);

        // Centers the form on the page
        StackPane root = new StackPane(form);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.getStyleClass().add("root-pane");

        scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource(ThemeManager.getThemeStylesheet()).toExternalForm());
    }

    // Creates the category selector block
    private VBox createCategoryField(ComboBox<String> categoryBox, double width) {
        Label label = new Label("Expense Category");
        label.getStyleClass().add("floating-label");

        VBox fieldBox = new VBox(2, label, categoryBox);
        fieldBox.getStyleClass().add("floating-field-box");
        fieldBox.setAlignment(Pos.CENTER_LEFT);
        fieldBox.setMinWidth(width);
        fieldBox.setPrefWidth(width);
        fieldBox.setMaxWidth(width);

        Label helper = new Label("Choose where this expense belongs");
        helper.getStyleClass().add("helper-text");
        helper.setAlignment(Pos.CENTER_LEFT);
        helper.setMinWidth(width);
        helper.setPrefWidth(width);
        helper.setMaxWidth(width);

        // Centers the whole block while keeping helper text aligned with the box
        VBox wrapper = new VBox(4, fieldBox, helper);
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setMinWidth(width);
        wrapper.setPrefWidth(width);
        wrapper.setMaxWidth(width);

        return wrapper;
    }

    // Creates a text input block with helper text
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

        // Centers the whole input block
        VBox wrapper = new VBox(4, fieldBox, helper);
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setMinWidth(width);
        wrapper.setPrefWidth(width);
        wrapper.setMaxWidth(width);

        return wrapper;
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

        // Centers the whole date block
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