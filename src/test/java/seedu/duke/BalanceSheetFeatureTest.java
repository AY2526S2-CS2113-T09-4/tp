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

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the balance sheet feature, including hierarchical filtering,
 * currency display mode, and CSV export.
 */
public class BalanceSheetFeatureTest {
    private static final String TEST_LEDGER_FILE = "data/balance-sheet-test-ledger.txt";
    private static final String TEST_RATES_FILE = "data/balance-sheet-test-rates.json";
    private static final String TEST_BALANCE_SHEET_FILE = "data/balance-sheet.csv";

    private TransactionsList list;
    private CurrencyConverter converter;
    private ExchangeRateStorage exchangeRateStorage;
    private LiveExchangeRateService liveExchangeRateService;

    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    public void setUp() throws Exception {
        deleteFile(TEST_LEDGER_FILE);
        deleteFile(TEST_RATES_FILE);
        deleteFile(TEST_BALANCE_SHEET_FILE);

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
        deleteFile(TEST_BALANCE_SHEET_FILE);
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

    private Transaction createBalanceSheetTransaction() {
        List<Posting> postings = new ArrayList<>();
        postings.add(new Posting("Assets:Bank:DBS", 1000.0));
        postings.add(new Posting("Income:Salary", 1000.0));
        return new Transaction("10/10/2023", "Salary", postings, "USD");
    }

    private Transaction createExpenseTransaction() {
        List<Posting> postings = new ArrayList<>();
        postings.add(new Posting("Expenses:Food", 50.0));
        postings.add(new Posting("Assets:Cash", -50.0));
        return new Transaction("11/10/2023", "Lunch", postings, "USD");
    }

    @Test
    public void testBalanceCommandPrintsSummaryAndExportsCsv() {
        list.addTransaction(createBalanceSheetTransaction());
        list.addTransaction(createExpenseTransaction());

        outputStreamCaptor.reset();
        runParserWithInput("balance\nexit\n");

        String output = outputStreamCaptor.toString();

        assertTrue(output.contains("===== BALANCE SHEET ====="));
        assertTrue(output.contains("ASSETS"));
        assertTrue(output.contains("LIABILITIES"));
        assertTrue(output.contains("EQUITY"));
        assertTrue(output.contains("CHECK"));
        assertTrue(output.contains("Assets:Bank:DBS"));
        assertTrue(output.contains("Income:Salary"));
        assertTrue(output.contains("Expenses:Food"));

        File csvFile = new File(TEST_BALANCE_SHEET_FILE);
        assertTrue(csvFile.exists());
    }

    @Test
    public void testBalanceAccCommandFiltersHierarchy() {
        list.addTransaction(createBalanceSheetTransaction());
        list.addTransaction(createExpenseTransaction());

        outputStreamCaptor.reset();
        runParserWithInput("balance -acc Assets:Bank\nexit\n");

        String output = outputStreamCaptor.toString();

        assertTrue(output.contains("Scope: Assets:Bank"));
        assertTrue(output.contains("Assets:Bank:DBS"));
        assertTrue(output.contains("FILTERED VIEW"));
    }

    @Test
    public void testBalanceToCommandShowsConvertedReportCurrency() {
        list.addTransaction(createBalanceSheetTransaction());

        outputStreamCaptor.reset();
        runParserWithInput("balance -to SGD\nexit\n");

        String output = outputStreamCaptor.toString();

        assertTrue(output.contains("===== BALANCE SHEET ====="));
        assertTrue(output.contains("Report Currency: SGD"));
    }

    @Test
    public void testBalanceAccToCommandSupportsBothAccountAndCurrencyFlags() {
        list.addTransaction(createBalanceSheetTransaction());

        outputStreamCaptor.reset();
        runParserWithInput("balance -acc Assets:Bank -to SGD\nexit\n");

        String output = outputStreamCaptor.toString();

        assertTrue(output.contains("Scope: Assets:Bank"));
        assertTrue(output.contains("Report Currency: SGD"));
        assertTrue(output.contains("Assets:Bank:DBS"));
    }
}
