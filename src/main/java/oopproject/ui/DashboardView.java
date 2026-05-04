package oopproject.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import oopproject.model.User;
import oopproject.service.FinanceService;
import oopproject.util.CurrencyManager;
import oopproject.util.SceneManager;
import oopproject.util.SessionManager;
import oopproject.util.ThemeManager;

// Main dashboard that shows wallet summary, totals, advice, and navigation
public class DashboardView {

    private final Scene scene;
    private final FinanceService financeService = new FinanceService();

    public DashboardView() {
        User currentUser = SessionManager.getCurrentUser();

        String name = currentUser != null ? currentUser.getFullName() : "User";
        int userId = currentUser != null ? currentUser.getId() : 0;

        String welcomeText = SessionManager.isFirstLoginAfterSignup()
                ? "Welcome, " + name + "!"
                : "Welcome back, " + name + "!";

        double totalIncome = financeService.getTotalIncome(userId);
        double totalExpenses = financeService.getTotalExpenses(userId);
        double balance = totalIncome - totalExpenses;

        BorderPane dashboardContent = new BorderPane();
        dashboardContent.getStyleClass().add("dashboard-root");

        Label appLabel = new Label("CLO");
        appLabel.getStyleClass().add("mini-app-title");

        Label welcomeLabel = new Label(welcomeText);
        welcomeLabel.getStyleClass().add("dashboard-title");

        Label subLabel = new Label(generateDashboardSubtitle(totalIncome, totalExpenses));
        subLabel.getStyleClass().add("dashboard-subtitle");

        VBox titleBox = new VBox(6, appLabel, welcomeLabel, subLabel);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        Button currencyButton = new Button(CurrencyManager.getCurrencyLabel());
        currencyButton.getStyleClass().add("top-button");
        currencyButton.setOnAction(e -> {
            CurrencyManager.toggleCurrency();
            SceneManager.switchTo(new DashboardView().getScene(), "CLO - Dashboard");
        });

        Button profileButton = new Button("Profile");
        profileButton.getStyleClass().add("top-button");
        profileButton.setOnAction(e ->
                SceneManager.switchTo(new ProfileView().getScene(), "CLO - Profile")
        );

        Button settingsButton = new Button("Settings");
        settingsButton.getStyleClass().add("top-button");
        settingsButton.setOnAction(e ->
                SceneManager.switchTo(new SettingsView().getScene(), "CLO - Settings")
        );

        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("top-button");

        HBox topRight = new HBox(10, currencyButton, profileButton, settingsButton, logoutButton);
        topRight.setAlignment(Pos.CENTER_RIGHT);

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        HBox topBar = new HBox(titleBox, topSpacer, topRight);
        topBar.setPadding(new Insets(20, 35, 10, 35));
        topBar.setAlignment(Pos.CENTER_LEFT);

        dashboardContent.setTop(topBar);

        VBox leftPanel = new VBox(14,
                createInfoCard("Total Income", formatAmount(totalIncome)),
                createInfoCard("Total Expenses", formatAmount(totalExpenses)),
                createInfoCard("Balance", formatAmount(balance)),
                createInfoCard("USD → LBP", financeService.getFormattedLbpRate())
        );
        leftPanel.setPrefWidth(240);
        leftPanel.setAlignment(Pos.TOP_CENTER);

        VBox walletPanel = new WalletView().getRoot();

        VBox rightPanel = new VBox(14,
                createSideCard("Financial Health", calculateHealthText(balance)),
                createSideCard("Advice", generateAdvice(totalIncome, totalExpenses)),
                createSideCard("Motivation", generateMotivation(balance))
        );
        rightPanel.setPrefWidth(260);
        rightPanel.setAlignment(Pos.TOP_CENTER);

        HBox centerLayout = new HBox(24, leftPanel, walletPanel, rightPanel);
        centerLayout.setPadding(new Insets(8, 35, 10, 35));
        centerLayout.setAlignment(Pos.TOP_CENTER);

        dashboardContent.setCenter(centerLayout);

        Button addIncomeButton = createBottomButton("Add Income");
        Button addExpenseButton = createBottomButton("Add Expense");
        Button insightsButton = createBottomButton("Insights");

        addIncomeButton.setOnAction(e ->
                SceneManager.switchTo(new AddIncomeView().getScene(), "CLO - Add Income")
        );

        addExpenseButton.setOnAction(e ->
                SceneManager.switchTo(new AddExpenseView().getScene(), "CLO - Add Expense")
        );

        insightsButton.setOnAction(e ->
                SceneManager.switchTo(new InsightsView().getScene(), "CLO - Insights")
        );

        HBox bottomBar = new HBox(10, addIncomeButton, addExpenseButton, insightsButton);
        bottomBar.setAlignment(Pos.CENTER);
        bottomBar.setPadding(new Insets(6, 20, 14, 20));

        dashboardContent.setBottom(bottomBar);

        StackPane root = new StackPane(dashboardContent);

        VBox logoutCard = createLogoutCard(dashboardContent);
        logoutCard.setVisible(false);
        logoutCard.setManaged(false);

        root.getChildren().add(logoutCard);
        StackPane.setAlignment(logoutCard, Pos.CENTER);

        logoutButton.setOnAction(e -> {
            logoutCard.setVisible(true);
            logoutCard.setManaged(true);
            dashboardContent.setDisable(true);
        });

        scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource(ThemeManager.getThemeStylesheet()).toExternalForm());
    }

    private VBox createInfoCard(String title, String value) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title");

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("card-value");

        VBox box = new VBox(8, titleLabel, valueLabel);
        box.setPadding(new Insets(16));
        box.getStyleClass().add("info-card");
        return box;
    }

    private VBox createSideCard(String title, String value) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title");

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("side-card-value");

        VBox box = new VBox(8, titleLabel, valueLabel);
        box.setPadding(new Insets(16));
        box.getStyleClass().add("side-card");
        return box;
    }

    private Button createBottomButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("bottom-button");
        return button;
    }

    private VBox createLogoutCard(BorderPane dashboardContent) {
        Label title = new Label("Log out?");
        title.getStyleClass().add("overlay-title");

        Label text = new Label("Your session will end and you will return to the login screen.");
        text.getStyleClass().add("overlay-text");
        text.setWrapText(true);

        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("secondary-button");

        Button confirmButton = new Button("Log out");
        confirmButton.getStyleClass().add("primary-button");

        HBox actions = new HBox(12, cancelButton, confirmButton);
        actions.setAlignment(Pos.CENTER);

        VBox card = new VBox(14, title, text, actions);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(22));
        card.getStyleClass().add("overlay-card");

        card.setPrefWidth(380);
        card.setMaxWidth(380);
        card.setMinHeight(Region.USE_PREF_SIZE);
        card.setMaxHeight(Region.USE_PREF_SIZE);

        cancelButton.setOnAction(e -> {
            card.setVisible(false);
            card.setManaged(false);
            dashboardContent.setDisable(false);
        });

        confirmButton.setOnAction(e -> {
            SessionManager.logout();
            SceneManager.switchTo(new LoginView().getScene(), "CLO - Login");
        });

        return card;
    }

    private String formatAmount(double amount) {
        return CurrencyManager.formatAmount(amount);
    }

    private String generateDashboardSubtitle(double income, double expenses) {
        if (income == 0 && expenses == 0) {
            return "Your wallet is ready for your first transaction.";
        }
        return "Your financial space is looking calm, clear, and in control.";
    }

    private String calculateHealthText(double balance) {
        if (balance > 500) {
            return "Strong";
        } else if (balance >= 0) {
            return "Stable";
        } else {
            return "Needs Attention";
        }
    }

    private String generateAdvice(double income, double expenses) {
        if (income == 0 && expenses == 0) {
            return "Start by adding your first income or expense.";
        } else if (expenses > income) {
            return "Your expenses are higher than your income. Try reducing non-essential spending.";
        } else if (income - expenses < 100) {
            return "You're close to your spending limit. Review your weekly purchases.";
        } else {
            return "You're managing well. Consider saving more this month.";
        }
    }

    private String generateMotivation(double balance) {
        if (balance > 500) {
            return "\"You're building great financial habits.\"";
        } else if (balance >= 0) {
            return "\"Small progress is still progress.\"";
        } else {
            return "\"Every reset is a new chance to improve.\"";
        }
    }

    public Scene getScene() {
        return scene;
    }
}