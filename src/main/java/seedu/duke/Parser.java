package seedu.duke;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reads command line inputs, checks for format validation, handles errors, and
 * executes transaction commands.
 */
public class Parser {
    private static final Logger logger = Logger.getLogger("Parser");

    private final TransactionsList list;
    private final CurrencyConverter converter;
    private final ExchangeRateStorage exchangeRateStorage;
    private final LiveExchangeRateService liveExchangeRateService;

    public Parser(TransactionsList list, CurrencyConverter converter,
                  ExchangeRateStorage exchangeRateStorage,
                  LiveExchangeRateService liveExchangeRateService) {
        assert list != null : "Parser requires a valid TransactionsList instance.";
        assert converter != null : "Parser requires a valid CurrencyConverter instance.";
        assert exchangeRateStorage != null : "Parser requires a valid ExchangeRateStorage instance.";
        assert liveExchangeRateService != null : "Parser requires a valid LiveExchangeRateService instance.";

        this.list = list;
        this.converter = converter;
        this.exchangeRateStorage = exchangeRateStorage;
        this.liveExchangeRateService = liveExchangeRateService;
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("exit")) {
                break;
            }
            if (input.isEmpty()) {
                continue;
            }
            try {
                logger.log(Level.INFO, "Processing user input: " + input);
                processInput(input);
            } catch (Exception e) {
                logger.log(Level.WARNING, "processing error", e);
                System.out.println("Error: " + e.getMessage());
            }
        }
        scanner.close();
    }

    private void processInput(String input) {
        assert input != null && !input.isEmpty() : "Input string to process should not be empty.";
        String[] parts = input.split("\\s+", 2);
        String command = parts[0].toLowerCase();
        String arguments = parts.length > 1 ? parts[1].trim() : "";

        if (!command.equals("convert") && !command.equals("rates")) {
            if (arguments.toLowerCase().startsWith("transaction ")) {
                arguments = arguments.substring("transaction ".length()).trim();
            } else if (arguments.equalsIgnoreCase("transaction")) {
                arguments = "";
            }
        }

        switch (command){
        case "add":
            handleAdd(arguments);
            break;
        case "list":
            handleList(arguments);
            break;
        case "delete":
            handleDelete(arguments);
            break;
        case "clear":
            list.clearTransactions();
            break;
        case "edit":
            handleEdit(arguments);
            break;
        case "convert":
            handleConvert(arguments);
            break;
        case "rates":
            handleRates(arguments);
            break;
        case "help":
            handleHelp();
            break;
        default:
            throw new IllegalArgumentException(
                "Unknown command. Use add, list, edit, delete, clear, convert, rates, help, or exit.");
        }

    }

    private Map<String, String> parseArguments(String args) {
        Map<String, String> map = new HashMap<>();
        String[] tokens = (" " + args).split("(?=\\s-[a-zA-Z])");
        for (String token : tokens) {
            token = token.trim();
            if (token.startsWith("-")) {
                int spaceIdx = token.indexOf(' ');
                if (spaceIdx != -1) {
                    String key = token.substring(0, spaceIdx).trim();
                    String value = token.substring(spaceIdx + 1).trim();
                    map.put(key, value);
                } else {
                    map.put(token, "");
                }
            }
        }
        return map;
    }

    private void handleAdd(String args) {
        Map<String, String> map = parseArguments(args);
        String date = map.get("-d");
        String desc = map.get("-desc");
        String amountStr = map.get("-a");
        String type = map.get("-t");
        String currency = map.get("-c");

        if (amountStr == null) {
            throw new IllegalArgumentException("Missing amount.");
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Amount must be a valid number.");
        }

        Transaction t = new Transaction(date, desc, amount, type, currency);
        list.addTransaction(t);
        System.out.println("Transaction added successfully.");
    }

    private void handleList(String args) {
        if (!args.isEmpty()) {
            Map<String, String> map = parseArguments(args);
            String to = map.get("-to");
            if (to != null) {
                list.setDisplayCurrency(to);
                list.setAutoConvertDisplay(true);
            } else {
                list.setAutoConvertDisplay(false);
            }
        } else {
            list.setAutoConvertDisplay(false);
        }

        list.listTransactions();
    }

    private void handleDelete(String args) {
        if (args.isEmpty()) {
            throw new IllegalArgumentException("Missing transaction ID to delete.");
        }
        try {
            int id = Integer.parseInt(args.split("\\s+")[0]);
            list.deleteTransaction(id);
            System.out.println("Transaction deleted successfully.");
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Transaction ID must be an integer.");
        }
    }

    private void handleEdit(String args) {
        if (args.isEmpty()) {
            throw new IllegalArgumentException("Missing transaction ID to edit.");
        }

        String[] parts = args.split("\\s+", 2);
        int id;
        try {
            id = Integer.parseInt(parts[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Transaction ID must be an integer.");
        }

        String editArgs = parts.length > 1 ? parts[1] : "";
        if (editArgs.isEmpty()) {
            throw new IllegalArgumentException("No fields provided to edit.");
        }

        Map<String, String> map = parseArguments(editArgs);
        String date = map.get("-d");
        String desc = map.get("-desc");
        String amountStr = map.get("-a");
        String type = map.get("-t");
        String currency = map.get("-c");

        Double amount = null;
        if (amountStr != null) {
            try {
                amount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Amount must be a valid number.");
            }
        }

        list.editTransaction(id, date, desc, amount, type, currency);
        System.out.println("Transaction edited successfully.");
    }

    private void handleConvert(String args) {
        if (args.toLowerCase().startsWith("transaction ")) {
            handleConvertTransaction(args.substring("transaction ".length()).trim());
            return;
        }

        Map<String, String> map = parseArguments(args);

        String amountStr = map.get("-a");
        String from = map.get("-from");
        String to = map.get("-to");

        if (amountStr == null || from == null || to == null) {
            throw new IllegalArgumentException("Missing arguments for convert.");
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Amount must be a valid number.");
        }

        from = CurrencyValidator.validateAndGet(from);
        to = CurrencyValidator.validateAndGet(to);

        double result = converter.convert(amount, from, to);

        System.out.printf("%.2f %s = %.2f %s%n", amount, from, result, to);
    }

    private void handleConvertTransaction(String args) {
        if (args.isEmpty()) {
            throw new IllegalArgumentException("Missing transaction ID to convert.");
        }

        String[] parts = args.split("\\s+", 2);
        int id;
        try {
            id = Integer.parseInt(parts[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Transaction ID must be an integer.");
        }

        String convertArgs = parts.length > 1 ? parts[1] : "";
        Map<String, String> map = parseArguments(convertArgs);
        String to = map.get("-to");

        if (to == null) {
            throw new IllegalArgumentException("Missing target currency.");
        }

        to = CurrencyValidator.validateAndGet(to);

        Transaction transaction = list.getTransactionById(id);
        double result = converter.convert(transaction.getAmount(), transaction.getCurrency(), to);

        System.out.printf(
                "Transaction %d: %.2f %s = %.2f %s%n",
                id,
                transaction.getAmount(),
                transaction.getCurrency(),
                result,
                to
        );
    }

    private void handleRates(String args) {
        if (!args.equalsIgnoreCase("refresh")) {
            throw new IllegalArgumentException("Unknown rates command. Use: rates refresh");
        }

        List<String> supportedCurrencies = Arrays.asList("EUR", "SGD", "USD");
        ExchangeRateData liveData = liveExchangeRateService.fetchLatest("EUR", supportedCurrencies);
        exchangeRateStorage.save(liveData);
        converter.updateRates(liveData);

        System.out.println("Exchange rates refreshed successfully for " + liveData.getDate() + ".");
    }

    private void handleHelp() {
        System.out.println("=== Ledger67 Help ===");
        System.out.println("Available commands:");
        System.out.println();
        System.out.println("1. add - Add a new transaction");
        System.out.println("   Format: add -d DATE -desc DESCRIPTION -a AMOUNT -t TYPE -c CURRENCY");
        System.out.println("   Example: add -d 18/03/2026 -desc Office supplies -a 45.50 -t debit -c SGD");
        System.out.println();
        System.out.println("2. list - Display all transactions");
        System.out.println("   Format: list");
        System.out.println("   Optional: list transaction -to CURRENCY");
        System.out.println("   Example: list transaction -to USD");
        System.out.println();
        System.out.println("3. edit - Modify an existing transaction");
        System.out.println("   Format: edit ID [-d DATE] [-desc DESC] [-a AMOUNT] [-t TYPE] [-c CURRENCY]");
        System.out.println("   Example: edit 1 -desc Updated description -a 50.00");
        System.out.println();
        System.out.println("4. delete - Remove a transaction");
        System.out.println("   Format: delete ID");
        System.out.println("   Example: delete 3");
        System.out.println();
        System.out.println("5. clear - Remove all transactions");
        System.out.println("   Format: clear");
        System.out.println();
        System.out.println("6. convert - Convert currencies");
        System.out.println("   Format: convert -a AMOUNT -from SOURCE_CURRENCY -to TARGET_CURRENCY");
        System.out.println("   Example: convert -a 100 -from USD -to SGD");
        System.out.println();
        System.out.println("7. convert transaction - Convert an existing transaction");
        System.out.println("   Format: convert transaction ID -to TARGET_CURRENCY");
        System.out.println("   Example: convert transaction 3 -to SGD");
        System.out.println();
        System.out.println("8. rates - Refresh live exchange rates");
        System.out.println("   Format: rates refresh");
        System.out.println();
        System.out.println("9. help - Show this help message");
        System.out.println("   Format: help");
        System.out.println();
        System.out.println("10. exit - Exit the application");
        System.out.println("   Format: exit");
        System.out.println();
        System.out.println("=== Additional Information ===");
        System.out.println("- DATE format: DD/MM/YYYY (e.g., 18/03/2026)");
        System.out.println("- TYPE must be either 'debit' or 'credit'");
        System.out.println("- CURRENCY must be one of: SGD, USD, EUR");
        System.out.println("- ID is shown when using the 'list' command");
        System.out.println("- Transactions are stored locally on your machine");
        System.out.println("- Exchange rates are loaded from exchange-rates.json");
        System.out.println();
        System.out.println("For detailed documentation, please refer to the User Guide.");
    }
}
