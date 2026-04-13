package seedu.ledger67;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests the creation, validation, and update logic of the Transaction class.
 */
public class TransactionTest {

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

    @Test
    public void testValidTransactionCreation() {
        Transaction t = createTransaction("15/03/2023", "Groceries", 50.0, "debit", "USD");
        Assertions.assertNotNull(t);
        Assertions.assertTrue(t.getId() > 0);
        Assertions.assertTrue(t.toString().contains("Groceries"));
        Assertions.assertTrue(t.toString().contains("15/03/2023"));
        Assertions.assertTrue(t.toString().contains("50.00"));
    }

    @Test
    public void testInvalidDateFormatThrowsException() {
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () ->
                createTransaction("2023-03-15", "Groceries", 50.0, "debit", "USD"));
        Assertions.assertEquals("DATE must be a valid calendar date in DD/MM/YYYY format.", exception.getMessage());
    }

    @Test
    public void testMissingDetailsThrowsException() {
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            List<Posting> postings = new ArrayList<>();
            postings.add(new Posting("Expenses:General", 50.0));
            postings.add(new Posting("Assets:Cash", -50.0));
            new Transaction(null, "Groceries", postings, "USD");
        });
        Assertions.assertEquals("Missing required transaction details.", exception.getMessage());
    }

    @Test
    public void testEmptyDetailsThrowsException() {
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            List<Posting> postings = new ArrayList<>();
            postings.add(new Posting("Expenses:General", 50.0));
            postings.add(new Posting("Assets:Cash", -50.0));
            new Transaction("15/03/2023", "   ", postings, "USD");
        });
        Assertions.assertEquals("Description cannot be empty.", exception.getMessage());
    }

    @Test
    public void testUpdateTransaction() {
        Transaction t = createTransaction("15/03/2023", "Groceries", 50.0, "debit", "USD");
        List<Posting> newPostings = new ArrayList<>();
        newPostings.add(new Posting("Expenses:General", 60.5));
        newPostings.add(new Posting("Assets:Cash", -60.5));
        t.update("16/03/2023", "Supermarket", newPostings, null);

        String output = t.toString();
        Assertions.assertTrue(output.contains("16/03/2023"));
        Assertions.assertTrue(output.contains("Supermarket"));
        Assertions.assertTrue(output.contains("60.50"));
        Assertions.assertTrue(output.contains("USD"));
    }

    @Test
    public void testParseDateSuccess() {
        LocalDate date = Transaction.parseDate("01/01/2026");
        Assertions.assertEquals(LocalDate.of(2026, 1, 1), date);
    }

    @Test
    public void testIsBalancedTrue() {
        Transaction t = createTransaction("15/03/2023", "Balanced", 10.0, "debit", "USD");
        Assertions.assertTrue(t.isBalanced());
    }

    @Test
    public void testIsBalancedFalse() {
        List<Posting> postings = new ArrayList<>();
        postings.add(new Posting("Assets:Cash", 10.0));
        postings.add(new Posting("Expenses:Food", 10.0));

        Transaction t = new Transaction("15/03/2023", "Unbalanced", postings, "USD");
        Assertions.assertFalse(t.isBalanced());
    }

    @Test
    public void testUpdateBlankDescriptionThrows() {
        Transaction t = createTransaction("15/03/2023", "Groceries", 50.0, "debit", "USD");

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> t.update(null, "   ", null, null));
    }

    @Test
    public void testUpdateUnbalancedPostingsThrows() {
        Transaction t = createTransaction("15/03/2023", "Groceries", 50.0, "debit", "USD");

        List<Posting> badPostings = new ArrayList<>();
        badPostings.add(new Posting("Assets:Cash", 10.0));
        badPostings.add(new Posting("Expenses:Food", 10.0));

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> t.update(null, null, badPostings, null));
    }

    @Test
    public void testFromStoragePreservesId() {
        List<Posting> postings = new ArrayList<>();
        postings.add(new Posting("Expenses:General", 20.0));
        postings.add(new Posting("Assets:Cash", -20.0));

        Transaction t = Transaction.fromStorage(99, "20/03/2023", "Loaded", postings, "EUR");

        Assertions.assertEquals(99, t.getId());
        Assertions.assertEquals("Loaded", t.getDescription());
        Assertions.assertEquals("EUR", t.getCurrency());
    }

    @Test
    public void testUpdateNextIdAndRollbackDoNotThrow() {
        Transaction.updateNextId(1000);
        Transaction.rollbackNextIdIfUnused(999);
        Assertions.assertTrue(true);
    }
}
