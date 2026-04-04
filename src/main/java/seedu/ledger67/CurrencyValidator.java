package seedu.ledger67;

import java.util.Arrays;
import java.util.List;

/**
 * Validates whether a given currency string belongs to the approved list of
 * currencies.
 */
public class CurrencyValidator {
    private static final List<String> APPROVED_CURRENCIES = Arrays.asList("SGD", "USD", "EUR");

    public static String validateAndGet(String currency) {
        assert currency != null : "Currency cannot be null.";
        String upperCurrency = currency.trim().toUpperCase();
        if (!APPROVED_CURRENCIES.contains(upperCurrency)) {
            throw new IllegalArgumentException("Currency must be one of: SGD, USD, EUR.");
        }
        return upperCurrency;
    }
}
