package seedu.ledger67;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Validates transactions and provides error recovery suggestions.
 * Implements comprehensive validation with auto-correction suggestions.
 */
public class TransactionValidator {
    
    /**
     * Validation result containing errors and suggestions.
     */
    public static class ValidationResult {
        private final List<String> errors = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();
        private final Map<String, String> suggestions = new HashMap<>();
        private boolean isValid = true;
        
        public void addError(String error) {
            errors.add(error);
            isValid = false;
        }
        
        public void addWarning(String warning) {
            warnings.add(warning);
        }
        
        public void addSuggestion(String field, String suggestion) {
            suggestions.put(field, suggestion);
        }
        
        public List<String> getErrors() {
            return new ArrayList<>(errors);
        }
        
        public List<String> getWarnings() {
            return new ArrayList<>(warnings);
        }
        
        public Map<String, String> getSuggestions() {
            return new HashMap<>(suggestions);
        }
        
        public boolean isValid() {
            return isValid;
        }
        
        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }
        
        public boolean hasSuggestions() {
            return !suggestions.isEmpty();
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            
            if (!isValid) {
                sb.append("Validation Errors:\n");
                for (String error : errors) {
                    sb.append("  • ").append(error).append("\n");
                }
            }
            
            if (hasWarnings()) {
                sb.append("Warnings:\n");
                for (String warning : warnings) {
                    sb.append("  ⚠ ").append(warning).append("\n");
                }
            }
            
            if (hasSuggestions()) {
                sb.append("Suggestions:\n");
                for (Map.Entry<String, String> entry : suggestions.entrySet()) {
                    sb.append("  💡 ").append(entry.getKey())
                      .append(": ").append(entry.getValue()).append("\n");
                }
            }
            
            return sb.toString();
        }
    }
    
    /**
     * Validates a transaction with comprehensive checks.
     * 
     * @param transaction the transaction to validate
     * @return validation result with errors, warnings, and suggestions
     */
    public static ValidationResult validateTransaction(Transaction transaction) {
        ValidationResult result = new ValidationResult();
        
        // 1. Validate date
        validateDate(transaction.getDateString(), result);
        
        // 2. Validate description
        validateDescription(transaction.getDescription(), result);
        
        // 3. Validate currency
        validateCurrency(transaction.getCurrency(), result);
        
        // 4. Validate postings
        validatePostings(transaction.getPostings(), result);
        
        // 5. Check accounting equation (debits = credits)
        validateAccountingEquation(transaction.getPostings(), result);
        
        // 6. Check for common issues
        checkForCommonIssues(transaction, result);
        
        return result;
    }
    
    /**
     * Validates a date string.
     */
    private static void validateDate(String dateStr, ValidationResult result) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            result.addError("Date is required");
            return;
        }
        
        try {
            LocalDate date = LocalDate.parse(dateStr.trim(), Config.DATE_FORMATTER);
            
            // Check if date is in the future
            if (date.isAfter(LocalDate.now())) {
                result.addWarning("Date is in the future");
            }
            
            // Check if date is too far in the past (e.g., > 10 years)
            if (date.isBefore(LocalDate.now().minusYears(10))) {
                result.addWarning("Date is more than 10 years in the past");
            }
            
        } catch (DateTimeParseException e) {
            result.addError(Config.ERROR_INVALID_DATE_FORMAT);
            
            // Try to auto-correct common date formats
            String corrected = AutoCorrector.correctDate(dateStr);
            if (corrected != null) {
                result.addSuggestion("date", "Did you mean: " + corrected + "?");
            }
        }
    }
    
    /**
     * Validates a description.
     */
    private static void validateDescription(String description, ValidationResult result) {
        if (description == null || description.trim().isEmpty()) {
            result.addError("Description is required");
            return;
        }
        
        String trimmed = description.trim();
        
        // Check length
        if (trimmed.length() > 200) {
            result.addError("Description too long (max 200 characters)");
            result.addSuggestion("description", "Consider shortening the description");
        }
        
        // Check for suspicious patterns
        if (trimmed.matches(".*[<>{}].*")) {
            result.addWarning("Description contains special characters that might be problematic");
        }
        
        // Check for very short descriptions
        if (trimmed.length() < 3) {
            result.addWarning("Description is very short");
        }
    }
    
    /**
     * Validates a currency code.
     */
    private static void validateCurrency(String currency, ValidationResult result) {
        if (currency == null || currency.trim().isEmpty()) {
            result.addError("Currency is required");
            return;
        }
        
        String normalized = currency.trim().toUpperCase();
        
        if (!Config.isValidCurrency(normalized)) {
            result.addError(String.format(Config.ERROR_INVALID_CURRENCY, 
                Config.getSupportedCurrenciesString()));
            
            // Try to auto-correct common currency typos
            String corrected = AutoCorrector.correctCurrency(normalized);
            if (corrected != null) {
                result.addSuggestion("currency", "Did you mean: " + corrected + "?");
            }
        }
    }
    
    /**
     * Validates postings.
     */
    private static void validatePostings(List<Posting> postings, ValidationResult result) {
        if (postings == null || postings.isEmpty()) {
            result.addError("At least one posting is required");
            return;
        }
        
        if (postings.size() < 2) {
            result.addError("At least two postings are required for double-entry accounting");
        }
        
        Map<String, Integer> accountCount = new HashMap<>();
        
        for (int i = 0; i < postings.size(); i++) {
            Posting posting = postings.get(i);
            
            // Validate account name
            try {
                ValidationUtils.validateAccount(posting.getAccountName(), "account");
            } catch (IllegalArgumentException e) {
                result.addError("Posting " + (i + 1) + ": " + e.getMessage());
                
                // Try to auto-correct account name
                String corrected = AutoCorrector.correctAccountName(posting.getAccountName());
                if (corrected != null) {
                    result.addSuggestion("account_" + i, "Did you mean: " + corrected + "?");
                }
            }
            
            // Validate amount
            try {
                ValidationUtils.validateAmount(posting.getAmount(), "amount");
            } catch (IllegalArgumentException e) {
                result.addError("Posting " + (i + 1) + ": " + e.getMessage());
            }
            
            // Check for duplicate accounts in same transaction
            String accountName = posting.getAccountName();
            accountCount.put(accountName, accountCount.getOrDefault(accountName, 0) + 1);
        }
        
        // Check for duplicate accounts
        for (Map.Entry<String, Integer> entry : accountCount.entrySet()) {
            if (entry.getValue() > 1) {
                result.addWarning("Account '" + entry.getKey() + 
                    "' appears " + entry.getValue() + " times in the same transaction");
            }
        }
    }
    
    /**
     * Validates the accounting equation (debits = credits).
     */
    private static void validateAccountingEquation(List<Posting> postings, ValidationResult result) {
        if (postings == null || postings.size() < 2) {
            return;
        }
        
        double totalDebits = 0;
        double totalCredits = 0;
        
        for (Posting posting : postings) {
            double amount = posting.getAmount();
            if (amount > 0) {
                totalCredits += amount;
            } else {
                totalDebits += Math.abs(amount);
            }
        }
        
        // Allow small floating-point differences
        double difference = Math.abs(totalDebits - totalCredits);
        if (difference > 0.01) {
            result.addError(String.format(
                "Transaction is not balanced. Debits: %.2f, Credits: %.2f, Difference: %.2f",
                totalDebits, totalCredits, difference));
            
            // Suggest correction
            double adjustment = totalCredits - totalDebits;
            if (Math.abs(adjustment) > 0.01) {
                result.addSuggestion("balance", String.format(
                    "Add a posting with amount %.2f to balance the transaction",
                    -adjustment));
            }
        } else if (difference > 0.001) {
            result.addWarning(String.format(
                "Small rounding difference: %.4f", difference));
        }
    }
    
    /**
     * Checks for common issues in transactions.
     */
    private static void checkForCommonIssues(Transaction transaction, ValidationResult result) {
        List<Posting> postings = transaction.getPostings();
        
        // Check for mixed currencies (if we had multi-currency support)
        // Check for unusual amounts
        for (Posting posting : postings) {
            double amount = Math.abs(posting.getAmount());
            
            if (amount > 1000000) {
                result.addWarning("Very large amount: " + amount);
            }
            
            if (amount < 0.01) {
                result.addWarning("Very small amount: " + amount);
            }
            
            // Check for round numbers (might indicate estimation)
            if (Math.abs(amount - Math.round(amount)) < 0.001) {
                result.addWarning("Round number amount: " + amount);
            }
        }
        
        // Check for common description patterns
        String description = transaction.getDescription().toLowerCase();
        if (description.matches(".*test.*|.*sample.*|.*example.*")) {
            result.addWarning("Description suggests this might be a test transaction");
        }
    }
    
    /**
     * Validates and attempts to auto-correct a transaction.
     * Returns a corrected transaction if possible, or null if not.
     */
    public static Transaction validateAndCorrect(Transaction transaction) {
        ValidationResult result = validateTransaction(transaction);
        
        if (result.isValid() && !result.hasSuggestions()) {
            return transaction; // No correction needed
        }
        
        // For now, we just return the validation result
        // In a more advanced implementation, we could auto-correct
        return transaction;
    }
    
    /**
     * Creates a validation report for a transaction.
     */
    public static String createValidationReport(Transaction transaction) {
        ValidationResult result = validateTransaction(transaction);
        
        StringBuilder report = new StringBuilder();
        report.append("=== Transaction Validation Report ===\n");
        report.append("Transaction ID: ").append(transaction.getId()).append("\n");
        report.append("Date: ").append(transaction.getDate()).append("\n");
        report.append("Description: ").append(transaction.getDescription()).append("\n");
        report.append("Currency: ").append(transaction.getCurrency()).append("\n");
        report.append("\n");
        
        report.append(result.toString());
        
        if (result.isValid()) {
            report.append("Transaction is valid");
            if (result.hasWarnings()) {
                report.append(" with warnings");
            }
        } else {
            report.append("Transaction has validation errors");
        }
        
        return report.toString();
    }
    
    // Private constructor to prevent instantiation
    private TransactionValidator() {
        throw new AssertionError("TransactionValidator class should not be instantiated.");
    }
}