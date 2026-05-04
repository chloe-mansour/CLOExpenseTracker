package oopproject.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import oopproject.model.Expense;
import oopproject.model.User;
import oopproject.service.FinanceService;
import oopproject.util.CurrencyManager;
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

        Label subtitle = new Label("Click any expense to view its details, then edit or delete it.");
        subtitle.getStyleClass().add("dashboard-subtitle");

        VBox totalCard = createTotalCard(categoryTotal, expenses);

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

    private VBox createTotalCard(double total, List<Expense> expenses) {
        Label totalTitle = new Label("Category Summary");
        totalTitle.getStyleClass().add("card-title");

        Label totalValue = new Label(CurrencyManager.formatAmount(total));
        totalValue.getStyleClass().add("card-value");

        int count = expenses.size();
        double average = count == 0 ? 0 : total / count;
        double highest = expenses.stream().mapToDouble(Expense::getAmount).max().orElse(0);

        Label countLabel = new Label(count + " transaction(s)");
        countLabel.getStyleClass().add("side-card-value");

        Label averageLabel = new Label("Average: " + CurrencyManager.formatAmount(average));
        averageLabel.getStyleClass().add("side-card-value");

        Label highestLabel = new Label("Highest: " + CurrencyManager.formatAmount(highest));
        highestLabel.getStyleClass().add("side-card-value");

        HBox stats = new HBox(18, countLabel, averageLabel, highestLabel);
        stats.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(8, totalTitle, totalValue, stats);
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

        Label amount = new Label(CurrencyManager.formatAmount(expense.getAmount()));
        amount.getStyleClass().add("card-value");

        Label date = new Label(expense.getDate() != null ? expense.getDate().toString() : "No date");
        date.getStyleClass().add("side-card-value");

        Label hint = new Label("Click to view details");
        hint.getStyleClass().add("helper-text");

        VBox leftInfo = new VBox(5, title, date, hint);
        leftInfo.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(16, leftInfo, spacer, amount);
        row.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(row);
        card.getStyleClass().add("info-card");
        card.setPadding(new Insets(18));
        card.setMaxWidth(760);

        // The row opens a clean popup instead of showing edit/delete directly
        card.setOnMouseClicked(e -> showExpenseDetails(expense, categoryName, root, content));

        return card;
    }

    private void showExpenseDetails(Expense expense, String categoryName, StackPane root, VBox content) {
        VBox detailsCard = createExpenseDetailsCard(expense, categoryName, root, content);

        root.getChildren().add(detailsCard);
        StackPane.setAlignment(detailsCard, Pos.CENTER);

        // Disables the background so the user focuses on the popup
        content.setDisable(true);
    }

    private VBox createExpenseDetailsCard(Expense expense, String categoryName, StackPane root, VBox content) {
        Label title = new Label(safeText(expense.getTitle()));
        title.getStyleClass().add("overlay-title");

        Label amount = new Label(CurrencyManager.formatAmount(expense.getAmount()));
        amount.getStyleClass().add("card-value");

        Label category = createDetailLine("Category", safeText(expense.getCategory()));
        Label date = createDetailLine("Date", expense.getDate() != null ? expense.getDate().toString() : "Not provided");
        Label note = createDetailLine("Note", safeText(expense.getNote()));
        note.setWrapText(true);

        Button closeButton = new Button("Close");
        closeButton.getStyleClass().add("secondary-button");

        Button editButton = new Button("Edit");
        editButton.getStyleClass().add("primary-button");

        Button deleteButton = new Button("Delete");
        deleteButton.getStyleClass().add("secondary-button");

        HBox actions = new HBox(12, closeButton, editButton, deleteButton);
        actions.setAlignment(Pos.CENTER);

        VBox card = new VBox(14, title, amount, category, date, note, actions);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(24));
        card.getStyleClass().add("overlay-card");
        card.setPrefWidth(380);
        card.setMaxWidth(380);
        card.setMinHeight(Region.USE_PREF_SIZE);
        card.setMaxHeight(Region.USE_PREF_SIZE);

        closeButton.setOnAction(e -> {
            root.getChildren().remove(card);
            content.setDisable(false);
        });

        editButton.setOnAction(e ->
                SceneManager.switchTo(new EditExpenseView(expense).getScene(), "CLO - Edit Expense")
        );

        deleteButton.setOnAction(e -> {
            root.getChildren().remove(card);

            VBox confirmationCard = createDeleteConfirmationCard(expense, categoryName, root, content);
            root.getChildren().add(confirmationCard);
            StackPane.setAlignment(confirmationCard, Pos.CENTER);
        });

        return card;
    }

    private Label createDetailLine(String label, String value) {
        Label line = new Label(label + ": " + value);
        line.getStyleClass().add("overlay-text");
        return line;
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
        card.setPadding(new Insets(24));
        card.getStyleClass().add("overlay-card");
        card.setPrefWidth(380);
        card.setMaxWidth(380);
        card.setMinHeight(Region.USE_PREF_SIZE);
        card.setMaxHeight(Region.USE_PREF_SIZE);

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

    public Scene getScene() {
        return scene;
    }
}