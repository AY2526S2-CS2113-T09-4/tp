package seedu.duke;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * Stores and manages a collection of Transaction objects, providing operations
 * to add, list, edit, delete, and clear.
 */
public class TransactionsList {
    private static Logger logger = Logger.getLogger("TransactionsList");
    private ArrayList<Transaction> transactions;

    public TransactionsList() {
        this.transactions = new ArrayList<>();
        assert this.transactions != null : "Transactions list initialization failed.";
        logger.log(Level.INFO, "TransactionsList initialized");
    }

    public void addTransaction(Transaction transaction) {
        assert transaction != null : "Cannot add a null transaction.";
        transactions.add(transaction);
        assert transactions.contains(transaction) : "Transaction was not successfully added.";
        logger.log(Level.INFO, "Transaction added" + transaction.getId());
    }

    public void listTransactions() {
        logger.log(Level.INFO, "Listing Transaction");
        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }
        for (Transaction t : transactions) {
            System.out.println(t.toString());
        }
    }

    public void deleteTransaction(int id) {
        int initialSize = transactions.size();
        boolean removed = transactions.removeIf(t -> t.getId() == id);
        if (!removed) {
            throw new IllegalArgumentException("Transaction ID not found.");
        }
        assert transactions.size() == initialSize - 1 : "Transaction list size did not decrease by exactly one.";
        logger.log(Level.INFO, "Delete Transaction " + id);
    }

    public void clearTransactions() {
        transactions.clear();
        assert transactions.isEmpty() : "Transactions list should be empty after clear.";
        System.out.println("All transactions have been cleared.");
        logger.log(Level.INFO, "Clear all Transactions");
    }

    public void editTransaction(int id, String dateStr, String desc, Double amount, String type, String currency) {
        logger.log(Level.INFO, "Edit Transaction " + id);
        for (Transaction t : transactions) {
            if (t.getId() == id) {
                t.update(dateStr, desc, amount, type, currency);
                return;
            }
        }
        throw new IllegalArgumentException("Transaction ID not found.");
    }
}
