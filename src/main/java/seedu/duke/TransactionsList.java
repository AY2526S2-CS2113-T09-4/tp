package seedu.duke;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

/**
 * Stores and manages all transactions in memory, and saves them using Storage.
 */
public class TransactionsList {
    private static final Logger logger = Logger.getLogger("TransactionsList");
    private final List<Transaction> transactions;
    private final Storage storage;

    private CurrencyConverter converter;
    private String displayCurrency = "SGD";
    private boolean autoConvertDisplay = false;

    public TransactionsList(Storage storage) {
        this.storage = storage;
        this.transactions = new ArrayList<>(storage.load());
    }

    public void setCurrencyConverter(CurrencyConverter converter) {
        this.converter = converter;
    }

    public void setDisplayCurrency(String displayCurrency) {
        this.displayCurrency = CurrencyValidator.validateAndGet(displayCurrency);
    }

    public String getDisplayCurrency() {
        return displayCurrency;
    }

    public void setAutoConvertDisplay(boolean autoConvertDisplay) {
        this.autoConvertDisplay = autoConvertDisplay;
    }

    public void addTransaction(Transaction t) {
        logger.info("Adding transactions: " + t);
        if (t.isBalanced()) {
            transactions.add(t);
            save();
            refreshBalanceSheetCsv();
        } else {
            throw new IllegalArgumentException("Transaction is unbalanced!");
        }
    }

    public double getAccountBalance(String accountName) {
        double total = 0;
        for (Transaction t : transactions) {
            for (Posting p : t.getPostings()) {
                if (p.getAccount().isUnder(accountName)) {
                    total += p.getAmount();
                }
            }
        }
        return total;
    }

    public static List<Transaction> filterTransactionsByAccount(List<Transaction> transactions, String accountPrefix) {
        if (accountPrefix == null){
            return transactions;
        }
        return transactions.stream()
                .filter(t -> t.getPostings().stream()
                        .anyMatch(p -> p.getAccount().isUnder(accountPrefix)))
                .collect(Collectors.toList());
    }

    public static List<Transaction> filterTransactionsByDate(List<Transaction> transactions,
            LocalDate beginDate,
            LocalDate endDate) {
        return transactions.stream()
                .filter(t -> {
                    LocalDate date = t.getDate();

                    // If beginDate is null, it's always true.
                    // Otherwise, check if date is NOT before beginDate (i.e., after or equal).
                    boolean matchesBegin = (beginDate == null) || !date.isBefore(beginDate);

                    // If endDate is null, it's always true.
                    // Otherwise, check if date is NOT after endDate (i.e., before or equal).
                    boolean matchesEnd = (endDate == null) || !date.isAfter(endDate);

                    return matchesBegin && matchesEnd;
                })
                .collect(Collectors.toList());
    }

    public static List<Transaction> filterTransactionsByRegex(List<Transaction> transactions,
            String regexStr) {
        if(regexStr == null){
            return transactions;
        }
        try {
            Pattern pattern = Pattern.compile(regexStr, Pattern.CASE_INSENSITIVE);
            return transactions.stream()
                    .filter(t -> pattern.matcher(t.getDescription()).find())
                    .collect(Collectors.toList());
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("Invalid Regex pattern: " + regexStr);
        }
    }


    public void listTransactions() {
        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }

        transactions.stream()
                .sorted(Comparator.comparing(Transaction::getDate))
                .forEach(this::printTransactionWithDisplayAmount);
    }

    public void listTransactionsByAccount(String accountPrefix) {
        List<Transaction> filteredTransactions = new ArrayList<>();

        for (Transaction transaction : transactions) {
            boolean hasMatchingPosting = false;
            for (Posting posting : transaction.getPostings()) {
                if (posting.getAccount().isUnder(accountPrefix)) {
                    hasMatchingPosting = true;
                    break;
                }
            }

            if (hasMatchingPosting) {
                filteredTransactions.add(transaction);
            }
        }

        if (filteredTransactions.isEmpty()) {
            System.out.println("No transactions found under account: " + accountPrefix);
            return;
        }

        filteredTransactions.stream()
                .sorted(Comparator.comparing(Transaction::getDate))
                .forEach(transaction -> printFilteredTransaction(transaction, accountPrefix));
    }
    
    public void renderTransactions(List<Transaction> filteredList) {
        if (filteredList.isEmpty()) {
            System.out.println("No matching transactions found.");
            return;
        }

        filteredList.stream()
                .sorted(Comparator.comparing(Transaction::getDate))
                .forEach(this::printTransactionWithDisplayAmount);
        // Reuse your existing printTransactionWithDisplayAmount method
    }

    private void printFilteredTransaction(Transaction transaction, String accountPrefix) {
        if (converter == null || !autoConvertDisplay) {
            System.out.printf("ID: %d | Date: %s | Desc: %s | [%s]%n",
                    transaction.getId(),
                    transaction.getDateString(),
                    transaction.getDescription(),
                    transaction.getCurrency());

            for (Posting posting : transaction.getPostings()) {
                if (posting.getAccount().isUnder(accountPrefix)) {
                    System.out.printf("    %-30s : %10.2f%n",
                            posting.getAccountName(),
                            posting.getAmount());
                }
            }
            return;
        }

        System.out.printf("ID: %d | Date: %s | Desc: %s | [%s -> %s]%n",
                transaction.getId(),
                transaction.getDateString(),
                transaction.getDescription(),
                transaction.getCurrency(),
                displayCurrency);

        for (Posting posting : transaction.getPostings()) {
            if (posting.getAccount().isUnder(accountPrefix)) {
                double converted = converter.convert(
                        posting.getAmount(),
                        transaction.getCurrency(),
                        displayCurrency);

                System.out.printf("    %-30s : %10.2f | Display: %.2f %s%n",
                        posting.getAccountName(),
                        posting.getAmount(),
                        converted,
                        displayCurrency);
            }
        }
    }

    private void printTransactionWithDisplayAmount(Transaction transaction) {
        if (converter == null || !autoConvertDisplay) {
            System.out.println(transaction);
            return;
        }

        System.out.printf("ID: %d | Date: %s | Desc: %s | [%s -> %s]%n",
                transaction.getId(),
                transaction.getDateString(),
                transaction.getDescription(),
                transaction.getCurrency(),
                displayCurrency);

        List<Posting> postings = transaction.getPostings();
        for (Posting posting : postings) {
            double converted = converter.convert(
                    posting.getAmount(),
                    transaction.getCurrency(),
                    displayCurrency);

            System.out.printf("    %-30s : %10.2f | Display: %.2f %s%n",
                    posting.getAccountName(),
                    posting.getAmount(),
                    converted,
                    displayCurrency);
        }
    }

    public void deleteTransaction(int id) {
        logger.info("Deleting transaction with ID: " + id);
        Transaction transaction = findById(id);
        transactions.remove(transaction);
        save();
        refreshBalanceSheetCsv();
    }

    public void clearTransactions() {
        logger.info("Clearing all transactions.");
        transactions.clear();
        save();
        refreshBalanceSheetCsv();
        System.out.println("All transactions have been cleared.");
    }

    public void editTransaction(int id, String date, String desc, List<Posting> newPostings, String currency) {
        logger.info("Editing transactions with ID: " + id);
        Transaction transaction = findById(id);
        transaction.update(date, desc, newPostings, currency);
        save();
        refreshBalanceSheetCsv();
    }

    public Transaction getTransactionById(int id) {
        return findById(id);
    }

    private Transaction findById(int id) {
        return transactions.stream()
                .filter(t -> t.getId() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Transaction ID not found."));
    }

    private void save() {
        storage.save(transactions);
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    // =========================
    // BALANCE SHEET FEATURE
    // =========================

    public void printBalanceSheet() {
        printBalanceSheet(null, autoConvertDisplay, autoConvertDisplay ? displayCurrency : null);
    }

    public void printBalanceSheet(String accountPrefix) {
        printBalanceSheet(accountPrefix, autoConvertDisplay, autoConvertDisplay ? displayCurrency : null);
    }

    public void printBalanceSheet(String accountPrefix, boolean useConversion, String targetCurrency) {
        Map<String, Double> totals = buildBalanceSheetTotals(accountPrefix, useConversion, targetCurrency);

        String scope = (accountPrefix == null || accountPrefix.isBlank()) ? "ALL ACCOUNTS" : accountPrefix;
        String reportCurrency = useConversion ? targetCurrency : "MULTI";

        BalanceSheet balanceSheet = new BalanceSheet(totals, scope, reportCurrency, useConversion);
        balanceSheet.print();
        balanceSheet.exportToCsv("data/balance-sheet.csv");
    }

    private Map<String, Double> buildBalanceSheetTotals(String accountPrefix,
                                                        boolean useConversion,
                                                        String targetCurrency) {
        Map<String, Double> totals = new TreeMap<>();

        for (Transaction transaction : transactions) {
            for (Posting posting : transaction.getPostings()) {
                if (accountPrefix != null && !accountPrefix.isBlank()
                        && !posting.getAccount().isUnder(accountPrefix)) {
                    continue;
                }

                double amount = posting.getAmount();
                if (useConversion) {
                    if (converter == null) {
                        throw new IllegalStateException("Currency converter is not available.");
                    }
                    amount = converter.convert(amount, transaction.getCurrency(), targetCurrency);
                }

                String accountName = posting.getAccountName();
                totals.put(accountName, totals.getOrDefault(accountName, 0.0) + amount);
            }
        }

        return totals;
    }

    private void refreshBalanceSheetCsv() {
        try {
            // Calculate totals without triggering a console print
            Map<String, Double> totals = buildBalanceSheetTotals(null, autoConvertDisplay,
                    autoConvertDisplay ? displayCurrency : null);

            String reportCurrency = autoConvertDisplay ? displayCurrency : "MULTI";

            BalanceSheet balanceSheet = new BalanceSheet(totals, "ALL ACCOUNTS",
                    reportCurrency, autoConvertDisplay);

            // Only export to file, do not call balanceSheet.print()
            balanceSheet.exportToCsv("data/balance-sheet.csv");
        } catch (Exception e) {
            logger.warning("Unable to refresh balance sheet CSV: " + e.getMessage());
        }
    }
}
