package oopproject.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import oopproject.model.Expense;
import oopproject.model.User;
import oopproject.service.FinanceService;
import oopproject.util.SceneManager;
import oopproject.util.SessionManager;
import oopproject.util.ThemeManager;

import java.util.List;

// Displays and manages expenses saved under one wallet category
public class CategoryDetailsView {

    private final Scene scene;
    private final FinanceService financeService = new FinanceService();

    public CategoryDetailsView(String categoryName) {
        User currentUser = SessionManager.getCurrentUser();
        int userId = currentUser != null ? currentUser.getId() : 0;

        double categoryTotal = financeService.getCategoryTotal(userId, categoryName);
        List<Expense> expenses = financeService.getExpensesByCategory(userId, categoryName);

        Label title = new Label(categoryName);
        title.getStyleClass().add("section-title");

        Label subtitle = new Label("Review and manage your saved expenses in this wallet section.");
        subtitle.getStyleClass().add("dashboard-subtitle");

        VBox totalCard = createTotalCard(categoryTotal, expenses.size());

        VBox transactionsBox = new VBox(12);
        transactionsBox.setAlignment(Pos.TOP_CENTER);
        transactionsBox.setMaxWidth(760);

        VBox content = new VBox(18, title, subtitle, totalCard, transactionsBox);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(35));
        content.getStyleClass().add("root-pane");

        StackPane root = new StackPane(content);

        if (expenses.isEmpty()) {
            transactionsBox.getChildren().add(createEmptyState(categoryName));
        } else {
            for (Expense expense : expenses) {
                transactionsBox.getChildren().add(createExpenseCard(expense, categoryName, root, content));
            }
        }

        Button addExpenseButton = new Button("Add Expense");
        addExpenseButton.getStyleClass().add("primary-button");
        addExpenseButton.setOnAction(e ->
                SceneManager.switchTo(new AddExpenseView().getScene(), "CLO - Add Expense")
        );

        Button backButton = new Button("Back to Dashboard");
        backButton.getStyleClass().add("secondary-button");
        backButton.setOnAction(e ->
                SceneManager.switchTo(new DashboardView().getScene(), "CLO - Dashboard")
        );

        HBox buttons = new HBox(12, addExpenseButton, backButton);
        buttons.setAlignment(Pos.CENTER);

        content.getChildren().add(buttons);

        scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource(ThemeManager.getThemeStylesheet()).toExternalForm());
    }

    private VBox createTotalCard(double total, int transactionCount) {
        Label totalTitle = new Label("Category Total");
        totalTitle.getStyleClass().add("card-title");

        Label totalValue = new Label(formatAmount(total));
        totalValue.getStyleClass().add("card-value");

        Label countLabel = new Label(transactionCount + " transaction(s)");
        countLabel.getStyleClass().add("side-card-value");

        VBox box = new VBox(8, totalTitle, totalValue, countLabel);
        box.getStyleClass().add("info-card");
        box.setPadding(new Insets(20));
        box.setMaxWidth(760);
        box.setAlignment(Pos.CENTER_LEFT);

        return box;
    }

    private VBox createEmptyState(String categoryName) {
        Label emptyTitle = new Label("No expenses yet");
        emptyTitle.getStyleClass().add("card-title");

        Label emptyText = new Label("You have not added any expenses under " + categoryName + " yet.");
        emptyText.getStyleClass().add("side-card-value");
        emptyText.setWrapText(true);

        VBox box = new VBox(8, emptyTitle, emptyText);
        box.getStyleClass().add("info-card");
        box.setPadding(new Insets(20));
        box.setMaxWidth(760);

        return box;
    }

    private VBox createExpenseCard(Expense expense, String categoryName, StackPane root, VBox content) {
        Label title = new Label(safeText(expense.getTitle()));
        title.getStyleClass().add("card-title");

        Label amount = new Label(formatAmount(expense.getAmount()));
        amount.getStyleClass().add("card-value");

        Label date = new Label("Date: " + (expense.getDate() != null ? expense.getDate().toString() : "Not provided"));
        date.getStyleClass().add("side-card-value");

        Label note = new Label("Note: " + safeText(expense.getNote()));
        note.getStyleClass().add("side-card-value");
        note.setWrapText(true);

        VBox leftInfo = new VBox(6, title, date, note);

        Button editButton = new Button("Edit");
        editButton.getStyleClass().add("secondary-button");
        editButton.setOnAction(e ->
                SceneManager.switchTo(new EditExpenseView(expense).getScene(), "CLO - Edit Expense")
        );

        Button deleteButton = new Button("Delete");
        deleteButton.getStyleClass().add("secondary-button");

        deleteButton.setOnAction(e -> {
            VBox confirmationCard = createDeleteConfirmationCard(expense, categoryName, root, content);
            root.getChildren().add(confirmationCard);
            StackPane.setAlignment(confirmationCard, Pos.CENTER);
            content.setDisable(true);
        });

        HBox actions = new HBox(8, editButton, deleteButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topRow = new HBox(16, leftInfo, spacer, amount, actions);
        topRow.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(topRow);
        card.getStyleClass().add("info-card");
        card.setPadding(new Insets(18));
        card.setMaxWidth(760);

        return card;
    }

    private VBox createDeleteConfirmationCard(Expense expense, String categoryName, StackPane root, VBox content) {
        Label title = new Label("Delete expense?");
        title.getStyleClass().add("overlay-title");

        Label text = new Label("This transaction will be permanently removed from this category.");
        text.getStyleClass().add("overlay-text");
        text.setWrapText(true);

        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("secondary-button");

        Button confirmButton = new Button("Delete");
        confirmButton.getStyleClass().add("primary-button");

        HBox actions = new HBox(12, cancelButton, confirmButton);
        actions.setAlignment(Pos.CENTER);

        VBox card = new VBox(14, title, text, actions);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(22));
        card.getStyleClass().add("overlay-card");
        card.setMaxWidth(460);

        cancelButton.setOnAction(e -> {
            root.getChildren().remove(card);
            content.setDisable(false);
        });

        confirmButton.setOnAction(e -> {
            boolean deleted = financeService.deleteExpense(expense.getId());

            if (deleted) {
                SceneManager.switchTo(new CategoryDetailsView(categoryName).getScene(), "CLO - " + categoryName);
            } else {
                root.getChildren().remove(card);
                content.setDisable(false);
            }
        });

        return card;
    }

    private String safeText(String text) {
        return text == null || text.isBlank() ? "Not provided" : text;
    }

    private String formatAmount(double amount) {
        return String.format("$%.2f", amount);
    }

    public Scene getScene() {
        return scene;
    }
}