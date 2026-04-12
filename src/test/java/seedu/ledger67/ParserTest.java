package seedu.ledger67;

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

    @Test
    public void testUiAssistToggle() {
        // Toggle on and then off
        String input = "uiassist -on\nuiassist -off\nexit";
        runParserWithInput(input);

        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("UI Assist is now ON"));
        assertTrue(output.contains("UI Assist is now OFF"));
    }

    @Test
    public void testUiAssistAddManualSuccess() {
        // Simulation steps:
        // 1. uiassist -on
        // 2. add
        // 3. 1 (Manual Entry)
        // 4. 20/03/2026 (Date)
        // 5. SGD (Currency)
        // 6. Dinner (Description)
        // 7. Expenses:Food 25.50 (Posting 1)
        // 8. Assets:Cash -25.50 (Posting 2)
        // 9. [Empty Line] (Finish postings)
        // 10. exit
        String input = "uiassist -on\n" +
                "add\n" +
                "1\n" +
                "20/03/2026\n" +
                "SGD\n" +
                "Dinner\n" +
                "2\n" +
                "Expenses:Food 25.50\n" +
                "Assets:Cash -25.50\n" +
                "exit";

        runParserWithInput(input);

        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Enter date (DD/MM/YYYY)"));
        assertTrue(output.contains("Enter currency"));
        assertTrue(output.contains("How many postings do you want to enter?"));
        assertTrue(output.contains("Transaction added successfully"));

        // Verify the transaction exists in the list
        assertEquals(1, list.getTransactions().size());
        assertEquals("Dinner", list.getTransactions().get(0).getDescription());
    }

    @Test
    public void testUiAssistAddPresetSuccess() {
        // Simulation steps:
        // 1. uiassist -on
        // 2. add
        // 3. 2 (Preset)
        // 4. 21/03/2026 (Date)
        // 5. EUR (Currency)
        // 6. DAILYEXPENSE (Type)
        // 7. 50.00 (Amount)
        // 8. Work Lunch (Description)
        // 9. exit
        String input = "uiassist -on\n" +
                "add\n" +
                "2\n" +
                "21/03/2026\n" +
                "EUR\n" +
                "DAILYEXPENSE\n" +
                "50.00\n" +
                "Work Lunch\n" +
                "exit";

        runParserWithInput(input);

        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Use Preset"));
        assertTrue(output.contains("Transaction added successfully via preset."));

        Transaction t = list.getTransactions().get(0);
        assertEquals(50.0, t.getPostings().get(0).getAmount());
        assertEquals("EUR", t.getCurrency());
    }

    @Test
    public void testUiAssistListWithFilters() {
        // 1. uiassist -on
        // 2. list
        // 3. Expenses (Account filter)
        // 4. [Enter] (No start date)
        // 5. [Enter] (No end date)
        // 6. [Enter] (No regex)
        // 7. USD (To currency)
        // 8. exit
        String input = "uiassist -on\n" +
                "list\n" +
                "Expenses\n" +
                "\n" +
                "\n" +
                "\n" +
                "USD\n" +
                "exit";

        runParserWithInput(input);

        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Filter by Account?"));
        assertTrue(output.contains("[Generated Command: list -acc Expenses -to USD]"));
    }

    @Test
    public void testUiAssistDeleteById() {
        // Add a transaction first
        list.addTransaction(createTransaction("10/10/2023", "Coffee", 5.0, "debit", "USD"));
        int id = list.getTransactions().get(0).getId();

        // 1. uiassist -on
        // 2. delete
        // 3. 1 (Choice: ID)
        // 4. [ID]
        // 5. exit
        String input = "uiassist -on\n" +
                "delete\n" +
                "1\n" +
                id + "\n" +
                "exit";

        runParserWithInput(input);

        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Delete by Transaction ID"));
        assertTrue(output.contains("Successfully deleted Transaction " + id));
        assertTrue(list.getTransactions().isEmpty());
    }

    @Test
    public void testUiAssistConvertSimple() {
        // 1. uiassist -on
        // 2. convert
        // 3. 1 (Simple amount)
        // 4. 100
        // 5. USD
        // 6. SGD
        // 7. exit
        String input = "uiassist -on\n" +
                "convert\n" +
                "1\n" +
                "100\n" +
                "USD\n" +
                "SGD\n" +
                "exit";

        runParserWithInput(input);

        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Amount to convert"));
        assertTrue(output.contains("100.00 USD ="));
    }

    @Test
    public void testUiAssistBalance() {
        // 1. uiassist -on
        // 2. balance
        // 3. Assets (Filter)
        // 4. SGD (To currency)
        // 5. exit
        String input = "uiassist -on\n" +
                "balance\n" +
                "Assets\n" +
                "SGD\n" +
                "exit";

        runParserWithInput(input);

        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("[Generated Command: balance -acc Assets -to SGD]"));
        assertTrue(output.contains("===== BALANCE SHEET ====="));
    }

    @Test
    public void testListAccCommand_matchesAccountsDespiteWhitespaceAndCaseDifferences() {
        String input = "add -date 01/01/2026 -desc Test1 -p \"Assets:Cash 100\" -p \"Expenses:Food -100\" -c SGD\n"
                + "add -date 01/01/2026 -desc Test2 -p \"assets: Cash 200\" -p \"Expenses:Food -200\" -c SGD\n"
                + "list -acc Assets:Cash\n"
                + "exit";

        runParserWithInput(input);

        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Test1"));
        assertTrue(output.contains("Test2"));
    }

    @Test
    public void testInvalidAddDoesNotSkipTransactionId() {
        String input = "add -date 01/01/2026 -desc First -p \"Assets:Cash 100\" -p \"Expenses:Food -100\" -c SGD\n"
                + "add -date 2026-01-01 -desc BadDate -p \"Assets:Cash 100\" -p \"Expenses:Food -100\" -c SGD\n"
                + "add -date 02/01/2026 -desc Second -p \"Assets:Cash 200\" -p \"Expenses:Food -200\" -c SGD\n"
                + "exit\n";

        runParserWithInput(input);

        assertEquals(2, list.getTransactions().size());

        Transaction first = list.getTransactions().get(0);
        Transaction second = list.getTransactions().get(1);

        assertEquals("First", first.getDescription());
        assertEquals("Second", second.getDescription());
        assertEquals(first.getId() + 1, second.getId());
    }

    @Test
    public void testEditCommandRejectsUnbalancedPostingsAndAllowsLaterBalancedEdit() {
        Transaction t = createTransaction("10/10/2023", "Coffee", 5.0, "debit", "USD");
        list.addTransaction(t);
        int id = t.getId();

        outputStreamCaptor.reset();
        String input = "edit " + id + " -p \"Expenses:Food 10\"\n"
                + "edit " + id + " -desc Tea -p \"Expenses:Food 10\" -p \"Assets:Cash -10\"\n"
                + "list\n"
                + "exit";

        runParserWithInput(input);

        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Error: Update failed: Transaction is unbalanced."));
        assertTrue(output.contains("Transaction edited successfully."));
        assertTrue(output.contains("Tea"));
    }

}
