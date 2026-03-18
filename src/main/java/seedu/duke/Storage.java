package seedu.duke;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles loading from and saving transactions to a local text file.
 */
public class Storage {
    private final String filePath;

    public Storage(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Loads transactions from the storage file.
     */
    public List<Transaction> load() {
        List<Transaction> transactions = new ArrayList<>();
        File file = new File(filePath);

        if (!file.exists()) {
            return transactions;
        }

        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            int maxId = 0;

            for (String line : lines) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = line.split("\t", -1);
                if (parts.length != 6) {
                    continue;
                }

                int id = Integer.parseInt(parts[0]);
                String date = parts[1];
                String description = unescape(parts[2]);
                double amount = Double.parseDouble(parts[3]);
                String type = parts[4];
                String currency = parts[5];

                Transaction transaction = Transaction.fromStorage(id, date, description, amount, type, currency);
                transactions.add(transaction);

                if (id > maxId) {
                    maxId = id;
                }
            }

            Transaction.updateNextId(maxId + 1);

        } catch (IOException e) {
            throw new RuntimeException("Unable to load transactions from file.");
        }

        return transactions;
    }

    /**
     * Saves all transactions into the storage file.
     */
    public void save(List<Transaction> transactions) {
        File file = new File(filePath);
        File parent = file.getParentFile();

        try {
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));

            for (Transaction t : transactions) {
                writer.write(formatTransaction(t));
                writer.newLine();
            }

            writer.close();
        } catch (IOException e) {
            throw new RuntimeException("Unable to save transactions to file.");
        }
    }

    private String formatTransaction(Transaction t) {
        return t.getId() + "\t"
                + t.getDateString() + "\t"
                + escape(t.getDescription()) + "\t"
                + t.getAmount() + "\t"
                + t.getType() + "\t"
                + t.getCurrency();
    }

    private String escape(String text) {
        return text.replace("\\", "\\\\")
                .replace("\t", "\\t")
                .replace("\n", "\\n");
    }

    private String unescape(String text) {
        return text.replace("\\n", "\n")
                .replace("\\t", "\t")
                .replace("\\\\", "\\");
    }
}
