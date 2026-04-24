package oopproject.util;

// Stores and provides the current app theme
public class ThemeManager {

    private static boolean darkMode = false;

    private ThemeManager() {
    }

    public static boolean isDarkMode() {
        return darkMode;
    }

    public static void setDarkMode(boolean value) {
        darkMode = value;
    }

    public static String getThemeStylesheet() {
        return darkMode ? "/styles/dark.css" : "/styles/app.css";
    }
}