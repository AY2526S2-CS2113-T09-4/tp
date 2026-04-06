package seedu.ledger67;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * Configuration constants for the Ledger67 application.
 * Centralizes all hardcoded values for easier maintenance and consistency.
 */
public class Config {
    
    // Date and Time Configuration
    public static final String DATE_FORMAT_PATTERN = "dd/MM/yyyy";
    public static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN);
    
    // Currency Configuration
    public static final List<String> SUPPORTED_CURRENCIES = 
        Arrays.asList("SGD", "USD", "EUR");
    public static final String BASE_CURRENCY = "EUR";
    
    // Account Configuration
    public static final List<String> VALID_ACCOUNT_ROOTS = 
        Arrays.asList("Assets", "Liabilities", "Equity", "Income", "Expenses");
    public static final String ACCOUNT_SEPARATOR = ":";
    
    // File and Storage Configuration
    public static final String TRANSACTIONS_FILE_PATH = "data/ledger.txt";
    public static final String EXCHANGE_RATES_FILE_PATH = "data/exchange-rates.json";
    public static final String BALANCE_SHEET_FILE_PATH = "data/balance-sheet.csv";
    public static final String DATA_DIRECTORY = "data/";
    
    // Application Constants
    public static final String APPLICATION_NAME = "Ledger67";
    public static final String APPLICATION_DESCRIPTION = 
        "A double-entry book-keeping CLI program that ensures your accounts are BALANCED!";
    
    // Preset Configuration
    public static final List<String> VALID_PRESET_TYPES = 
        Arrays.asList("DAILYEXPENSE", "INCOME", "BUYINGSTOCKS");
    
    // UI Configuration
    public static final String PROMPT_PREFIX = "Enter Command: ";
    public static final String EXIT_COMMAND = "exit";
    public static final String HELP_COMMAND = "help";
    
    // Error Message Templates
    public static final String ERROR_INVALID_DATE_FORMAT = 
        "Date must be in DD/MM/YYYY format. Example: 18/03/2026";
    public static final String ERROR_INVALID_CURRENCY = 
        "Currency must be one of: %s";
    public static final String ERROR_INVALID_ACCOUNT_ROOT = 
        "Account root must be one of: %s. Example: Assets:Cash";
    public static final String ERROR_MISSING_REQUIRED_FIELD = 
        "Missing required field: %s";
    public static final String ERROR_INVALID_NUMBER_FORMAT = 
        "Amount must be a valid number. Example: 45.50";
    public static final String ERROR_INVALID_TRANSACTION_ID = 
        "Transaction ID must be a positive integer";
    
    // Success Messages
    public static final String SUCCESS_TRANSACTION_ADDED = 
        "Transaction added successfully";
    public static final String SUCCESS_TRANSACTION_EDITED = 
        "Transaction edited successfully";
    public static final String SUCCESS_TRANSACTION_DELETED = 
        "Successfully deleted Transaction %d";
    public static final String SUCCESS_EXCHANGE_RATES_REFRESHED = 
        "Exchange rates refreshed successfully for %s";
    
    // Help Text Constants
    public static final String HELP_HEADER = "=== Ledger67 Help ===";
    public static final String HELP_ADD_MANUAL_FORMAT = 
        "Format (Manual): add -date DATE -desc DESCRIPTION -p POSTING1 -p POSTING2 -c CURRENCY";
    public static final String HELP_ADD_PRESET_FORMAT = 
        "Format (Preset): add -date DATE -preset TYPE AMOUNT -c CURRENCY";
    public static final String HELP_ACCOUNTING_EQUATION = 
        "ASSETS = EQUITY - LIABILITIES + (INCOME - EXPENSES)";
    
    // Validation Methods
    public static boolean isValidCurrency(String currency) {
        return SUPPORTED_CURRENCIES.contains(currency.toUpperCase());
    }
    
    public static boolean isValidAccountRoot(String accountRoot) {
        return VALID_ACCOUNT_ROOTS.contains(accountRoot);
    }
    
    public static boolean isValidPresetType(String presetType) {
        return VALID_PRESET_TYPES.contains(presetType.toUpperCase());
    }
    
    public static String getSupportedCurrenciesString() {
        return String.join(", ", SUPPORTED_CURRENCIES);
    }
    
    public static String getValidAccountRootsString() {
        return String.join(", ", VALID_ACCOUNT_ROOTS);
    }
    
    public static String getValidPresetTypesString() {
        return String.join(", ", VALID_PRESET_TYPES);
    }
    
    // Private constructor to prevent instantiation
    private Config() {
        throw new AssertionError("Config class should not be instantiated.");
    }
}