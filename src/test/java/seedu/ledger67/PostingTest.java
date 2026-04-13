package seedu.ledger67;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PostingTest {

    @Test
    public void testPostingForAssetsUsesSameInternalAmount() {
        Posting posting = new Posting("Assets:Cash", 10.0);
        assertEquals(10.0, posting.getInternalAmount(), 0.0001);
    }

    @Test
    public void testPostingForExpensesUsesSameInternalAmount() {
        Posting posting = new Posting("Expenses:Food", 10.0);
        assertEquals(10.0, posting.getInternalAmount(), 0.0001);
    }

    @Test
    public void testPostingForIncomeNegatesInternalAmount() {
        Posting posting = new Posting("Income:Salary", 10.0);
        assertEquals(-10.0, posting.getInternalAmount(), 0.0001);
    }

    @Test
    public void testPostingForLiabilitiesNegatesInternalAmount() {
        Posting posting = new Posting("Liabilities:Loan", 10.0);
        assertEquals(-10.0, posting.getInternalAmount(), 0.0001);
    }

    @Test
    public void testPostingForEquityNegatesInternalAmount() {
        Posting posting = new Posting("Equity:Capital", 10.0);
        assertEquals(-10.0, posting.getInternalAmount(), 0.0001);
    }

    @Test
    public void testZeroAmountThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Posting("Assets:Cash", 0.0));
    }

    @Test
    public void testGetters() {
        Posting posting = new Posting("Assets:Cash", -25.5);

        assertEquals("Assets:Cash", posting.getAccountName());
        assertEquals(-25.5, posting.getAmount(), 0.0001);
        assertEquals("Assets:Cash", posting.getAccount().getFullName());
    }
}
