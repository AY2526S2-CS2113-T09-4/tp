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

    @Test
    public void testWhitespaceAroundColonIsNormalized() {
        Account acc = new Account(" Assets : Cash ");

        assertEquals("Assets:Cash", acc.getFullName());
        assertEquals("assets", acc.getRoot());
        assertEquals(2, acc.getHierarchy().size());
    }

    @Test
    public void testRootCaseIsNormalizedButSubaccountCaseIsPreserved() {
        Account acc = new Account("assets:DBS");

        assertEquals("Assets:DBS", acc.getFullName());
        assertEquals("assets", acc.getRoot());
    }

    @Test
    public void testIsUnder_caseInsensitiveMatching() {
        Account acc = new Account("assets:Food");

        assertTrue(acc.isUnder("Assets:Food"));
        assertTrue(acc.isUnder("ASSETS:FOOD"));
        assertTrue(acc.isUnder("assets"));
    }

    @Test
    public void testConstructor_normalizesRootOnly() {
        Account acc = new Account("assets:Bank:DBS");

        assertEquals("Assets:Bank:DBS", acc.getFullName());
        assertEquals("assets", acc.getRoot());
    }

    @Test
    public void testConstructor_preservesSubAccountCase() {
        Account acc = new Account("Assets:bank:DbS");

        assertEquals("Assets:bank:DbS", acc.getFullName());
        assertEquals(3, acc.getHierarchy().size());
    }

    @Test
    public void testBlankAccountThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new Account("   "));
    }

    @Test
    public void testNullAccountThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new Account(null));
    }

    @Test
    public void testIsUnder_nullAndBlankParentReturnFalse() {
        Account acc = new Account("Assets:Bank:DBS");

        assertFalse(acc.isUnder(null));
        assertFalse(acc.isUnder(" "));
    }

    @Test
    public void testToStringReturnsFullName() {
        Account acc = new Account("Income:Salary");

        assertEquals("Income:Salary", acc.toString());
    }
}
