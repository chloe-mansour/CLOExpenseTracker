package oopproject.service;

import oopproject.model.Expense;
import oopproject.model.Income;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Handles all finance-related database operations
public class FinanceService {

    private final SettingsService settingsService = new SettingsService();

    // Saves a new income transaction
    public void addIncome(Income income) {
        String sql = """
                INSERT INTO income(user_id, source, title, amount, date, note)
                VALUES(?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseService.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, income.getUserId());
            preparedStatement.setString(2, income.getSource());
            preparedStatement.setString(3, income.getTitle());
            preparedStatement.setDouble(4, income.getAmount());
            preparedStatement.setString(5, income.getDate() != null ? income.getDate().toString() : null);
            preparedStatement.setString(6, income.getNote());

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Saves a new expense transaction
    public void addExpense(Expense expense) {
        String sql = """
                INSERT INTO expenses(user_id, category, title, amount, date, note)
                VALUES(?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseService.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, expense.getUserId());
            preparedStatement.setString(2, expense.getCategory());
            preparedStatement.setString(3, expense.getTitle());
            preparedStatement.setDouble(4, expense.getAmount());
            preparedStatement.setString(5, expense.getDate() != null ? expense.getDate().toString() : null);
            preparedStatement.setString(6, expense.getNote());

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Returns total income for one user
    public double getTotalIncome(int userId) {
        String sql = "SELECT SUM(amount) FROM income WHERE user_id = ?";
        return getTotal(sql, userId);
    }

    // Returns total expenses for one user
    public double getTotalExpenses(int userId) {
        String sql = "SELECT SUM(amount) FROM expenses WHERE user_id = ?";
        return getTotal(sql, userId);
    }

    // Returns total for one category
    public double getCategoryTotal(int userId, String category) {
        String sql = "SELECT SUM(amount) FROM expenses WHERE user_id = ? AND category = ?";

        try (Connection connection = DatabaseService.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2, category);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getDouble(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    // Returns total income between two dates
    public double getTotalIncomeBetweenDates(int userId, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        String sql = """
            SELECT SUM(amount) FROM income
            WHERE user_id = ? AND date BETWEEN ? AND ?
            """;

        return getTotalBetweenDates(sql, userId, startDate, endDate);
    }

    // Returns total expenses between two dates
    public double getTotalExpensesBetweenDates(int userId, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        String sql = """
            SELECT SUM(amount) FROM expenses
            WHERE user_id = ? AND date BETWEEN ? AND ?
            """;

        return getTotalBetweenDates(sql, userId, startDate, endDate);
    }

    // Returns category totals between two dates
    public java.util.Map<String, Double> getCategoryTotalsBetweenDates(
            int userId,
            java.time.LocalDate startDate,
            java.time.LocalDate endDate
    ) {
        java.util.Map<String, Double> totals = new java.util.LinkedHashMap<>();

        totals.put("Essentials", getCategoryTotalBetweenDates(userId, "Essentials", startDate, endDate));
        totals.put("Education & Work", getCategoryTotalBetweenDates(userId, "Education & Work", startDate, endDate));
        totals.put("Savings & Investments", getCategoryTotalBetweenDates(userId, "Savings & Investments", startDate, endDate));
        totals.put("Lifestyle & Fun", getCategoryTotalBetweenDates(userId, "Lifestyle & Fun", startDate, endDate));
        totals.put("Health & Emergencies", getCategoryTotalBetweenDates(userId, "Health & Emergencies", startDate, endDate));

        return totals;
    }

    // Returns one category total between dates
    private double getCategoryTotalBetweenDates(
            int userId,
            String category,
            java.time.LocalDate startDate,
            java.time.LocalDate endDate
    ) {
        String sql = """
            SELECT SUM(amount) FROM expenses
            WHERE user_id = ? AND category = ? AND date BETWEEN ? AND ?
            """;

        try (Connection connection = DatabaseService.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2, category);
            preparedStatement.setString(3, startDate.toString());
            preparedStatement.setString(4, endDate.toString());

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getDouble(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    // Shared helper for date-range totals
    private double getTotalBetweenDates(
            String sql,
            int userId,
            java.time.LocalDate startDate,
            java.time.LocalDate endDate
    ) {
        try (Connection connection = DatabaseService.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2, startDate.toString());
            preparedStatement.setString(3, endDate.toString());

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getDouble(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    // Returns all expenses in one category for one user
    public List<Expense> getExpensesByCategory(int userId, String category) {
        List<Expense> expenses = new ArrayList<>();

        String sql = """
                SELECT * FROM expenses
                WHERE user_id = ? AND category = ?
                ORDER BY date DESC, id DESC
                """;

        try (Connection connection = DatabaseService.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2, category);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                expenses.add(new Expense(
                        resultSet.getInt("id"),
                        resultSet.getInt("user_id"),
                        resultSet.getString("category"),
                        resultSet.getString("title"),
                        resultSet.getDouble("amount"),
                        resultSet.getString("date") != null ? java.time.LocalDate.parse(resultSet.getString("date")) : null,
                        resultSet.getString("note")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return expenses;
    }

    // Deletes one expense transaction by its ID
    public boolean deleteExpense(int expenseId) {
        String sql = "DELETE FROM expenses WHERE id = ?";

        try (Connection connection = DatabaseService.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, expenseId);
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Updates an existing expense transaction
    public boolean updateExpense(Expense expense) {
        String sql = """
            UPDATE expenses
            SET category = ?, title = ?, amount = ?, date = ?, note = ?
            WHERE id = ?
            """;

        try (Connection connection = DatabaseService.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, expense.getCategory());
            preparedStatement.setString(2, expense.getTitle());
            preparedStatement.setDouble(3, expense.getAmount());
            preparedStatement.setString(4, expense.getDate() != null ? expense.getDate().toString() : null);
            preparedStatement.setString(5, expense.getNote());
            preparedStatement.setInt(6, expense.getId());

            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Returns the formatted current LBP rate for the dashboard
    public String getFormattedLbpRate() {
        return String.format("%.0f", settingsService.getUsdToLbpRate());
    }

    // Shared helper used by total income and total expense queries
    private double getTotal(String sql, int userId) {
        try (Connection connection = DatabaseService.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getDouble(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;

    }
    // Returns total expenses for each category
    public java.util.Map<String, Double> getCategoryTotals(int userId) {
        java.util.Map<String, Double> totals = new java.util.LinkedHashMap<>();

        totals.put("Essentials", getCategoryTotal(userId, "Essentials"));
        totals.put("Education & Work", getCategoryTotal(userId, "Education & Work"));
        totals.put("Savings & Investments", getCategoryTotal(userId, "Savings & Investments"));
        totals.put("Lifestyle & Fun", getCategoryTotal(userId, "Lifestyle & Fun"));
        totals.put("Health & Emergencies", getCategoryTotal(userId, "Health & Emergencies"));

        return totals;
    }
}