package oopproject.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// Handles app settings such as currency rate and theme preference
public class SettingsService {

    public double getUsdToLbpRate() {
        String sql = "SELECT usd_to_lbp_rate FROM settings WHERE id = 1";

        try (Connection connection = DatabaseService.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getDouble("usd_to_lbp_rate");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 89500;
    }

    public void setUsdToLbpRate(double rate) {
        String sql = """
                UPDATE settings
                SET usd_to_lbp_rate = ?
                WHERE id = 1
                """;

        try (Connection connection = DatabaseService.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setDouble(1, rate);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isDarkModeEnabled() {
        String sql = "SELECT dark_mode FROM settings WHERE id = 1";

        try (Connection connection = DatabaseService.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("dark_mode") == 1;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void setDarkModeEnabled(boolean enabled) {
        String sql = """
                UPDATE settings
                SET dark_mode = ?
                WHERE id = 1
                """;

        try (Connection connection = DatabaseService.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, enabled ? 1 : 0);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}