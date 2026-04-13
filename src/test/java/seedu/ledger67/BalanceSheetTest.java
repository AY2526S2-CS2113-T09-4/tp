package seedu.ledger67;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class BalanceSheetTest {

    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @AfterEach
    public void tearDown() {
        System.setOut(originalOut);
        File file = new File("data/balance-sheet-unit-test.csv");
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    public void testPrintAllAccountsShowsPassFailLine() {
        Map<String, Double> totals = new HashMap<>();
        totals.put("Assets:Cash", 100.0);
        totals.put("Income:Salary", 100.0);

        BalanceSheet balanceSheet = new BalanceSheet(totals, "ALL ACCOUNTS", "USD", false);

        System.setOut(new PrintStream(outputStreamCaptor));
        balanceSheet.print();

        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("===== BALANCE SHEET ====="));
        assertTrue(output.contains("Equation Status"));
    }

    @Test
    public void testPrintFilteredViewShowsFilteredMessage() {
        Map<String, Double> totals = new HashMap<>();
        totals.put("Assets:Cash", 100.0);

        BalanceSheet balanceSheet = new BalanceSheet(totals, "Assets", "USD", false);

        System.setOut(new PrintStream(outputStreamCaptor));
        balanceSheet.print();

        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("FILTERED VIEW"));
    }

    @Test
    public void testExportToCsvCreatesFileAndEscapesValues() throws Exception {
        Map<String, Double> totals = new HashMap<>();
        totals.put("Assets:Cash", 100.0);
        totals.put("Income:Salary", 100.0);

        BalanceSheet balanceSheet = new BalanceSheet(totals, "ALL,ACCOUNTS", "USD", true);
        String filePath = "data/balance-sheet-unit-test.csv";

        balanceSheet.exportToCsv(filePath);

        File file = new File(filePath);
        assertTrue(file.exists());

        String content = Files.readString(file.toPath());
        assertTrue(content.contains("Balance Sheet"));
        assertTrue(content.contains("\"ALL,ACCOUNTS\""));
        assertTrue(content.contains("Report Currency,USD"));
    }
}
