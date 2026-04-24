package oopproject.service;

import oopproject.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

// Handles profile updates and profile reloading
public class ProfileService {

    // Updates editable profile fields
    public boolean updateProfile(int userId, LocalDate dateOfBirth, String studyField, boolean employed, String jobTitle) {
        String sql = """
                UPDATE users
                SET date_of_birth = ?, study_field = ?, employed = ?, job_title = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseService.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, dateOfBirth != null ? dateOfBirth.toString() : null);
            preparedStatement.setString(2, studyField);
            preparedStatement.setInt(3, employed ? 1 : 0);
            preparedStatement.setString(4, jobTitle);
            preparedStatement.setInt(5, userId);

            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Reloads the full user data after profile update
    public User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection connection = DatabaseService.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, userId);
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
}