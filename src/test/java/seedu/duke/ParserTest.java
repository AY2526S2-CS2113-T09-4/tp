package seedu.duke;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests user input parsing, argument mapping, and command execution of the
 * Parser class.
 */
public class ParserTest {
    private TransactionsList list;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeEach
    public void setUp() {
        list = new TransactionsList();
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    private void runParserWithInput(String input) {
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        Parser parser = new Parser(list);
        parser.start();
    }

    @Test
    public void testAddCommandSuccess() {
        String input = "add transaction -d 15/03/2023 -desc Lunch -a 15.5 -t debit -c SGD\nexit";
        runParserWithInput(input);

        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Transaction added successfully."));

        list.listTransactions();
        assertTrue(outputStreamCaptor.toString().contains("Lunch"));
    }

    @Test
    public void testAddCommandMissingAmount() {
        String input = "add transaction -d 15/03/2023 -desc Lunch -t debit -c SGD\nexit";
        runParserWithInput(input);

        assertTrue(outputStreamCaptor.toString().contains("Error: Missing amount."));
    }

    @Test
    public void testListCommand() {
        list.addTransaction(new Transaction("10/10/2023", "Coffee", 5.0, "debit", "USD"));
        String input = "list transaction\nexit";
        runParserWithInput(input);

        assertTrue(outputStreamCaptor.toString().contains("Coffee"));
    }

    @Test
    public void testDeleteCommand() {
        Transaction t = new Transaction("10/10/2023", "Coffee", 5.0, "debit", "USD");
        list.addTransaction(t);
        int id = t.getId();

        String input = "delete transaction " + id + "\nexit";
        runParserWithInput(input);

        assertTrue(outputStreamCaptor.toString().contains("Transaction deleted successfully."));
    }

    @Test
    public void testEditCommand() {
        Transaction t = new Transaction("10/10/2023", "Coffee", 5.0, "debit", "USD");
        list.addTransaction(t);
        int id = t.getId();

        String input = "edit transaction " + id + " -desc Tea -a 6.0\nlist\nexit";
        runParserWithInput(input);

        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Transaction edited successfully."));
        assertTrue(output.contains("Tea"));
        assertTrue(output.contains("6.00"));
    }

    @Test
    public void testClearCommand() {
        list.addTransaction(new Transaction("10/10/2023", "Coffee", 5.0, "debit", "USD"));
        String input = "clear transaction\nlist\nexit";
        runParserWithInput(input);

        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("All transactions have been cleared."));
        assertTrue(output.contains("No transactions found."));
    }
}
