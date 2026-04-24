package oopproject.util;

import oopproject.model.User;

// Keeps track of the logged-in user across scenes
public class SessionManager {
    private static User currentUser;
    private static boolean firstLoginAfterSignup;

    private SessionManager() {
    }

    // Saves the current logged-in user
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    // Returns the current logged-in user
    public static User getCurrentUser() {
        return currentUser;
    }

    // Checks whether a user session exists
    public static boolean isUserLoggedIn() {
        return currentUser != null;
    }

    // Clears session data on logout
    public static void logout() {
        currentUser = null;
        firstLoginAfterSignup = false;
    }

    // Used to show "Welcome" after signup and "Welcome back" after login
    public static boolean isFirstLoginAfterSignup() {
        return firstLoginAfterSignup;
    }

    public static void setFirstLoginAfterSignup(boolean value) {
        firstLoginAfterSignup = value;
    }
}