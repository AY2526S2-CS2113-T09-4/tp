package seedu.ledger67;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Utility class for validating user input and data consistency.
 * Centralizes validation logic to ensure consistency across the application.
 */
public class ValidationUtils {
    
    // Validation constants
    private static final double MAX_AMOUNT = 1_000_000_000.00; // 1 billion
    private static final int MAX_DESCRIPTION_LENGTH = 200;
    private static final int MAX_ACCOUNT_DEPTH = 5;
    
    /**
     * Validates and parses a date string.
     * 
     * @param dateStr the date string to parse
     * @param fieldName the name of the field for error messages
     * @return the parsed LocalDate
     * @throws IllegalArgumentException if the date is invalid
     */
    public static LocalDate parseDate(String dateStr, String fieldName) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new IllegalArgumentException(
                String.format(Config.ERROR_MISSING_REQUIRED_FIELD, fieldName));
        }
        
        try {
            return LocalDate.parse(dateStr.trim(), Config.DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(Config.ERROR_INVALID_DATE_FORMAT);
        }
    }
    
    /**
     * Validates and parses an amount string.
     * 
     * @param amountStr the amount string to parse
     * @param fieldName the name of the field for error messages
     * @return the parsed double amount
     * @throws IllegalArgumentException if the amount is invalid
     */
    public static double parseAmount(String amountStr, String fieldName) {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            throw new IllegalArgumentException(
                String.format(Config.ERROR_MISSING_REQUIRED_FIELD, fieldName));
        }
        
        try {
            double amount = Double.parseDouble(amountStr.trim());
            validateAmount(amount, fieldName);
            return amount;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(Config.ERROR_INVALID_NUMBER_FORMAT);
        }
    }
    
    /**
     * Validates an amount value.
     * 
     * @param amount the amount to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if the amount is invalid
     */
    public static void validateAmount(double amount, String fieldName) {
        if (Double.isNaN(amount) || Double.isInfinite(amount)) {
            throw new IllegalArgumentException(
                String.format("Invalid %s amount: %f", fieldName, amount));
        }
        
        if (amount < 0) {
            throw new IllegalArgumentException(
                String.format("%s amount cannot be negative: %.2f", fieldName, amount));
        }
        
        if (amount == 0) {
            throw new IllegalArgumentException(
                String.format("%s amount cannot be zero", fieldName));
        }
        
        if (amount > MAX_AMOUNT) {
            throw new IllegalArgumentException(
                String.format("%s amount exceeds maximum allowed value of %,.2f", 
                    fieldName, MAX_AMOUNT));
        }
        
        // Validate precision (optional)
        String amountStr = String.valueOf(amount);
        if (amountStr.contains(".") && amountStr.split("\\.")[1].length() > 2) {
            throw new IllegalArgumentException(
                String.format("%s amount can have at most 2 decimal places: %.2f", 
                    fieldName, amount));
        }
    }
    
    /**
     * Validates a currency code.
     * 
     * @param currency the currency code to validate
     * @param fieldName the name of the field for error messages
     * @return the normalized currency code (uppercase)
     * @throws IllegalArgumentException if the currency is invalid
     */
    public static String validateCurrency(String currency, String fieldName) {
        if (currency == null || currency.trim().isEmpty()) {
            throw new IllegalArgumentException(
                String.format(Config.ERROR_MISSING_REQUIRED_FIELD, fieldName));
        }
        
        String normalizedCurrency = currency.trim().toUpperCase();
        
        if (!Config.isValidCurrency(normalizedCurrency)) {
            throw new IllegalArgumentException(
                String.format(Config.ERROR_INVALID_CURRENCY, 
                    Config.getSupportedCurrenciesString()));
        }
        
        return normalizedCurrency;
    }
    
    /**
     * Validates an account name.
     * 
     * @param account the account name to validate
     * @param fieldName the name of the field for error messages
     * @return the normalized account name
     * @throws IllegalArgumentException if the account is invalid
     */
    public static String validateAccount(String account, String fieldName) {
        if (account == null || account.trim().isEmpty()) {
            throw new IllegalArgumentException(
                String.format(Config.ERROR_MISSING_REQUIRED_FIELD, fieldName));
        }
        
        String normalizedAccount = account.trim();
        
        // Check if account contains separator
        if (!normalizedAccount.contains(Config.ACCOUNT_SEPARATOR)) {
            throw new IllegalArgumentException(
                String.format(ErrorMessages.INVALID_ACCOUNT_FORMAT,
                    Config.getValidAccountRootsString()));
        }
        
        // Split and validate root
        String[] parts = normalizedAccount.split(Config.ACCOUNT_SEPARATOR);
        String root = parts[0];
        
        if (!Config.isValidAccountRoot(root)) {
            throw new IllegalArgumentException(
                ErrorMessages.getInvalidAccountRootMessage(root));
        }
        
        // Validate depth
        if (parts.length > MAX_ACCOUNT_DEPTH) {
            throw new IllegalArgumentException(
                String.format("Account hierarchy too deep. Maximum depth is %d levels.",
                    MAX_ACCOUNT_DEPTH));
        }
        
        // Validate subcategory names
        for (int i = 1; i < parts.length; i++) {
            String subcategory = parts[i];
            if (subcategory.trim().isEmpty()) {
                throw new IllegalArgumentException(
                    "Account subcategory cannot be empty.");
            }
            
            if (!subcategory.matches("^[a-zA-Z0-9\\s]+$")) {
                throw new IllegalArgumentException(
                    String.format("Invalid subcategory name: %s. " +
                        "Only letters, numbers, and spaces allowed.", subcategory));
            }
        }
        
        return normalizedAccount;
    }
    
    /**
     * Validates a transaction ID.
     * 
     * @param idStr the transaction ID string to validate
     * @param fieldName the name of the field for error messages
     * @return the parsed integer ID
     * @throws IllegalArgumentException if the ID is invalid
     */
    public static int validateTransactionId(String idStr, String fieldName) {
        if (idStr == null || idStr.trim().isEmpty()) {
            throw new IllegalArgumentException(
                String.format(Config.ERROR_MISSING_REQUIRED_FIELD, fieldName));
        }
        
        try {
            int id = Integer.parseInt(idStr.trim());
            
            if (id <= 0) {
                throw new IllegalArgumentException(Config.ERROR_INVALID_TRANSACTION_ID);
            }
            
            return id;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(Config.ERROR_INVALID_TRANSACTION_ID);
        }
    }
    
    /**
     * Validates a description.
     * 
     * @param description the description to validate
     * @param fieldName the name of the field for error messages
     * @return the normalized description
     * @throws IllegalArgumentException if the description is invalid
     */
    public static String validateDescription(String description, String fieldName) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException(
                String.format(Config.ERROR_MISSING_REQUIRED_FIELD, fieldName));
        }
        
        String normalizedDescription = description.trim();
        
        if (normalizedDescription.length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Description too long. Maximum length is %d characters.",
                    MAX_DESCRIPTION_LENGTH));
        }
        
        return normalizedDescription;
    }
    
    /**
     * Validates a regular expression pattern.
     * 
     * @param regex the regular expression to validate
     * @param fieldName the name of the field for error messages
     * @return the compiled Pattern
     * @throws IllegalArgumentException if the regex is invalid
     */
    public static Pattern validateRegex(String regex, String fieldName) {
        if (regex == null || regex.trim().isEmpty()) {
            return null;
        }
        
        try {
            return Pattern.compile(regex.trim(), Pattern.CASE_INSENSITIVE);
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException(
                String.format(ErrorMessages.INVALID_REGEX_PATTERN, regex));
        }
    }
    
    /**
     * Validates that a list is not empty.
     * 
     * @param list the list to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if the list is empty
     */
    public static <T> void validateListNotEmpty(List<T> list, String fieldName) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException(
                String.format("%s cannot be empty", fieldName));
        }
    }
    
    /**
     * Validates that a string is not null or empty.
     * 
     * @param value the string to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if the string is null or empty
     */
    public static void validateStringNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(
                String.format(Config.ERROR_MISSING_REQUIRED_FIELD, fieldName));
        }
    }
    
    /**
     * Validates that an object is not null.
     * 
     * @param obj the object to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if the object is null
     */
    public static void validateNotNull(Object obj, String fieldName) {
        if (obj == null) {
            throw new IllegalArgumentException(
                String.format("%s cannot be null", fieldName));
        }
    }
    
    /**
     * Validates a date range.
     * 
     * @param start the start date
     * @param end the end date
     * @throws IllegalArgumentException if the date range is invalid
     */
    public static void validateDateRange(LocalDate start, LocalDate end) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new IllegalArgumentException(ErrorMessages.INVALID_DATE_RANGE);
        }
    }
    
    /**
     * Validates that a date is not in the future.
     * 
     * @param date the date to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if the date is in the future
     */
    public static void validateDateNotInFuture(LocalDate date, String fieldName) {
        if (date != null && date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(ErrorMessages.FUTURE_DATE_NOT_ALLOWED);
        }
    }
    
    /**
     * Validates a preset type.
     * 
     * @param presetType the preset type to validate
     * @param fieldName the name of the field for error messages
     * @return the normalized preset type (uppercase)
     * @throws IllegalArgumentException if the preset type is invalid
     */
    public static String validatePresetType(String presetType, String fieldName) {
        if (presetType == null || presetType.trim().isEmpty()) {
            throw new IllegalArgumentException(
                String.format(Config.ERROR_MISSING_REQUIRED_FIELD, fieldName));
        }
        
        String normalizedPreset = presetType.trim().toUpperCase();
        
        if (!Config.isValidPresetType(normalizedPreset)) {
            throw new IllegalArgumentException(
                ErrorMessages.getInvalidPresetTypeMessage(normalizedPreset));
        }
        
        return normalizedPreset;
    }
    
    // Private constructor to prevent instantiation
    private ValidationUtils() {
        throw new AssertionError("ValidationUtils class should not be instantiated.");
    }
}