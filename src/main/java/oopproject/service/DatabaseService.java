package oopproject.service;

import java.sql.*;

// Handles database connection and table initialization
public class DatabaseService {

    private static final String URL = "jdbc:sqlite:clo_expense_tracker.db";

    private DatabaseService() {
    }

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() {
        String usersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    full_name TEXT NOT NULL,
                    email TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    date_of_birth TEXT,
                    study_field TEXT,
                    employed INTEGER,
                    job_title TEXT
                );
                """;

        String incomeTable = """
                CREATE TABLE IF NOT EXISTS income (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER,
                    source TEXT,
                    title TEXT,
                    amount REAL,
                    date TEXT,
                    note TEXT
                );
                """;

        String expenseTable = """
                CREATE TABLE IF NOT EXISTS expenses (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER,
                    category TEXT,
                    title TEXT,
                    amount REAL,
                    date TEXT,
                    note TEXT
                );
                """;

        String settingsTable = """
                CREATE TABLE IF NOT EXISTS settings (
                    id INTEGER PRIMARY KEY CHECK (id = 1),
                    usd_to_lbp_rate REAL DEFAULT 89500,
                    dark_mode INTEGER DEFAULT 0
                );
                """;

        try (Connection connection = connect();
             Statement statement = connection.createStatement()) {

            statement.execute(usersTable);
            statement.execute(incomeTable);
            statement.execute(expenseTable);
            statement.execute(settingsTable);

            addColumnIfMissing(connection, "settings", "dark_mode", "INTEGER DEFAULT 0");

            statement.execute("""
                    INSERT OR IGNORE INTO settings (id, usd_to_lbp_rate, dark_mode)
                    VALUES (1, 89500, 0)
                    """);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Adds new columns safely when the database already exists
    private static void addColumnIfMissing(Connection connection, String tableName, String columnName, String columnDefinition)
            throws SQLException {

        String checkSql = "PRAGMA table_info(" + tableName + ")";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(checkSql)) {

            while (resultSet.next()) {
                if (columnName.equalsIgnoreCase(resultSet.getString("name"))) {
                    return;
                }
            }
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDefinition);
        }
    }
}