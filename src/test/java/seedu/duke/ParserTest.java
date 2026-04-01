package seedu.duke;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests user input parsing, argument mapping, and command execution of the
 * Parser class.
 */
public class ParserTest {
    private static final String TEST_LEDGER_FILE = "data/parser-test-ledger.txt";
    private static final String TEST_RATES_FILE = "data/exchange-rates.json";

    private TransactionsList list;
    private CurrencyConverter converter;
    private ExchangeRateStorage exchangeRateStorage;
    private LiveExchangeRateService liveExchangeRateService;

    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    private Transaction createTransaction(String date, String description,
            double amount, String type, String currency) {
        List<Posting> postings = new ArrayList<>();
        if (type.equals("debit")) {
            postings.add(new Posting("Expenses:General", amount));
            postings.add(new Posting("Assets:Cash", -amount)); // Credit balancing entry
        } else {
            postings.add(new Posting("Assets:Cash", amount));
            postings.add(new Posting("Expenses:General", -amount)); // Debit balancing entry
        }
        return new Transaction(date, description, postings, currency);
    }

    @BeforeEach
    public void setUp() throws Exception {
        deleteFile(TEST_LEDGER_FILE);

        File ratesFile = new File(TEST_RATES_FILE);
        File parent = ratesFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (FileWriter writer = new FileWriter(TEST_RATES_FILE)) {
            writer.write("{\"base\":\"EUR\",\"date\":\"2026-03-19\",\"rates\":{\"SGD\":1.46,\"USD\":1.09}}");
        }

        list = new TransactionsList(new Storage(TEST_LEDGER_FILE));

        Map<String, Double> rates = new HashMap<>();
        rates.put("SGD", 1.46);
        rates.put("USD", 1.09);
        ExchangeRateData rateData = new ExchangeRateData("EUR", "2026-03-19", rates);

        converter = new CurrencyConverter(rateData);
        exchangeRateStorage = new ExchangeRateStorage(TEST_RATES_FILE);
        liveExchangeRateService = new LiveExchangeRateService();

        list.setCurrencyConverter(converter);

        outputStreamCaptor.reset();
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    public void tearDown() {
        deleteFile(TEST_LEDGER_FILE);
    }

    private void runParserWithInput(String input) {
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        Parser parser = new Parser(list, converter, exchangeRateStorage, liveExchangeRateService);
        parser.start();
    }

    private void deleteFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    public void testAddCommandSuccess() {
        String input = "add -date 15/03/2023 -desc Lunch -p " +
                "\"Expenses 15.5\" -p \"Assets -15.5\" -c SGD\nexit";
        runParserWithInput(input);

        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Transaction added successfully"));

        list.listTransactions();
        assertTrue(outputStreamCaptor.toString().contains("Lunch"));
    }

    @Test
    public void testListCommand() {
        list.addTransaction(createTransaction("10/10/2023", "Coffee", 5.0, "debit", "USD"));

        outputStreamCaptor.reset();
        String input = "list\nexit";
        runParserWithInput(input);

        assertTrue(outputStreamCaptor.toString().contains("Coffee"));
    }

    @Test
    public void testDeleteCommand() {
        Transaction t = createTransaction("10/10/2023", "Coffee", 5.0, "debit", "USD");
        list.addTransaction(t);
        int id = t.getId();

        outputStreamCaptor.reset();
        String input = "delete " + id + "\nexit";
        runParserWithInput(input);

        assertTrue(outputStreamCaptor.toString().contains("Successfully deleted"));
    }

    @Test
    public void testEditCommand() {
        Transaction t = createTransaction("10/10/2023", "Coffee", 5.0, "debit", "USD");
        list.addTransaction(t);
        int id = t.getId();

        outputStreamCaptor.reset();
        String input = "edit " + id + " -desc Tea\nlist\nexit";
        runParserWithInput(input);

        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Transaction edited successfully."));
        assertTrue(output.contains("Tea"));
    }

    @Test
    public void testClearCommand() {
        list.addTransaction(createTransaction("10/10/2023", "Coffee", 5.0, "debit", "USD"));

        outputStreamCaptor.reset();
        String input = "clear\nlist\nexit";
        runParserWithInput(input);

        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("All transactions have been cleared."));
        assertTrue(output.contains("No matching transactions found."));
    }

    @Test
    public void testConvertCommand() {
        String input = "convert -a 100 -from USD -to SGD\nexit";
        runParserWithInput(input);

        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("100.00 USD ="));
        assertTrue(output.contains("SGD"));
    }

    @Test
    public void testHelpCommand() {
        String input = "help\nexit";
        runParserWithInput(input);

        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("=== Ledger67 Help ==="));
        assertTrue(output.contains("convert transaction"));
        assertTrue(output.contains("confirm all"));
    }

    @Test
    public void testListAccCommand() {
        list.addTransaction(createTransaction("10/10/2023", "Coffee", 5.0, "debit", "USD"));

        outputStreamCaptor.reset();
        String input = "list -acc Assets\nexit";
        runParserWithInput(input);

        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Assets"));
    }

    @Test
    public void testBalanceCommand() {
        list.addTransaction(createTransaction("10/10/2023", "Coffee", 5.0, "debit", "USD"));

        outputStreamCaptor.reset();
        String input = "balance\nexit";
        runParserWithInput(input);

        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("===== BALANCE SHEET ====="));
    }

    @Test
    public void testBalanceAccCommand() {
        list.addTransaction(createTransaction("10/10/2023", "Coffee", 5.0, "debit", "USD"));

        outputStreamCaptor.reset();
        String input = "balance -acc Assets\nexit";
        runParserWithInput(input);

        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Scope: Assets"));
    }

    @Test
    public void generatePostings_dailyExpense_success() {
        List<Posting> postings = PresetHandler.generatePostings("DAILYEXPENSE 50.00");
        assertEquals(2, postings.size());
        
        // Expenses +50, Cash -50
        assertEquals("Expenses", postings.get(0).getAccountName());
        assertEquals(50.0, postings.get(0).getAmount());
        assertEquals("Assets:Cash", postings.get(1).getAccountName());
        assertEquals(-50.0, postings.get(1).getAmount());
    }

    @Test
    public void generatePostings_income_success() {
        List<Posting> postings = PresetHandler.generatePostings("INCOME 1000");
        assertEquals(2, postings.size());
        
        // Bank +1000, Income -1000
        assertEquals("Assets:Bank", postings.get(0).getAccountName());
        assertEquals(1000.0, postings.get(0).getAmount());
        assertEquals("Income", postings.get(1).getAccountName());
        assertEquals(-1000.0, postings.get(1).getAmount());
    }

    @Test
    public void generatePostings_invalidFormat_exceptionThrown() {
        // Missing amount
        assertThrows(IllegalArgumentException.class, () -> {
            PresetHandler.generatePostings("DAILYEXPENSE");
        });
    }

    @Test
    public void generatePostings_invalidAmount_exceptionThrown() {
        // Amount is not a number
        assertThrows(IllegalArgumentException.class, () -> {
            PresetHandler.generatePostings("DAILYEXPENSE abc");
        });
    }

    @Test
    public void generatePostings_unknownType_exceptionThrown() {
        // Type does not exist
        assertThrows(IllegalArgumentException.class, () -> {
            PresetHandler.generatePostings("GIFT 10.00");
        });
    }

    @Test
    public void generatePostings_emptyInput_exceptionThrown() {
        assertThrows(IllegalArgumentException.class, () -> {
            PresetHandler.generatePostings("");
        });
    }
}
