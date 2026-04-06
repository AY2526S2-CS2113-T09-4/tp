package seedu.ledger67;

/**
 * Centralized error messages for the Ledger67 application.
 * Provides consistent error messaging throughout the application.
 */
public class ErrorMessages {
    
    // Command Parsing Errors
    public static final String UNKNOWN_COMMAND = 
        "Unknown command. Use add, list, edit, delete, clear, convert, rates, help, balance, or exit.";
    
    public static final String INVALID_COMMAND_FORMAT = 
        "Invalid command format. Use: %s";
    
    public static final String MISSING_ARGUMENTS = 
        "Missing arguments for %s command.";
    
    // Transaction Errors
    public static final String TRANSACTION_NOT_FOUND = 
        "Transaction with ID %d not found.";
    
    public static final String INVALID_TRANSACTION_ID = 
        "Transaction ID must be a positive integer.";
    
    public static final String TRANSACTION_ALREADY_EXISTS = 
        "Transaction with ID %d already exists.";
    
    // Date and Time Errors
    public static final String INVALID_DATE_FORMAT = 
        "Date must be in DD/MM/YYYY format. Example: 18/03/2026";
    
    public static final String INVALID_DATE_RANGE = 
        "End date must be after or equal to start date.";
    
    public static final String FUTURE_DATE_NOT_ALLOWED = 
        "Date cannot be in the future.";
    
    // Currency Errors
    public static final String INVALID_CURRENCY = 
        "Currency must be one of: %s";
    
    public static final String SAME_CURRENCY_CONVERSION = 
        "Source and target currencies cannot be the same.";
    
    public static final String CURRENCY_NOT_SUPPORTED = 
        "Currency '%s' is not supported. Supported currencies: %s";
    
    // Account Errors
    public static final String INVALID_ACCOUNT_FORMAT = 
        "Account must follow format: Root:Subcategory. Valid roots: %s";
    
    public static final String INVALID_ACCOUNT_ROOT = 
        "Account root '%s' is invalid. Valid roots: %s";
    
    public static final String ACCOUNT_NOT_FOUND = 
        "Account '%s' not found.";
    
    // Amount and Number Errors
    public static final String INVALID_AMOUNT_FORMAT = 
        "Amount must be a valid number. Example: 45.50";
    
    public static final String NEGATIVE_AMOUNT_NOT_ALLOWED = 
        "Amount cannot be negative.";
    
    public static final String ZERO_AMOUNT_NOT_ALLOWED = 
        "Amount cannot be zero.";
    
    public static final String AMOUNT_TOO_LARGE = 
        "Amount exceeds maximum allowed value of %,.2f";
    
    // Posting and Balance Errors
    public static final String UNBALANCED_TRANSACTION = 
        "Transaction is not balanced. Debits must equal credits.";
    
    public static final String MISSING_POSTINGS = 
        "Either -preset or at least one posting (-p) is required.";
    
    public static final String INVALID_POSTING_FORMAT = 
        "Invalid posting format. Use: -p \"Account Amount\"";
    
    public static final String DUPLICATE_ACCOUNT_IN_POSTING = 
        "Duplicate account '%s' in postings is not allowed.";
    
    // Preset Errors
    public static final String INVALID_PRESET_TYPE = 
        "Invalid preset type '%s'. Valid types: %s";
    
    public static final String PRESET_GENERATION_FAILED = 
        "Failed to generate postings for preset '%s'.";
    
    // Filtering Errors
    public static final String NO_FILTERS_PROVIDED = 
        "No valid filters provided. Use -begin, -end, -match, or -acc.";
    
    public static final String INVALID_REGEX_PATTERN = 
        "Invalid regular expression pattern: %s";
    
    public static final String NO_TRANSACTIONS_FOUND = 
        "No transactions found matching the specified criteria.";
    
    // File and Storage Errors
    public static final String FILE_READ_ERROR = 
        "Error reading file: %s";
    
    public static final String FILE_WRITE_ERROR = 
        "Error writing to file: %s";
    
    public static final String FILE_NOT_FOUND = 
        "File not found: %s";
    
    public static final String DATA_CORRUPTION = 
        "Data corruption detected in file: %s";
    
    // Exchange Rate Errors
    public static final String EXCHANGE_RATE_FETCH_FAILED = 
        "Failed to fetch exchange rates. Using fallback data.";
    
    public static final String INVALID_EXCHANGE_RATE = 
        "Invalid exchange rate for currency '%s'.";
    
    public static final String EXCHANGE_RATE_NOT_AVAILABLE = 
        "Exchange rate not available for currency pair %s/%s.";
    
    // Confirmation Workflow Errors
    public static final String NO_PENDING_CONFIRMATION = 
        "No pending confirmation to process.";
    
    public static final String INVALID_CONFIRMATION_COMMAND = 
        "After 'convert transaction', use only 'confirm' to store the viewed transaction.";
    
    public static final String INVALID_LIST_CONFIRMATION = 
        "After 'list transaction -to ...', use 'confirm all' or 'confirm ID'.";
    
    // UI Assist Errors
    public static final String UI_ASSIST_COMMAND_NOT_SUPPORTED = 
        "UI Assist is not supported for command: %s";
    
    public static final String UI_ASSIST_INPUT_VALIDATION_FAILED = 
        "Invalid input provided during UI Assist: %s";
    
    // Balance Sheet Errors
    public static final String BALANCE_SHEET_GENERATION_FAILED = 
        "Failed to generate balance sheet.";
    
    public static final String INVALID_ACCOUNT_FOR_BALANCE = 
        "Invalid account for balance command: %s";
    
    // Helper methods for formatted error messages
    public static String getInvalidCurrencyMessage(String invalidCurrency) {
        return String.format(CURRENCY_NOT_SUPPORTED, 
            invalidCurrency, Config.getSupportedCurrenciesString());
    }
    
    public static String getInvalidAccountRootMessage(String invalidRoot) {
        return String.format(INVALID_ACCOUNT_ROOT, 
            invalidRoot, Config.getValidAccountRootsString());
    }
    
    public static String getInvalidPresetTypeMessage(String invalidPreset) {
        return String.format(INVALID_PRESET_TYPE, 
            invalidPreset, Config.getValidPresetTypesString());
    }
    
    public static String getTransactionNotFoundMessage(int id) {
        return String.format(TRANSACTION_NOT_FOUND, id);
    }
    
    public static String getFileErrorMessage(String operation, String filename) {
        return String.format("Error %s file: %s", operation, filename);
    }
    
    // Private constructor to prevent instantiation
    private ErrorMessages() {
        throw new AssertionError("ErrorMessages class should not be instantiated.");
    }
}