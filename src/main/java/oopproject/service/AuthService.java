package oopproject.service;

import oopproject.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

// Handles registration, login, and email checks
public class AuthService {

    // Saves a new user into the database
    public boolean registerUser(User user) {
        String sql = """
                INSERT INTO users(full_name, email, password, date_of_birth, study_field, employed, job_title)
                VALUES(?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseService.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, user.getFullName());
            preparedStatement.setString(2, user.getEmail());
            preparedStatement.setString(3, user.getPassword());
            preparedStatement.setString(4, user.getDateOfBirth() != null ? user.getDateOfBirth().toString() : null);
            preparedStatement.setString(5, user.getStudyField());
            preparedStatement.setInt(6, user.isEmployed() ? 1 : 0);
            preparedStatement.setString(7, user.getJobTitle());

            preparedStatement.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Checks login credentials and returns the matching user if found
    public User loginUser(String email, String password) {
        String sql = """
                SELECT * FROM users
                WHERE email = ? AND password = ?
                """;

        try (Connection connection = DatabaseService.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, email);
            preparedStatement.setString(2, password);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String dobText = resultSet.getString("date_of_birth");
                LocalDate dateOfBirth =
                        (dobText != null && !dobText.isBlank()) ? LocalDate.parse(dobText) : null;

                return new User(
                        resultSet.getInt("id"),
                        resultSet.getString("full_name"),
                        resultSet.getString("email"),
                        resultSet.getString("password"),
                        dateOfBirth,
                        resultSet.getString("study_field"),
                        resultSet.getInt("employed") == 1,
                        resultSet.getString("job_title")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Prevents duplicate accounts with the same email
    public boolean emailExists(String email) {
        String sql = "SELECT id FROM users WHERE email = ?";

        try (Connection connection = DatabaseService.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}