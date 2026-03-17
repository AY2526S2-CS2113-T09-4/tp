package seedu.duke;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.Assertions;

/**
 * Tests adding, listing, editing, deleting, and clearing functions of the
 * TransactionsList class.
 */
public class TransactionsListTest {
    private TransactionsList list;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeEach
    public void setUp() {
        list = new TransactionsList();
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @Test
    public void testAddAndListTransactions() {
        Transaction t = new Transaction("15/03/2023", "Salary", 5000.0, "debit", "USD");
        list.addTransaction(t);
        list.listTransactions();

        String output = outputStreamCaptor.toString().trim();
        Assertions.assertTrue(output.contains("Salary"));
        Assertions.assertTrue(output.contains("5000.00"));
    }

    @Test
    public void testDeleteTransactionSuccess() {
        Transaction t = new Transaction("15/03/2023", "Salary", 5000.0, "debit", "USD");
        list.addTransaction(t);
        int id = t.getId();

        Assertions.assertDoesNotThrow(() -> list.deleteTransaction(id));

        list.listTransactions();
        Assertions.assertTrue(outputStreamCaptor.toString().trim().contains("No transactions found."));
    }

    @Test
    public void testDeleteTransactionNotFound() {
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            list.deleteTransaction(999);
        });
        Assertions.assertEquals("Transaction ID not found.", exception.getMessage());
    }

    @Test
    public void testEditTransactionSuccess() {
        Transaction t = new Transaction("15/03/2023", "Rent", 1000.0, "debit", "USD");
        list.addTransaction(t);
        int id = t.getId();

        list.editTransaction(id, null, null, 1200.0, null, null);
        list.listTransactions();

        Assertions.assertTrue(outputStreamCaptor.toString().trim().contains("1200.00"));
    }

    @Test
    public void testClearTransactions() {
        list.addTransaction(new Transaction("15/03/2023", "A", 10.0, "debit", "USD"));
        list.addTransaction(new Transaction("16/03/2023", "B", 20.0, "debit", "USD"));

        list.clearTransactions();
        list.listTransactions();

        String output = outputStreamCaptor.toString();
        Assertions.assertTrue(output.contains("All transactions have been cleared."));
        Assertions.assertTrue(output.contains("No transactions found."));
    }
}
