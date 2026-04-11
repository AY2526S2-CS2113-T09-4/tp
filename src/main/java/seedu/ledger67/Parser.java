package seedu.ledger67;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
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

    // Pending confirmation state
// Pending confirmation state
    private Integer pendingTransactionId = null;
    private String pendingTargetCurrency = null;
    private boolean pendingFromListView = false;
    private List<Integer> pendingDisplayedTransactionIds = new ArrayList<>();

    private boolean isUiAssistOn = false;
    private Scanner scanner;
    
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
        this.scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Enter Command: ");
            
            if (!scanner.hasNextLine()) {
                break;
            }
            
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("exit")) {
                break;
            }
            if (input.isEmpty()) {
                continue;
            }
            
            if (input.equalsIgnoreCase("uiassist -on")) {
                isUiAssistOn = true;
                System.out.println("UI Assist is now ON. I will guide you through commands.");
                continue;
            } else if (input.equalsIgnoreCase("uiassist -off")) {
                isUiAssistOn = false;
                System.out.println("UI Assist is now OFF. Standard command mode active.");
                continue;
            }

            try {
                logger.log(Level.INFO, "Processing user input: " + input);

                if (tryHandlePendingConfirmation(input)) {
                    continue;
                }

                if (hasPendingConfirmation()) {
                    clearPendingConfirmation();
                }

                processInput(input);
            } catch (Exception e) {
                logger.log(Level.WARNING, "processing error", e);
                System.out.println("Error: " + e.getMessage());
            }
        }
        scanner.close();
    }

    private boolean hasPendingConfirmation() {
        return pendingTargetCurrency != null;
    }

    private void clearPendingConfirmation() {
        pendingTransactionId = null;
        pendingTargetCurrency = null;
        pendingFromListView = false;
        pendingDisplayedTransactionIds.clear();
    }

    /**
     * Handles:
     * - confirm
     * - confirm all
     * - confirm ID
     */
    private boolean tryHandlePendingConfirmation(String input) {
        if (!hasPendingConfirmation()) {
            return false;
        }

        String trimmedInput = input.trim();
        if (!trimmedInput.toLowerCase().startsWith("confirm")) {
            return false;
        }

        String[] parts = trimmedInput.split("\\s+");

        if (!pendingFromListView) {
            if (parts.length != 1 || !parts[0].equalsIgnoreCase("confirm")) {
                throw new IllegalArgumentException(
                        "After 'convert transaction', use only 'confirm' to store the viewed transaction.");
            }

            confirmAndStoreConvertedTransaction(pendingTransactionId, pendingTargetCurrency);
            clearPendingConfirmation();
            return true;
        }

        if (parts.length == 2 && parts[1].equalsIgnoreCase("all")) {
            confirmAndStoreAllDisplayedTransactions(pendingTargetCurrency);
            clearPendingConfirmation();
            return true;
        }

        if (parts.length == 2) {
            int id;
            try {
                id = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Transaction ID for confirm must be an integer.");
            }

            if (!pendingDisplayedTransactionIds.contains(id)) {
                throw new IllegalArgumentException(
                        "Transaction " + id + " was not in the previous converted list view.");
            }

            confirmAndStoreConvertedTransaction(id, pendingTargetCurrency);
            clearPendingConfirmation();
            return true;
        }

        throw new IllegalArgumentException(
                "After 'list transaction -to ...', use 'confirm all' or 'confirm ID'.");
    }

    private void confirmAndStoreConvertedTransaction(int id, String targetCurrency) {
        Transaction transaction = list.getTransactionById(id);
        List<Posting> originalPostings = transaction.getPostings();

        List<Posting> convertedPostings = new ArrayList<>();

        for (Posting posting : originalPostings) {
            double convertedAmount = converter.convert(
                    posting.getAmount(),
                    transaction.getCurrency(),
                    targetCurrency);
            convertedPostings.add(new Posting(posting.getAccountName(), convertedAmount));
        }

        list.editTransaction(id, null, null, convertedPostings, targetCurrency);
        System.out.printf(
                "Transaction %d confirmed and stored in %s.%n",
                id,
                targetCurrency);
    }

    private void confirmAndStoreAllDisplayedTransactions(String targetCurrency) {
        if (pendingDisplayedTransactionIds.isEmpty()) {
            throw new IllegalArgumentException("No pending converted list results to confirm.");
        }

        int updatedCount = 0;
        for (int id : pendingDisplayedTransactionIds) {
            confirmAndStoreConvertedTransaction(id, targetCurrency);
            updatedCount++;
        }

        System.out.printf(
                "All %d displayed transaction(s) have been confirmed and stored in %s.%n",
                updatedCount,
                targetCurrency);
    }

    private void processInput(String input) {
        assert input != null && !input.isEmpty() : "Input string to process should not be empty.";
        String[] parts = input.split("\\s+", 2);
        String command = parts[0].toLowerCase();
        String arguments = parts.length > 1 ? parts[1].trim() : "";

        if (isUiAssistOn && arguments.isEmpty()) {
            String interactiveArgs = UiAssistFactory.getInteractiveArguments(command, this.scanner);
            if (!interactiveArgs.isEmpty()) {
                arguments = interactiveArgs;
                // Optional: Show the user what was generated so they learn the CLI formatting
                System.out.println("[Generated Command: " + command + " " + arguments + "]");
            }
        }

        if (!command.equals("convert") && !command.equals("rates")) {
            if (arguments.toLowerCase().startsWith("transaction ")) {
                arguments = arguments.substring("transaction ".length()).trim();
            } else if (arguments.equalsIgnoreCase("transaction")) {
                arguments = "";
            }
        }

        switch (command) {
        case "convert":
            handleConvert(arguments);
            break;
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
        case "rates":
            handleRates(arguments);
            break;
        case "help":
            handleHelp();
            break;
        case "balance":
            handleBalance(arguments);
            break;
        default:
            throw new IllegalArgumentException(
                    "Unknown command. Use add, list, edit, delete, clear, convert, rates, help, or exit.");
        }
    }

    public static Map<String, List<String>> parseArguments(String args) {
        Map<String, List<String>> map = new HashMap<>();
        // Your regex split is good; it looks for " -letter"
        String[] tokens = (" " + args).split("(?=\\s-[a-zA-Z])");

        for (String token : tokens) {
            token = token.trim();
            if (token.isEmpty()) {
                continue;
            }

            if (token.startsWith("-")) {
                int spaceIdx = token.indexOf(' ');
                String key;
                String value;

                if (spaceIdx != -1) {
                    key = token.substring(0, spaceIdx).trim();
                    value = token.substring(spaceIdx + 1).trim();
                } else {
                    key = token;
                    value = "";
                }

                // computeIfAbsent creates a new list if the key doesn't exist yet
                map.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
            }
        }
        return map;
    }

    public static String getFirstElementFromMap(Map<String, List<String>> map, String s) {
        if (!map.containsKey(s)) {
            return null;
        }
        String value = map.get(s).get(0);
        // Remove leading and trailing double or single quotes
        if (value != null && value.length() >= 2) {
            if ((value.startsWith("\"") && value.endsWith("\"")) ||
                    (value.startsWith("'") && value.endsWith("'"))) {
                value = value.substring(1, value.length() - 1);
            }
        }
        return value;
    }

    public static List<Posting> convertStringList2PostingList(List<String> postingStrings) {
        List<Posting> converted = new ArrayList<>();
        for (String pStr : postingStrings) {
            String cleanedStr = pStr.replace("\"", "").replace("'", "").trim();
            String[] parts = cleanedStr.split("\\s+");

            if (parts.length < 2) {
                throw new IllegalArgumentException("Invalid posting format. Use: -p \"Account Amount\"");
            }
            try {
                // The amount is always the last part
                int lastIndex = parts.length - 1;
                double amount = Double.parseDouble(parts[lastIndex].trim());

                // Everything before the amount is the account name
                String accountName = cleanedStr.substring(0, cleanedStr.lastIndexOf(parts[lastIndex])).trim();

                converted.add(new Posting(accountName, amount));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Amount must be a valid number.");
            }
        }
        return converted;
    }



    private void handleAdd(String args) {
        Map<String, List<String>> map = parseArguments(args);
        String date = getFirstElementFromMap(map, "-date");
        String desc = getFirstElementFromMap(map, "-desc");
        String currency = getFirstElementFromMap(map, "-c");

        // New: Check for preset
        String presetData = getFirstElementFromMap(map, "-preset");
        List<Posting> postings;

        if (presetData != null) {
            // Use logic from PresetHandler
            postings = PresetHandler.generatePostings(presetData);
            // If user didn't provide a description, use the preset name as a default
            if (desc == null) {
                desc = presetData.split("\\s+")[0];
            }
        } else {
            // Fallback to standard manual posting logic
            List<String> postingStrings = map.get("-p");
            if (postingStrings == null) {
                throw new IllegalArgumentException("Error: Either -preset or at least one posting (-p) is required.");
            }
            postings = convertStringList2PostingList(postingStrings);
        }

        

        Transaction t = new Transaction(date, desc, postings, currency);
        list.addTransaction(t);
        System.out.println("Transaction added successfully via " + (presetData != null ? "preset." : "manual input."));
    }

    private void handleList(String args) {
        clearPendingConfirmation();

        Map<String, List<String>> map = parseArguments(args);
        String to = getFirstElementFromMap(map, "-to");
        String acc = getFirstElementFromMap(map, "-acc");
        String regex = getFirstElementFromMap(map, "-match");
        String startStr = getFirstElementFromMap(map, "-begin");
        String endStr = getFirstElementFromMap(map, "-end");

        if (to != null) {
            to = CurrencyValidator.validateAndGet(to);
            list.setDisplayCurrency(to);
            list.setAutoConvertDisplay(true);
        } else {
            list.setAutoConvertDisplay(false);
        }

        List<Transaction> results = new ArrayList<>(list.getTransactions());

        try {
            LocalDate start = (startStr != null) ? LocalDate.parse(startStr, Config.DATE_FORMATTER) : null;
            LocalDate end = (endStr != null) ? LocalDate.parse(endStr, Config.DATE_FORMATTER) : null;

            results = TransactionsList.filterTransactionsByDate(results, start, end);
            results = TransactionsList.filterTransactionsByRegex(results, regex);
            results = TransactionsList.filterTransactionsByAccount(results, acc);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(Config.ERROR_INVALID_DATE_FORMAT);
        }

        list.renderTransactions(results);

        if (to != null && !results.isEmpty()) {
            pendingTransactionId = null;
            pendingTargetCurrency = to;
            pendingFromListView = true;
            pendingDisplayedTransactionIds = results.stream()
                    .map(Transaction::getId)
                    .toList();

            System.out.println();
            System.out.println("The displayed values are view-only by default.");
            System.out.println("Type 'confirm all' to store ALL transactions in " + to + ".");
            System.out.println("Type 'confirm ID' to store one displayed transaction in " + to + ".");
            System.out.println("Example: confirm 3");
            System.out.println("Enter any other command to ignore.");
        }
    }

    

    private void handleDelete(String args) {
        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty()) {
            throw new IllegalArgumentException("Missing transaction ID or filter flags to delete.");
        }

        // 1. Handle Single ID Deletion (e.g., "delete 5")
        if (trimmedArgs.matches("^\\d+$")) {
            int id = Integer.parseInt(trimmedArgs);
            list.deleteTransaction(id);
            System.out.println("Successfully deleted Transaction " + id);
            return;
        }

        // 2. Handle Filtered Deletion
        Map<String, List<String>> map = parseArguments(args);
        String acc = getFirstElementFromMap(map, "-acc");
        String startStr = getFirstElementFromMap(map, "-begin");
        String endStr = getFirstElementFromMap(map, "-end");
        String regex = getFirstElementFromMap(map, "-match");


        if (startStr == null && endStr == null && regex == null && acc == null) {
            throw new IllegalArgumentException("No valid filters provided. Use -begin, -end, -match, or -acc.");
        }

        List<Transaction> toDelete = new ArrayList<>(list.getTransactions());
        try {
            LocalDate start = (startStr != null) ? LocalDate.parse(startStr, Config.DATE_FORMATTER) : null;
            LocalDate end = (endStr != null) ? LocalDate.parse(endStr, Config.DATE_FORMATTER) : null;

            toDelete = TransactionsList.filterTransactionsByDate(toDelete, start, end);
            toDelete = TransactionsList.filterTransactionsByRegex(toDelete, regex);
            toDelete = TransactionsList.filterTransactionsByAccount(toDelete, acc);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(Config.ERROR_INVALID_DATE_FORMAT);
        }

        

        // 3. Execution
        if (toDelete.isEmpty()) {
            System.out.println("No transactions found matching the specified criteria. Nothing deleted.");
            return;
        }

        int count = toDelete.size();
        for (Transaction t : toDelete) {
            list.deleteTransaction(t.getId());
        }

        System.out.println("Successfully deleted " + count + " transaction(s) matching criteria.");
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

        Map<String, List<String>> map = parseArguments(editArgs);
        String date = getFirstElementFromMap(map, "-date");
        String desc = getFirstElementFromMap(map, "-desc");
        List<String> postingStrings = map.get("-p");
        String currency = getFirstElementFromMap(map, "-c");

        List<Posting> postings;
        if (postingStrings == null) {
            postings = null;
        } else {
            postings = convertStringList2PostingList(postingStrings);
        }
        list.editTransaction(id, date, desc, postings, currency);
        System.out.println("Transaction edited successfully.");
    }

    private void handleConvert(String args) {
        if (args.toLowerCase().startsWith("transaction ")) {
            handleConvertTransaction(args.substring("transaction ".length()).trim());
            return;
        }

        Map<String, List<String>> map = parseArguments(args);

        String amountStr = getFirstElementFromMap(map, "-a");
        String from = getFirstElementFromMap(map, "-from");
        String to = getFirstElementFromMap(map, "-to");

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

        clearPendingConfirmation();
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
        Map<String, List<String>> map = parseArguments(convertArgs);
        String to = getFirstElementFromMap(map, "-to");

        if (to == null) {
            throw new IllegalArgumentException("Missing target currency.");
        }

        to = CurrencyValidator.validateAndGet(to);

        Transaction transaction = list.getTransactionById(id);
        for (Posting posting : transaction.getPostings()) {
            double originalAmount = posting.getAmount();
            double convertedAmount = converter.convert(originalAmount, transaction.getCurrency(), to);

            System.out.printf("  %-30s : %10.2f %-3s = %10.2f %-3s%n",
                    posting.getAccountName(),
                    originalAmount, transaction.getCurrency(),
                    convertedAmount, to);
        }

        pendingTransactionId = id;
        pendingTargetCurrency = to;
        pendingFromListView = false;

        System.out.println("This converted value is view-only by default.");
        System.out.println("Type 'confirm' to store this transaction in " + to + ".");
        System.out.println("Enter any other command to ignore.");
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

    private void handleBalance(String args) {
        Map<String, List<String>> map = parseArguments(args);
        String to = getFirstElementFromMap(map, "-to");
        String acc = getFirstElementFromMap(map, "-acc");

        if (to != null) {
            to = CurrencyValidator.validateAndGet(to);
            list.setDisplayCurrency(to);
            list.setAutoConvertDisplay(true);
        } else {
            list.setAutoConvertDisplay(false);
        }

        if (acc != null) {
            list.printBalanceSheet(acc);
            return;
        }

        list.printBalanceSheet();
    }

    private void handleHelp() {
        System.out.println("=== Ledger67 Help ===");
        System.out.println("Available commands:");
        System.out.println();

        System.out.println("1. add - Add a new transaction");
        System.out.println("   Format (Manual): add -date DATE -desc DESCRIPTION -p POSTING1 -p POSTING2 -c CURRENCY");
        System.out.println("   ASSETS = EQUITY - LIABILITIES + (INCOME - EXPENSES)");
        System.out.println("   Each transaction must be balanced. This is checked by the system automatically.");
        System.out.println("   Postings support hierarchical account names using ':'");
        System.out.println("   Examples: Assets:Cash, Assets:Bank:DBS, Expenses:Food, Income:Salary");
        System.out.println("   Example: add -date 18/03/2026 -desc Office supplies -p " +
                "\"Assets:Cash -45.50\" -p \"Expenses:OfficeSupplies 45.50\" -c SGD");
        System.out.println("   Format (Preset): add -date DATE -preset TYPE AMOUNT -c CURRENCY");
        System.out.println("   Presets: DAILYEXPENSE, INCOME, BUYINGSTOCKS");
        System.out.println("   Example: add -date 18/03/2026 -preset DAILYEXPENSE 50.00 -c SGD");
        System.out.println("   Example: add -date 18/03/2026 -desc Office supplies -p \"Assets:Cash -45.50\" " +
                "-p \"Expenses:OfficeSupplies 45.50\" -c SGD");
        System.out.println();

        System.out.println("2. list - Display and filter transactions");
        System.out.println("   Format: list [-acc ACCOUNT] [-begin DATE] [-end DATE] [-match REGEX] [-to CURRENCY]");
        System.out.println("   - All flags are optional and can be combined (layered).");
        System.out.println("   - Dates must be in DD/MM/YYYY format.");
        System.out.println("   - Using -acc shows only postings for that account category.");
        System.out.println("   Example (Layered): list -acc Expenses -begin 01/01/2026 -end 31/01/2026");
        System.out.println("   Example (Regex):   list -match \"(Lunch|Dinner)\"");
        System.out.println();

        System.out.println("3. list -to CURRENCY - Display transactions with converted view values");
        System.out.println("   Format: list -to CURRENCY");
        System.out.println("   Example: list -to USD");
        System.out.println("   By default, this is a view-only feature.");
        System.out.println("   To store all displayed conversions, use: confirm all");
        System.out.println("   To store one displayed conversion, use: confirm ID");
        System.out.println("   Example: confirm 3");
        System.out.println();

        System.out.println("4. edit - Modify an existing transaction");
        System.out.println("   Format: edit ID [-date DATE] [-desc DESC] [-p POSTING] [-c CURRENCY]");
        System.out.println("   Example: edit 1 -desc Updated description -p " +
                "\"Expenses:Food 50\" -p \"Assets:Cash -50\"");
        System.out.println();

        System.out.println("5. delete - Remove a transaction");
        System.out.println("   Format (Single): delete ID");
        System.out.println("   Format (Bulk):   delete [-begin DATE] [-end DATE] [-match REGEX] [-acc ACCOUNT]");
        System.out.println("   Note: Bulk deletion requires at least one filter flag.");
        System.out.println("   Example (Single): delete 3");
        System.out.println("   Example (Range):  delete -begin 01/01/2026 -end 15/01/2026");
        System.out.println("   Example (Match):  delete -match Steak");
        System.out.println();

        System.out.println("6. clear - Remove all transactions");
        System.out.println("   Format: clear");
        System.out.println();

        System.out.println("7. convert - Convert currencies");
        System.out.println("   Format: convert -a AMOUNT -from SOURCE_CURRENCY -to TARGET_CURRENCY");
        System.out.println("   Example: convert -a 100 -from USD -to SGD");
        System.out.println("   Note: This is a view-mode feature only. It does NOT create or store a transaction.");
        System.out.println();

        System.out.println("8. convert transaction - Convert an existing transaction");
        System.out.println("   Format: convert transaction ID -to TARGET_CURRENCY");
        System.out.println("   Example: convert transaction 3 -to SGD");
        System.out.println("   By default, this is a view-mode feature only & won't modify the stored transaction.");
        System.out.println("   To store the converted transaction, type: confirm");
        System.out.println();

        System.out.println("9. confirm - Store viewed converted transaction(s)");
        System.out.println("   After convert transaction ...");
        System.out.println("   Format: confirm");
        System.out.println();
        System.out.println("   After list -to ...");
        System.out.println("   Format A: confirm all");
        System.out.println("   Format B: confirm ID");
        System.out.println("   Example: confirm all");
        System.out.println("   Example: confirm 3");
        System.out.println();

        System.out.println("10. rates - Refresh live exchange rates");
        System.out.println("    Format: rates refresh");
        System.out.println();

        System.out.println("11. balance - Generate Balance Sheet");
        System.out.println("    Format: balance");
        System.out.println("    Optional A: balance -to CURRENCY");
        System.out.println("    Optional B: balance -acc ACCOUNT");
        System.out.println("    Optional C: balance -acc ACCOUNT -to CURRENCY");
        System.out.println("    Example: balance -to USD");
        System.out.println("    Example: balance -acc Assets:Bank");
        System.out.println();

        System.out.println("12. uiassist - Turn on/off UI Assistance");
        System.out.println("    Format: uiassist -on/off");
        System.out.println("    With UI Assistance on, the program will guide the user through using prompted inputs");
        System.out.println();

        System.out.println("13. help - Show this help message");
        System.out.println("    Format: help");
        System.out.println();

        System.out.println("14. exit - Exit the application");
        System.out.println("    Format: exit");
        System.out.println();

        System.out.println("=== Additional Information ===");
        System.out.println("- DATE format: DD/MM/YYYY (e.g., 18/03/2026)");
        System.out.println("- CURRENCY must be one of: SGD, USD, EUR");
        System.out.println("- ID is shown when using the 'list' command");
        System.out.println("- Transactions are stored locally on your machine");
        System.out.println("- Exchange rates are loaded from exchange-rates.json");
        System.out.println("- Converted values are view-only unless confirmed");
        System.out.println("- Any command other than confirm will ignore the pending conversion");
        System.out.println("- Account names may be hierarchical using ':'");
        System.out.println("- Example account names: Assets:Cash, Assets:Bank:DBS, Expenses:Food");
        System.out.println("- Valid root account types are: Assets, Liabilities, Equity, Income, Expenses");
        System.out.println();
        System.out.println("For detailed documentation, please refer to the User Guide.");
    }
}
