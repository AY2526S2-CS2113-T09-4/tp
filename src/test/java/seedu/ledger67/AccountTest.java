package seedu.ledger67;

import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AccountTest {

    @Test
    public void testValidAccountHierarchy() {
        Account acc = new Account("Assets:Bank:DBS");

        assertEquals("Assets:Bank:DBS", acc.getFullName());
        assertEquals("assets", acc.getRoot());
        assertEquals(3, acc.getHierarchy().size());
    }

    @Test
    public void testInvalidRootThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Account("Random:Cash");
        });
    }

    @Test
    public void testIsUnderHierarchy() {
        Account acc = new Account("Assets:Bank:DBS");

        assertTrue(acc.isUnder("Assets"));
        assertTrue(acc.isUnder("Assets:Bank"));
        assertTrue(acc.isUnder("Assets:Bank:DBS"));

        assertFalse(acc.isUnder("Expenses"));
        assertFalse(acc.isUnder("Assets:Cash"));
    }
}
