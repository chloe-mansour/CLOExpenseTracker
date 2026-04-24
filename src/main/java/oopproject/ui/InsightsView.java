package oopproject.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import oopproject.model.User;
import oopproject.service.FinanceService;
import oopproject.util.SceneManager;
import oopproject.util.SessionManager;
import oopproject.util.ThemeManager;

import java.time.LocalDate;
import java.util.Map;

// Shows financial progress with weekly, monthly, and yearly filters
public class InsightsView {

    private final Scene scene;
    private final FinanceService financeService = new FinanceService();

    public InsightsView() {
        scene = createScene("Monthly");
    }

    private Scene createScene(String period) {
        User currentUser = SessionManager.getCurrentUser();
        int userId = currentUser != null ? currentUser.getId() : 0;

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = getStartDate(period, endDate);

        double income = financeService.getTotalIncomeBetweenDates(userId, startDate, endDate);
        double expenses = financeService.getTotalExpensesBetweenDates(userId, startDate, endDate);
        double balance = income - expenses;

        Label title = new Label("Financial Insights");
        title.getStyleClass().add("section-title");

        Label subtitle = new Label("Viewing " + period.toLowerCase() + " progress from " + startDate + " to " + endDate + ".");
        subtitle.getStyleClass().add("dashboard-subtitle");

        Button weeklyButton = createPeriodButton("Weekly", period);
        Button monthlyButton = createPeriodButton("Monthly", period);
        Button yearlyButton = createPeriodButton("Yearly", period);

        weeklyButton.setOnAction(e ->
                SceneManager.switchTo(new InsightsView("Weekly").getScene(), "CLO - Weekly Insights")
        );

        monthlyButton.setOnAction(e ->
                SceneManager.switchTo(new InsightsView("Monthly").getScene(), "CLO - Monthly Insights")
        );

        yearlyButton.setOnAction(e ->
                SceneManager.switchTo(new InsightsView("Yearly").getScene(), "CLO - Yearly Insights")
        );

        HBox filterButtons = new HBox(12, weeklyButton, monthlyButton, yearlyButton);
        filterButtons.setAlignment(Pos.CENTER);

        VBox summaryCard = createSummaryCard(income, expenses, balance);
        PieChart categoryChart = createCategoryPieChart(userId, startDate, endDate);
        BarChart<String, Number> comparisonChart = createIncomeExpenseChart(income, expenses, balance);

        HBox chartsBox = new HBox(28, categoryChart, comparisonChart);
        chartsBox.setAlignment(Pos.CENTER);

        VBox adviceCard = createAdviceCard(period, income, expenses, balance);

        Button backButton = new Button("Back to Dashboard");
        backButton.getStyleClass().add("secondary-button");
        backButton.setOnAction(e ->
                SceneManager.switchTo(new DashboardView().getScene(), "CLO - Dashboard")
        );

        VBox root = new VBox(
                18,
                title,
                subtitle,
                filterButtons,
                summaryCard,
                chartsBox,
                adviceCard,
                backButton
        );

        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.getStyleClass().add("root-pane");

        Scene newScene = new Scene(root);
        newScene.getStylesheets().add(getClass().getResource(ThemeManager.getThemeStylesheet()).toExternalForm());

        return newScene;
    }

    // Constructor used when switching between weekly, monthly, and yearly views
    public InsightsView(String period) {
        scene = createScene(period);
    }

    private Button createPeriodButton(String text, String currentPeriod) {
        Button button = new Button(text);

        if (text.equals(currentPeriod)) {
            button.getStyleClass().add("primary-button");
        } else {
            button.getStyleClass().add("secondary-button");
        }

        return button;
    }

    private LocalDate getStartDate(String period, LocalDate endDate) {
        return switch (period) {
            case "Weekly" -> endDate.minusDays(6);
            case "Yearly" -> endDate.minusYears(1).plusDays(1);
            default -> endDate.withDayOfMonth(1);
        };
    }

    private VBox createSummaryCard(double income, double expenses, double balance) {
        Label incomeLabel = new Label("Income: " + formatAmount(income));
        incomeLabel.getStyleClass().add("side-card-value");

        Label expenseLabel = new Label("Expenses: " + formatAmount(expenses));
        expenseLabel.getStyleClass().add("side-card-value");

        Label balanceLabel = new Label("Balance: " + formatAmount(balance));
        balanceLabel.getStyleClass().add("side-card-value");

        VBox box = new VBox(8, incomeLabel, expenseLabel, balanceLabel);
        box.getStyleClass().add("info-card");
        box.setPadding(new Insets(20));
        box.setMaxWidth(760);

        return box;
    }

    private PieChart createCategoryPieChart(int userId, LocalDate startDate, LocalDate endDate) {
        PieChart chart = new PieChart();
        chart.setTitle("Expenses by Category");
        chart.setLegendVisible(true);
        chart.setLabelsVisible(true);
        chart.setPrefSize(460, 320);

        Map<String, Double> categoryTotals =
                financeService.getCategoryTotalsBetweenDates(userId, startDate, endDate);

        boolean hasData = false;

        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            if (entry.getValue() > 0) {
                chart.getData().add(new PieChart.Data(entry.getKey(), entry.getValue()));
                hasData = true;
            }
        }

        if (!hasData) {
            chart.getData().add(new PieChart.Data("No expenses yet", 1));
        }

        return chart;
    }

    private BarChart<String, Number> createIncomeExpenseChart(double income, double expenses, double balance) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Income vs Expenses");
        chart.setLegendVisible(false);
        chart.setPrefSize(460, 320);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Income", income));
        series.getData().add(new XYChart.Data<>("Expenses", expenses));
        series.getData().add(new XYChart.Data<>("Balance", Math.max(balance, 0)));

        chart.getData().add(series);

        return chart;
    }

    private VBox createAdviceCard(String period, double income, double expenses, double balance) {
        Label adviceTitle = new Label(period + " Advice");
        adviceTitle.getStyleClass().add("card-title");

        Label adviceText = new Label(generateAdvice(period, income, expenses, balance));
        adviceText.getStyleClass().add("side-card-value");
        adviceText.setWrapText(true);

        VBox adviceCard = new VBox(8, adviceTitle, adviceText);
        adviceCard.getStyleClass().add("info-card");
        adviceCard.setPadding(new Insets(20));
        adviceCard.setMaxWidth(760);

        return adviceCard;
    }

    private String generateAdvice(String period, double income, double expenses, double balance) {
        if (income == 0 && expenses == 0) {
            return "No " + period.toLowerCase() + " data yet. Add income and expenses to start tracking your progress.";
        }

        if (expenses > income) {
            return "Your " + period.toLowerCase() + " expenses are higher than your income. Review non-essential spending first.";
        }

        if (balance < 100) {
            return "Your " + period.toLowerCase() + " balance is positive but low. Try setting a smaller spending limit.";
        }

        if (balance > 500) {
            return "Great " + period.toLowerCase() + " progress. Consider saving part of this balance or planning an investment goal.";
        }

        return "You are doing well this " + period.toLowerCase() + ". Keep tracking consistently to improve your habits.";
    }

    private String formatAmount(double amount) {
        return String.format("$%.2f", amount);
    }

    public Scene getScene() {
        return scene;
    }
}