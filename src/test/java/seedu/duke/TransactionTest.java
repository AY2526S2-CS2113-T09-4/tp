package seedu.duke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

/**
 * Tests the creation, validation, and update logic of the Transaction class.
 */
public class TransactionTest {

    @Test
    public void testValidTransactionCreation() {
        Transaction t = new Transaction("15/03/2023", "Groceries", 50.0, "debit", "USD");
        Assertions.assertNotNull(t);
        Assertions.assertTrue(t.getId() > 0);
        Assertions.assertTrue(t.toString().contains("Groceries"));
        Assertions.assertTrue(t.toString().contains("15/03/2023"));
        Assertions.assertTrue(t.toString().contains("50.00"));
    }

    @Test
    public void testInvalidDateFormatThrowsException() {
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Transaction("2023-03-15", "Groceries", 50.0, "debit", "USD");
        });
        Assertions.assertEquals("DATE must be in DD/MM/YYYY format.", exception.getMessage());
    }

    @Test
    public void testMissingDetailsThrowsException() {
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Transaction(null, "Groceries", 50.0, "debit", "USD");
        });
        Assertions.assertEquals("Missing required transaction details.", exception.getMessage());
    }

    @Test
    public void testEmptyDetailsThrowsException() {
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Transaction("15/03/2023", "   ", 50.0, "debit", "USD");
        });
        Assertions.assertEquals("Description cannot be empty.", exception.getMessage());
    }

    @Test
    public void testUpdateTransaction() {
        Transaction t = new Transaction("15/03/2023", "Groceries", 50.0, "debit", "USD");
        t.update("16/03/2023", "Supermarket", 60.5, null, null);

        String output = t.toString();
        Assertions.assertTrue(output.contains("16/03/2023"));
        Assertions.assertTrue(output.contains("Supermarket"));
        Assertions.assertTrue(output.contains("60.50"));
        Assertions.assertTrue(output.contains("debit")); // Unchanged
        Assertions.assertTrue(output.contains("USD")); // Unchanged
    }
}
