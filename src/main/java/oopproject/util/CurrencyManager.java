package oopproject.util;

import oopproject.service.SettingsService;

// Manages the selected display currency across the application
public class CurrencyManager {

    private static boolean lbpMode = false;

    private CurrencyManager() {
    }

    public static boolean isLbpMode() {
        return lbpMode;
    }

    public static void toggleCurrency() {
        lbpMode = !lbpMode;
    }

    public static String getCurrencyLabel() {
        return lbpMode ? "LBP" : "USD";
    }

    public static String formatAmount(double amountInUsd) {
        if (!lbpMode) {
            return String.format("$%.2f", amountInUsd);
        }

        SettingsService settingsService = new SettingsService();
        double rate = settingsService.getUsdToLbpRate();
        double amountInLbp = amountInUsd * rate;

        return String.format("%,.0f LBP", amountInLbp);
    }
}