package seedu.ledger67;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests loading and saving transactions using the Storage class.
 */
public class StorageTest {
    private static final String TEST_FILE_PATH = "data/storage-test-ledger.txt";

    private Transaction createTransaction(String date, String description,
                                          double amount, String type, String currency) {
        List<Posting> postings = new ArrayList<>();
        if (type.equals("debit")) {
            postings.add(new Posting("Expenses:General", amount));
            postings.add(new Posting("Assets:Cash", -amount));
        } else {
            postings.add(new Posting("Assets:Cash", amount));
            postings.add(new Posting("Income:Salary", amount));
        }
        return new Transaction(date, description, postings, currency);
    }

    @AfterEach
    public void tearDown() {
        File file = new File(TEST_FILE_PATH);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    public void testLoadWhenFileDoesNotExist() {
        Storage storage = new Storage(TEST_FILE_PATH);

        List<Transaction> transactions = storage.load();

        Assertions.assertNotNull(transactions);
        Assertions.assertTrue(transactions.isEmpty());
    }

    @Test
    public void testSaveAndLoadTransactions() {
        Storage storage = new Storage(TEST_FILE_PATH);

        Transaction t1 = createTransaction("15/03/2023", "Groceries", 50.0, "debit", "USD");
        Transaction t2 = createTransaction("16/03/2023", "Salary", 1000.0, "credit", "SGD");

        storage.save(List.of(t1, t2));

        List<Transaction> loadedList = storage.load();

        Assertions.assertEquals(2, loadedList.size());
        Assertions.assertEquals("Groceries", loadedList.get(0).getDescription());
        Assertions.assertEquals("Salary", loadedList.get(1).getDescription());
    }

    @Test
    public void testSaveAndLoadPreservesTransactionDetails() {
        Storage storage = new Storage(TEST_FILE_PATH);

        Transaction t = createTransaction("20/03/2023", "Book Purchase", 25.75, "debit", "EUR");
        storage.save(List.of(t));

        List<Transaction> loadedList = storage.load();

        Assertions.assertEquals(1, loadedList.size());
        Transaction loaded = loadedList.get(0);

        Assertions.assertEquals("20/03/2023", loaded.getDateString());
        Assertions.assertEquals("Book Purchase", loaded.getDescription());
        Assertions.assertEquals(25.75, loaded.getPostings().get(0).getAmount(), 0.0001);
        Assertions.assertEquals("EUR", loaded.getCurrency());
    }

    @Test
    public void testSaveEmptyListAndLoad() {
        Storage storage = new Storage(TEST_FILE_PATH);

        storage.save(List.of());
        List<Transaction> loadedList = storage.load();

        Assertions.assertNotNull(loadedList);
        Assertions.assertTrue(loadedList.isEmpty());
    }

    @Test
    public void testSaveAndLoadUpdatedConvertedTransaction() {
        Storage storage = new Storage(TEST_FILE_PATH);

        Transaction t = createTransaction("20/03/2023", "Book Purchase", 25.75, "debit", "USD");
        List<Posting> newPostings = new ArrayList<>();
        newPostings.add(new Posting("Expenses:General", 34.50));
        newPostings.add(new Posting("Assets:Cash", -34.50));
        t.update(null, null, newPostings, "SGD");

        storage.save(List.of(t));
        List<Transaction> loadedList = storage.load();

        Assertions.assertEquals(1, loadedList.size());
        Transaction loaded = loadedList.get(0);

        Assertions.assertEquals(34.50, loaded.getPostings().get(0).getAmount(), 0.0001);
        Assertions.assertEquals("SGD", loaded.getCurrency());
        Assertions.assertEquals("Book Purchase", loaded.getDescription());
    }

    @Test
    public void testLoadSkipsCorruptedLineAndContinues() throws Exception {
        File file = new File(TEST_FILE_PATH);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        Storage storage = new Storage(TEST_FILE_PATH);
        Transaction valid = createTransaction("21/03/2023", "Valid Txn", 12.5, "debit", "USD");
        storage.save(List.of(valid));

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TEST_FILE_PATH, true))) {
            writer.newLine();
            writer.write("bad\tcorrupted\tline");
        }

        List<Transaction> loaded = storage.load();
        Assertions.assertEquals(1, loaded.size());
        Assertions.assertEquals("Valid Txn", loaded.get(0).getDescription());
    }

    @Test
    public void testSaveAndLoadEscapedDescription() {
        Storage storage = new Storage(TEST_FILE_PATH);

        Transaction t = createTransaction("22/03/2023", "Line1\tTabbed\nLine2\\Slash", 10.0, "debit", "USD");
        storage.save(List.of(t));

        List<Transaction> loaded = storage.load();

        Assertions.assertEquals(1, loaded.size());
        Assertions.assertEquals("Line1\tTabbed\nLine2\\Slash", loaded.get(0).getDescription());
    }
}
