package oopproject.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
        categoryBox.setPrefWidth(300);

        TextField titleField = new TextField(expense.getTitle());
        TextField amountField = new TextField(String.format("%.2f", expense.getAmount()));
        TextField noteField = new TextField(expense.getNote());

        DatePicker datePicker = new DatePicker();
        datePicker.setValue(expense.getDate() != null ? expense.getDate() : LocalDate.now());
        datePicker.getStyleClass().add("floating-input");

        VBox categoryBoxWrapper = createCategoryField(categoryBox);
        VBox titleBox = createFloatingField("Title / Description", titleField, "Example: Rent, groceries, taxi");
        VBox amountBox = createFloatingField("Amount", amountField, "Enter amount in USD");
        VBox dateBox = createDateField("Date", datePicker, "Choose the transaction date");
        VBox noteBox = createFloatingField("Note", noteField, "Optional");

        Label messageLabel = new Label();
        messageLabel.getStyleClass().add("message-label");
        messageLabel.setMinHeight(24);

        // Restricts amount input to numbers with up to 2 decimals
        amountField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d{0,2})?")) {
                amountField.setText(oldVal);
            }
            amountField.getParent().getStyleClass().remove("input-error");
            messageLabel.setText("");
        });

        Button saveButton = new Button("Save Changes");
        saveButton.getStyleClass().add("primary-button");

        Button backButton = new Button("Back to Category");
        backButton.getStyleClass().add("secondary-button");

        // Keyboard navigation for faster data editing
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

            if (category == null || transactionTitle.isEmpty() || amountText.isEmpty()) {
                messageLabel.setText("Please fill in all required fields.");

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
                amountField.getParent().getStyleClass().add("input-error");
            }
        });

        backButton.setOnAction(e ->
                SceneManager.switchTo(new CategoryDetailsView(expense.getCategory()).getScene(), "CLO - " + expense.getCategory())
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

    private VBox createCategoryField(ComboBox<String> categoryBox) {
        Label label = new Label("Expense Category");
        label.getStyleClass().add("floating-label");

        VBox fieldBox = new VBox(2, label, categoryBox);
        fieldBox.getStyleClass().add("floating-field-box");

        Label helper = new Label("Choose where this expense belongs");
        helper.getStyleClass().add("helper-text");

        return new VBox(4, fieldBox, helper);
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