package seedu.ledger67;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Handles loading from and saving transactions to a local text file.
 */
public class Storage {
    private static final Logger logger = Logger.getLogger("Storage");
    private final String filePath;

    public Storage(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Loads transactions from the storage file.
     */
    public List<Transaction> load() {
        logger.info("Loading Transactions from file " + filePath);
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
                // We now expect 5 parts instead of 6: ID, Date, Desc, Currency, Postings
                if (parts.length < 5) {
                    logger.warning("Skipping malformed line in storage: " + line);
                    continue;
                }

                int id = Integer.parseInt(parts[0]);
                String date = parts[1];
                String description = unescape(parts[2]);
                String currency = parts[3];
                String postingsJoined = parts[4];

                // Deserialize the postings string back into the "Account Amount" format
                List<String> postingStrings = new ArrayList<>();
                if (!postingsJoined.isEmpty()) {
                    String[] postingTokens = postingsJoined.split(";");
                    for (String token : postingTokens) {
                        String[] accAmt = token.split("=");
                        if (accAmt.length == 2) {
                            // Rebuild the string to pass into the Transaction constructor
                            postingStrings.add(accAmt[0] + " " + accAmt[1]);
                        }
                    }
                }
                List<Posting> postings = Parser.convertStringList2PostingList(postingStrings);
                // Call your Transaction factory/constructor
                Transaction transaction = Transaction.fromStorage(id, date, description, postings, currency);
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
        logger.info("Saving " + transactions.size() + " transactions to file: " + filePath);
        File file = new File(filePath);
        File parent = file.getParentFile();

        try {
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                for (Transaction t : transactions) {
                    writer.write(formatTransaction(t));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to save transactions to file.");
        }
    }

    private String formatTransaction(Transaction t) {
        // Serialize the list of postings into a single string:
        // "Account1=50.0;Account2=-50.0"
        List<String> serializedPostings = new ArrayList<>();
        for (Posting p : t.getPostings()) {
            serializedPostings.add(p.getAccountName() + "=" + p.getAmount());
        }
        String postingsStr = String.join(";", serializedPostings);

        // The new format: ID \t Date \t Description \t Currency \t Postings
        return t.getId() + "\t"
                + t.getDateString() + "\t"
                + escape(t.getDescription()) + "\t"
                + t.getCurrency() + "\t"
                + postingsStr;
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
