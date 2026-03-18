package seedu.duke;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Represents a financial transaction containing an auto-incremented ID, date,
 * description, amount, type, and validated currency.
 */
public class Transaction {
    private static int nextId = 1;

    private int id;
    private LocalDate date;
    private String description;
    private double amount;
    private TransactionType type;
    private String currency;

    public Transaction(String dateStr, String description, Double amount, String typeStr, String currencyStr) {
        if (dateStr == null || description == null || amount == null || typeStr == null || currencyStr == null) {
            throw new IllegalArgumentException("Missing required transaction details.");
        }
        if (description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty.");
        }

        this.id = nextId++;
        this.date = parseDate(dateStr);
        this.description = description;
        this.amount = amount;
        this.type = new TransactionType(typeStr);
        this.currency = CurrencyValidator.validateAndGet(currencyStr);
    }

    /**
     * Constructor used when loading transactions from storage.
     */
    private Transaction(int id, String dateStr, String description, Double amount, String typeStr, String currencyStr) {
        if (dateStr == null || description == null || amount == null || typeStr == null || currencyStr == null) {
            throw new IllegalArgumentException("Missing required transaction details.");
        }
        if (description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty.");
        }

        this.id = id;
        this.date = parseDate(dateStr);
        this.description = description;
        this.amount = amount;
        this.type = new TransactionType(typeStr);
        this.currency = CurrencyValidator.validateAndGet(currencyStr);
    }

    public static Transaction fromStorage(int id, String dateStr, String description,
                                          Double amount, String typeStr, String currencyStr) {
        return new Transaction(id, dateStr, description, amount, typeStr, currencyStr);
    }

    public static void updateNextId(int nextIdValue) {
        if (nextIdValue > nextId) {
            nextId = nextIdValue;
        }
    }

    private LocalDate parseDate(String dateStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return LocalDate.parse(dateStr, formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("DATE must be in DD/MM/YYYY format.");
        }
    }

    public int getId() {
        return id;
    }

    public String getDateString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return date.format(formatter);
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public String getType() {
        return type.value;
    }

    public String getCurrency() {
        return currency;
    }

    public void update(String dateStr, String description, Double amount, String typeStr, String currencyStr) {
        if (dateStr != null) {
            this.date = parseDate(dateStr);
        }
        if (description != null) {
            if (description.trim().isEmpty()) {
                throw new IllegalArgumentException("Description cannot be empty.");
            }
            this.description = description;
        }
        if (amount != null) {
            this.amount = amount;
        }
        if (typeStr != null) {
            this.type = new TransactionType(typeStr);
        }
        if (currencyStr != null) {
            this.currency = CurrencyValidator.validateAndGet(currencyStr);
        }
    }

    public LocalDate getDate() {
        return date;
    }

    @Override
    public String toString() {
        return String.format("ID: %d | Date: %s | Desc: %s | Amount: %.2f | Type: %s | Currency: %s",
                id, getDateString(), description, amount, type.value, currency);
    }
}