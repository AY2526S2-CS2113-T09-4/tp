package seedu.ledger67;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Provides auto-correction suggestions for common user input errors.
 * Helps improve user experience by suggesting corrections for typos and common mistakes.
 */
public class AutoCorrector {
    
    // Common date format patterns and their corrections
    private static final Map<Pattern, String> DATE_CORRECTIONS = new HashMap<>();
    static {
        // Common date format mistakes
        DATE_CORRECTIONS.put(Pattern.compile("^(\\d{1,2})[-.](\\d{1,2})[-.](\\d{4})$"), "$1/$2/$3"); // 18-03-2026 -> 18/03/2026
        DATE_CORRECTIONS.put(Pattern.compile("^(\\d{4})[-.](\\d{1,2})[-.](\\d{1,2})$"), "$3/$2/$1"); // 2026-03-18 -> 18/03/2026
        DATE_CORRECTIONS.put(Pattern.compile("^(\\d{1,2})[/](\\d{1,2})[/](\\d{2})$"), "$1/$2/20$3"); // 18/03/26 -> 18/03/2026
        DATE_CORRECTIONS.put(Pattern.compile("^(\\d{1,2})[/](\\d{1,2})$"), "$1/$2/" + LocalDate.now().getYear()); // 18/03 -> 18/03/2026
    }
    
    // Common currency typos and their corrections
    private static final Map<String, String> CURRENCY_CORRECTIONS = new HashMap<>();
    static {
        CURRENCY_CORRECTIONS.put("SGD", "SGD"); // Exact match
        CURRENCY_CORRECTIONS.put("USD", "USD");
        CURRENCY_CORRECTIONS.put("EUR", "EUR");
        
        // Common typos
        CURRENCY_CORRECTIONS.put("SGP", "SGD");
        CURRENCY_CORRECTIONS.put("S$", "SGD");
        CURRENCY_CORRECTIONS.put("US", "USD");
        CURRENCY_CORRECTIONS.put("U$", "USD");
        CURRENCY_CORRECTIONS.put("EU", "EUR");
        CURRENCY_CORRECTIONS.put("€", "EUR");
    }
    
    // Common account root typos and their corrections
    private static final Map<String, String> ACCOUNT_ROOT_CORRECTIONS = new HashMap<>();
    static {
        for (String root : Config.VALID_ACCOUNT_ROOTS) {
            ACCOUNT_ROOT_CORRECTIONS.put(root.toLowerCase(), root);
            ACCOUNT_ROOT_CORRECTIONS.put(root.substring(0, 1).toLowerCase(), root);
        }
        
        // Common typos
        ACCOUNT_ROOT_CORRECTIONS.put("asset", "Assets");
        ACCOUNT_ROOT_CORRECTIONS.put("asstes", "Assets");
        ACCOUNT_ROOT_CORRECTIONS.put("aset", "Assets");
        ACCOUNT_ROOT_CORRECTIONS.put("liability", "Liabilities");
        ACCOUNT_ROOT_CORRECTIONS.put("liablity", "Liabilities");
        ACCOUNT_ROOT_CORRECTIONS.put("liabilites", "Liabilities");
        ACCOUNT_ROOT_CORRECTIONS.put("equities", "Equity");
        ACCOUNT_ROOT_CORRECTIONS.put("income", "Income");
        ACCOUNT_ROOT_CORRECTIONS.put("inc", "Income");
        ACCOUNT_ROOT_CORRECTIONS.put("expense", "Expenses");
        ACCOUNT_ROOT_CORRECTIONS.put("exp", "Expenses");
        ACCOUNT_ROOT_CORRECTIONS.put("expenditure", "Expenses");
    }
    
    // Common subcategory suggestions
    private static final Map<String, List<String>> COMMON_SUBCATEGORIES = new HashMap<>();
    static {
        COMMON_SUBCATEGORIES.put("Assets", Arrays.asList("Cash", "Bank", "Savings", "Investments", "Property"));
        COMMON_SUBCATEGORIES.put("Liabilities", Arrays.asList("Loan", "CreditCard", "Mortgage", "Debt"));
        COMMON_SUBCATEGORIES.put("Equity", Arrays.asList("Capital", "RetainedEarnings", "Drawings"));
        COMMON_SUBCATEGORIES.put("Income", Arrays.asList("Salary", "Business", "Investment", "Rental", "Freelance"));
        COMMON_SUBCATEGORIES.put("Expenses", Arrays.asList("Food", "Transport", "Utilities", "Entertainment", "Rent"));
    }
    
    /**
     * Attempts to correct a date string.
     * 
     * @param dateStr the date string to correct
     * @return corrected date string, or null if no correction can be suggested
     */
    public static String correctDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        
        String trimmed = dateStr.trim();
        
        // First, try to parse with the standard format
        try {
            LocalDate.parse(trimmed, Config.DATE_FORMATTER);
            return trimmed; // Already correct
        } catch (DateTimeParseException e) {
            // Try pattern-based corrections
            for (Map.Entry<Pattern, String> entry : DATE_CORRECTIONS.entrySet()) {
                if (entry.getKey().matcher(trimmed).matches()) {
                    String corrected = trimmed.replaceAll(entry.getKey().pattern(), entry.getValue());
                    
                    // Verify the corrected date can be parsed
                    try {
                        LocalDate.parse(corrected, Config.DATE_FORMATTER);
                        return corrected;
                    } catch (DateTimeParseException e2) {
                        // Continue to next pattern
                    }
                }
            }
            
            // Try to extract numbers and reconstruct
            String numbersOnly = trimmed.replaceAll("[^0-9]", "");
            if (numbersOnly.length() == 6) {
                // Assume DDMMYY
                String day = numbersOnly.substring(0, 2);
                String month = numbersOnly.substring(2, 4);
                String year = "20" + numbersOnly.substring(4, 6);
                String candidate = day + "/" + month + "/" + year;
                
                try {
                    LocalDate.parse(candidate, Config.DATE_FORMATTER);
                    return candidate;
                } catch (DateTimeParseException e2) {
                    // Not valid
                }
            } else if (numbersOnly.length() == 8) {
                // Assume DDMMYYYY or MMDDYYYY
                String day1 = numbersOnly.substring(0, 2);
                String month1 = numbersOnly.substring(2, 4);
                String year1 = numbersOnly.substring(4, 8);
                String candidate1 = day1 + "/" + month1 + "/" + year1;
                
                String day2 = numbersOnly.substring(2, 4);
                String month2 = numbersOnly.substring(0, 2);
                String year2 = numbersOnly.substring(4, 8);
                String candidate2 = day2 + "/" + month2 + "/" + year2;
                
                try {
                    LocalDate.parse(candidate1, Config.DATE_FORMATTER);
                    return candidate1;
                } catch (DateTimeParseException e2) {
                    try {
                        LocalDate.parse(candidate2, Config.DATE_FORMATTER);
                        return candidate2;
                    } catch (DateTimeParseException e3) {
                        // Neither works
                    }
                }
            }
        }
        
        return null; // No correction found
    }
    
    /**
     * Attempts to correct a currency code.
     * 
     * @param currencyStr the currency string to correct
     * @return corrected currency code, or null if no correction can be suggested
     */
    public static String correctCurrency(String currencyStr) {
        if (currencyStr == null || currencyStr.trim().isEmpty()) {
            return null;
        }
        
        String trimmed = currencyStr.trim().toUpperCase();
        
        // Check if already valid
        if (Config.isValidCurrency(trimmed)) {
            return trimmed;
        }
        
        // Try direct mapping
        if (CURRENCY_CORRECTIONS.containsKey(trimmed)) {
            return CURRENCY_CORRECTIONS.get(trimmed);
        }
        
        // Try fuzzy matching (case-insensitive contains)
        for (String validCurrency : Config.SUPPORTED_CURRENCIES) {
            if (validCurrency.equalsIgnoreCase(trimmed) ||
                trimmed.contains(validCurrency) ||
                validCurrency.contains(trimmed)) {
                return validCurrency;
            }
        }
        
        // Try Levenshtein distance (simple version)
        for (String validCurrency : Config.SUPPORTED_CURRENCIES) {
            if (calculateSimilarity(trimmed, validCurrency) > 0.7) {
                return validCurrency;
            }
        }
        
        return null; // No correction found
    }
    
    /**
     * Attempts to correct an account name.
     * 
     * @param accountStr the account string to correct
     * @return corrected account name, or null if no correction can be suggested
     */
    public static String correctAccountName(String accountStr) {
        if (accountStr == null || accountStr.trim().isEmpty()) {
            return null;
        }
        
        String trimmed = accountStr.trim();
        
        // Check if it already contains separator
        if (trimmed.contains(Config.ACCOUNT_SEPARATOR)) {
            String[] parts = trimmed.split(Config.ACCOUNT_SEPARATOR, 2);
            String root = parts[0].trim();
            String subcategory = parts.length > 1 ? parts[1].trim() : "";
            
            String correctedRoot = correctAccountRoot(root);
            if (correctedRoot != null && !correctedRoot.equals(root)) {
                return correctedRoot + (subcategory.isEmpty() ? "" : Config.ACCOUNT_SEPARATOR + subcategory);
            }
            
            // Check subcategory suggestions
            if (!subcategory.isEmpty() && COMMON_SUBCATEGORIES.containsKey(correctedRoot != null ? correctedRoot : root)) {
                List<String> commonSubs = COMMON_SUBCATEGORIES.get(correctedRoot != null ? correctedRoot : root);
                for (String commonSub : commonSubs) {
                    if (calculateSimilarity(subcategory, commonSub) > 0.7) {
                        return (correctedRoot != null ? correctedRoot : root) + 
                               Config.ACCOUNT_SEPARATOR + commonSub;
                    }
                }
            }
        } else {
            // No separator - might be just a root or a common phrase
            String correctedRoot = correctAccountRoot(trimmed);
            if (correctedRoot != null) {
                return correctedRoot;
            }
            
            // Check if it's a common subcategory phrase
            for (String root : Config.VALID_ACCOUNT_ROOTS) {
                if (COMMON_SUBCATEGORIES.containsKey(root)) {
                    for (String subcategory : COMMON_SUBCATEGORIES.get(root)) {
                        if (calculateSimilarity(trimmed, subcategory) > 0.7) {
                            return root + Config.ACCOUNT_SEPARATOR + subcategory;
                        }
                    }
                }
            }
        }
        
        return null; // No correction found
    }
    
    /**
     * Attempts to correct an account root.
     * 
     * @param rootStr the account root string to correct
     * @return corrected account root, or null if no correction can be suggested
     */
    private static String correctAccountRoot(String rootStr) {
        if (rootStr == null || rootStr.trim().isEmpty()) {
            return null;
        }
        
        String trimmed = rootStr.trim();
        
        // Check if already valid
        if (Config.isValidAccountRoot(trimmed)) {
            return trimmed;
        }
        
        // Try direct mapping
        String lowerTrimmed = trimmed.toLowerCase();
        if (ACCOUNT_ROOT_CORRECTIONS.containsKey(lowerTrimmed)) {
            return ACCOUNT_ROOT_CORRECTIONS.get(lowerTrimmed);
        }
        
        // Try fuzzy matching
        for (String validRoot : Config.VALID_ACCOUNT_ROOTS) {
            if (validRoot.equalsIgnoreCase(trimmed) ||
                calculateSimilarity(trimmed.toLowerCase(), validRoot.toLowerCase()) > 0.7) {
                return validRoot;
            }
        }
        
        return null; // No correction found
    }
    
    /**
     * Calculates similarity between two strings (0.0 to 1.0).
     * Simple implementation using Levenshtein distance.
     */
    private static double calculateSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return 0.0;
        }
        
        if (s1.equals(s2)) {
            return 1.0;
        }
        
        // Simple case-insensitive equality check
        if (s1.equalsIgnoreCase(s2)) {
            return 0.9;
        }
        
        // Check if one contains the other
        if (s1.toLowerCase().contains(s2.toLowerCase()) ||
            s2.toLowerCase().contains(s1.toLowerCase())) {
            return 0.8;
        }
        
        // Simple prefix/suffix matching
        if (s1.toLowerCase().startsWith(s2.toLowerCase()) ||
            s2.toLowerCase().startsWith(s1.toLowerCase()) ||
            s1.toLowerCase().endsWith(s2.toLowerCase()) ||
            s2.toLowerCase().endsWith(s1.toLowerCase())) {
            return 0.7;
        }
        
        // For a more sophisticated implementation, we could use Levenshtein distance
        // But for now, return a lower similarity
        return 0.3;
    }
    
    /**
     * Suggests possible corrections for an invalid input.
     * 
     * @param field the field name (e.g., "date", "currency", "account")
     * @param value the invalid value
     * @return list of suggested corrections, empty if none
     */
    public static List<String> suggestCorrections(String field, String value) {
        if (value == null || value.trim().isEmpty()) {
            return List.of();
        }
        
        String trimmed = value.trim();
        List<String> suggestions = new java.util.ArrayList<>();
        
        switch (field.toLowerCase()) {
            case "date":
                String correctedDate = correctDate(trimmed);
                if (correctedDate != null) {
                    suggestions.add(correctedDate);
                }
                break;
                
            case "currency":
                String correctedCurrency = correctCurrency(trimmed);
                if (correctedCurrency != null) {
                    suggestions.add(correctedCurrency);
                }
                // Also suggest all valid currencies
                suggestions.addAll(Config.SUPPORTED_CURRENCIES);
                break;
                
            case "account":
                String correctedAccount = correctAccountName(trimmed);
                if (correctedAccount != null) {
                    suggestions.add(correctedAccount);
                }
                // Suggest common account patterns
                for (String root : Config.VALID_ACCOUNT_ROOTS) {
                    if (COMMON_SUBCATEGORIES.containsKey(root)) {
                        for (String subcategory : COMMON_SUBCATEGORIES.get(root)) {
                            suggestions.add(root + Config.ACCOUNT_SEPARATOR + subcategory);
                        }
                    }
                }
                break;
                
            case "preset":
                // Suggest valid preset types
                suggestions.addAll(Config.VALID_PRESET_TYPES);
                break;
                
            default:
                // No specific suggestions for other fields
                break;
        }
        
        // Remove duplicates and limit to 5 suggestions
        return suggestions.stream()
            .distinct()
            .limit(5)
            .toList();
    }
    
    /**
     * Provides a friendly error message with suggestions.
     * 
     * @param field the field name
     * @param value the invalid value
     * @param errorMessage the original error message
     * @return enhanced error message with suggestions
     */
    public static String getEnhancedErrorMessage(String field, String value, String errorMessage) {
        List<String> suggestions = suggestCorrections(field, value);
        
        if (suggestions.isEmpty()) {
            return errorMessage;
        }
        
        StringBuilder enhanced = new StringBuilder(errorMessage);
        enhanced.append("\n\nDid you mean one of these?\n");
        
        for (int i = 0; i < suggestions.size(); i++) {
            enhanced.append("  ").append(i + 1).append(". ").append(suggestions.get(i)).append("\n");
        }
        
        return enhanced.toString();
    }
    
    // Private constructor to prevent instantiation
    private AutoCorrector() {
        throw new AssertionError("AutoCorrector class should not be instantiated.");
    }
}