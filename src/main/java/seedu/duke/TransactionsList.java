package seedu.duke;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Stores and manages all transactions in memory, and saves them using Storage.
 */
public class TransactionsList {
    private final List<Transaction> transactions;
    private final Storage storage;

    public TransactionsList(Storage storage) {
        this.storage = storage;
        this.transactions = new ArrayList<>(storage.load());
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
        save();
    }

    public void listTransactions() {
        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }

        transactions.stream()
                .sorted(Comparator.comparing(Transaction::getDate))
                .forEach(System.out::println);
    }

    public void deleteTransaction(int id) {
        Transaction transaction = findById(id);
        transactions.remove(transaction);
        save();
    }

    public void clearTransactions() {
        transactions.clear();
        save();
        System.out.println("All transactions cleared.");
    }

    public void editTransaction(int id, String date, String desc, Double amount, String type, String currency) {
        Transaction transaction = findById(id);
        transaction.update(date, desc, amount, type, currency);
        save();
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
}
