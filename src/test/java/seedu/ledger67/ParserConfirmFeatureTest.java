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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the confirm flow for converted transactions.
 */
public class ParserConfirmFeatureTest {
    private static final String TEST_LEDGER_FILE = "data/parser-confirm-test-ledger.txt";
    private static final String TEST_RATES_FILE = "data/parser-confirm-test-rates.json";

    private TransactionsList list;
    private CurrencyConverter converter;
    private ExchangeRateStorage exchangeRateStorage;
    private LiveExchangeRateService liveExchangeRateService;

    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

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
        deleteFile(TEST_RATES_FILE);

        File ratesFile = new File(TEST_RATES_FILE);
        File parent = ratesFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (FileWriter writer = new FileWriter(TEST_RATES_FILE)) {
            writer.write("{\"base\":\"EUR\",\"date\":\"2026-03-25\",\"rates\":{\"SGD\":1.46,\"USD\":1.09}}");
        }

        Storage storage = new Storage(TEST_LEDGER_FILE);
        list = new TransactionsList(storage);

        Map<String, Double> rates = new HashMap<>();
        rates.put("SGD", 1.46);
        rates.put("USD", 1.09);

        ExchangeRateData rateData = new ExchangeRateData("EUR", "2026-03-25", rates);
        converter = new CurrencyConverter(rateData);
        list.setCurrencyConverter(converter);

        exchangeRateStorage = new ExchangeRateStorage(TEST_RATES_FILE);
        liveExchangeRateService = new LiveExchangeRateService();

        outputStreamCaptor.reset();
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    public void tearDown() {
        System.setOut(originalOut);
        deleteFile(TEST_LEDGER_FILE);
        deleteFile(TEST_RATES_FILE);
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
    public void testConvertTransactionThenConfirmStoresConvertedValue() {
        Transaction t = createTransaction("10/10/2023", "Coffee", 5.0, "debit", "USD");
        list.addTransaction(t);
        int id = t.getId();

        outputStreamCaptor.reset();
        String input = "convert transaction " + id + " -to SGD\nconfirm\nexit\n";
        runParserWithInput(input);

        Transaction updated = list.getTransactionById(id);
        double expected = converter.convert(5.0, "USD", "SGD");

        assertEquals("SGD", updated.getCurrency());
        assertEquals(expected, updated.getPostings().get(0).getAmount(), 0.0001);

        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Transaction " + id + " confirmed and stored in SGD."));
        assertTrue(output.contains("Type 'confirm' to store this transaction in SGD."));
    }

    @Test
    public void testConvertTransactionThenOtherCommandIgnoresPendingConversion() {
        Transaction t = createTransaction("10/10/2023", "Coffee", 5.0, "debit", "USD");
        list.addTransaction(t);
        int id = t.getId();

        outputStreamCaptor.reset();
        String input = "convert transaction " + id + " -to SGD\nhelp\nexit\n";
        runParserWithInput(input);

        Transaction unchanged = list.getTransactionById(id);

        assertEquals("USD", unchanged.getCurrency());
        assertEquals(5.0, unchanged.getPostings().get(0).getAmount(), 0.0001);

        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("=== Ledger67 Help ==="));
    }

    @Test
    public void testListTransactionToThenConfirmIdStoresOneTransactionOnly() {
        Transaction t1 = createTransaction("10/10/2023", "Coffee", 5.0, "debit", "USD");
        Transaction t2 = createTransaction("11/10/2023", "Tea", 10.0, "debit", "USD");
        list.addTransaction(t1);
        list.addTransaction(t2);

        int id1 = t1.getId();
        int id2 = t2.getId();

        outputStreamCaptor.reset();
        String input = "list transaction -to SGD\nconfirm " + id1 + "\nexit\n";
        runParserWithInput(input);

        Transaction updated = list.getTransactionById(id1);
        Transaction unchanged = list.getTransactionById(id2);

        assertEquals("SGD", updated.getCurrency());
        assertEquals(converter.convert(5.0, "USD", "SGD"), updated.getPostings().get(0).getAmount(), 0.0001);

        assertEquals("USD", unchanged.getCurrency());
        assertEquals(10.0, unchanged.getPostings().get(0).getAmount(), 0.0001);

        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("confirm all"));
        assertTrue(output.contains("confirm ID"));
        assertTrue(output.contains("Transaction " + id1 + " confirmed and stored"));
    }

    @Test
    public void testListTransactionToThenConfirmAllStoresAllTransactions() {
        Transaction t1 = createTransaction("10/10/2023", "Coffee", 5.0, "debit", "USD");
        Transaction t2 = createTransaction("11/10/2023", "Tea", 10.0, "debit", "USD");
        list.addTransaction(t1);
        list.addTransaction(t2);

        outputStreamCaptor.reset();
        String input = "list transaction -to SGD\nconfirm all\nexit\n";
        runParserWithInput(input);

        Transaction updated1 = list.getTransactionById(t1.getId());
        Transaction updated2 = list.getTransactionById(t2.getId());

        assertEquals("SGD", updated1.getCurrency());
        assertEquals("SGD", updated2.getCurrency());
        assertEquals(converter.convert(5.0, "USD", "SGD"), updated1.getPostings().get(0).getAmount(), 0.0001);
        assertEquals(converter.convert(10.0, "USD", "SGD"), updated2.getPostings().get(0).getAmount(), 0.0001);

        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("All 2 transactions have been confirmed and stored in SGD."));
    }

    @Test
    public void testListTransactionToThenOtherCommandIgnoresPendingConversion() {
        Transaction t = createTransaction("10/10/2023", "Coffee", 5.0, "debit", "USD");
        list.addTransaction(t);

        outputStreamCaptor.reset();
        String input = "list transaction -to SGD\nlist\nexit\n";
        runParserWithInput(input);

        Transaction unchanged = list.getTransactionById(t.getId());

        assertEquals("USD", unchanged.getCurrency());
        assertEquals(5.0, unchanged.getPostings().get(0).getAmount(), 0.0001);

        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("The displayed values are view-only by default."));
    }
}
