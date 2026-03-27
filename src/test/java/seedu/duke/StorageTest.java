package seedu.duke;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

/**
 * Tests loading and saving transactions using the Storage class.
 */
public class StorageTest {
    private static final String TEST_FILE_PATH = "data/storage-test-ledger.txt";

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

        Transaction t1 = new Transaction("15/03/2023", "Groceries", 50.0, "debit", "USD");
        Transaction t2 = new Transaction("16/03/2023", "Salary", 1000.0, "credit", "SGD");

        storage.save(List.of(t1, t2));

        List<Transaction> loadedList = storage.load();

        Assertions.assertEquals(2, loadedList.size());
        Assertions.assertEquals("Groceries", loadedList.get(0).getDescription());
        Assertions.assertEquals("Salary", loadedList.get(1).getDescription());
    }

    @Test
    public void testSaveAndLoadPreservesTransactionDetails() {
        Storage storage = new Storage(TEST_FILE_PATH);

        Transaction t = new Transaction("20/03/2023", "Book Purchase", 25.75, "debit", "EUR");
        storage.save(List.of(t));

        List<Transaction> loadedList = storage.load();

        Assertions.assertEquals(1, loadedList.size());
        Transaction loaded = loadedList.get(0);

        Assertions.assertEquals("20/03/2023", loaded.getDateString());
        Assertions.assertEquals("Book Purchase", loaded.getDescription());
        Assertions.assertEquals(25.75, loaded.getAmount(), 0.0001);
        Assertions.assertEquals("debit", loaded.getType());
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

        Transaction t = new Transaction("20/03/2023", "Book Purchase", 25.75, "debit", "USD");
        t.update(null, null, 34.50, null, "SGD");

        storage.save(List.of(t));
        List<Transaction> loadedList = storage.load();

        Assertions.assertEquals(1, loadedList.size());
        Transaction loaded = loadedList.get(0);

        Assertions.assertEquals(34.50, loaded.getAmount(), 0.0001);
        Assertions.assertEquals("SGD", loaded.getCurrency());
        Assertions.assertEquals("Book Purchase", loaded.getDescription());
    }
}
