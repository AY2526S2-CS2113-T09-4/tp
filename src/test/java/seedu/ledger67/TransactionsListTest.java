package seedu.ledger67;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests adding, listing, editing, deleting, and clearing functions of the
 * TransactionsList class.
 */
public class TransactionsListTest {
    private static final String TEST_FILE_PATH = "data/transactions-list-test.txt";

    private TransactionsList list;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private PrintStream originalOut;

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

    @BeforeEach
    public void setUp() {
        deleteFile(TEST_FILE_PATH);

        list = new TransactionsList(new Storage(TEST_FILE_PATH));

        Map<String, Double> rates = new HashMap<>();
        rates.put("SGD", 1.46);
        rates.put("USD", 1.09);
        ExchangeRateData rateData = new ExchangeRateData("EUR", "2026-03-19", rates);
        CurrencyConverter converter = new CurrencyConverter(rateData);
        list.setCurrencyConverter(converter);

        outputStreamCaptor.reset();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    public void tearDown() {
        System.setOut(originalOut);
        deleteFile(TEST_FILE_PATH);
    }

    private void deleteFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    public void testAddAndListTransactions() {
        Transaction t = createTransaction("15/03/2023", "Salary", 5000.0, "credit", "USD");
        list.addTransaction(t);

        outputStreamCaptor.reset();
        list.listTransactions();

        String output = outputStreamCaptor.toString().trim();
        Assertions.assertTrue(output.contains("Salary"));
        Assertions.assertTrue(output.contains("5000.00"));
    }

    @Test
    public void testDeleteTransactionSuccess() {
        Transaction t = createTransaction("15/03/2023", "Salary", 5000.0, "credit", "USD");
        list.addTransaction(t);
        int id = t.getId();

        Assertions.assertDoesNotThrow(() -> list.deleteTransaction(id));

        outputStreamCaptor.reset();
        list.listTransactions();
        Assertions.assertTrue(outputStreamCaptor.toString().contains("No transactions found."));
    }

    @Test
    public void testDeleteTransactionNotFound() {
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            list.deleteTransaction(999);
        });
        Assertions.assertEquals("Transaction ID not found.", exception.getMessage());
    }

    @Test
    public void testEditTransactionSuccess() {
        Transaction t = createTransaction("15/03/2023", "Rent", 1000.0, "debit", "USD");
        list.addTransaction(t);
        int id = t.getId();

        List<Posting> newPostings = new ArrayList<>();
        newPostings.add(new Posting("Expenses:General", 1200.0));
        newPostings.add(new Posting("Assets:Cash", -1200.0));
        list.editTransaction(id, null, null, newPostings, null);

        outputStreamCaptor.reset();
        list.listTransactions();

        Assertions.assertTrue(outputStreamCaptor.toString().contains("1200.00"));
    }

    @Test
    public void testClearTransactions() {
        list.addTransaction(createTransaction("15/03/2023", "A", 10.0, "debit", "USD"));
        list.addTransaction(createTransaction("16/03/2023", "B", 20.0, "debit", "USD"));

        outputStreamCaptor.reset();
        list.clearTransactions();
        list.listTransactions();

        String output = outputStreamCaptor.toString();
        Assertions.assertTrue(output.contains("All transactions have been cleared."));
        Assertions.assertTrue(output.contains("No transactions found."));
    }

    @Test
    public void testListTransactionsByAccountFiltering() {
        Transaction t1 = createTransaction("10/10/2023", "Lunch", 10.0, "debit", "USD");
        Transaction t2 = createTransaction("11/10/2023", "Salary", 1000.0, "credit", "USD");

        List<Posting> postings1 = new ArrayList<>();
        postings1.add(new Posting("Expenses:Food", 10.0));
        postings1.add(new Posting("Assets:Cash", -10.0));

        List<Posting> postings2 = new ArrayList<>();
        postings2.add(new Posting("Assets:Bank:DBS", 1000.0));
        postings2.add(new Posting("Income:Salary", 1000.0));

        t1.update(null, null, postings1, null);
        t2.update(null, null, postings2, null);

        list.addTransaction(t1);
        list.addTransaction(t2);

        outputStreamCaptor.reset();
        list.listTransactionsByAccount("Assets");

        String output = outputStreamCaptor.toString();

        Assertions.assertTrue(output.contains("Assets:Cash"));
        Assertions.assertTrue(output.contains("Assets:Bank:DBS"));

        outputStreamCaptor.reset();
        list.listTransactionsByAccount("Assets:Bank");

        output = outputStreamCaptor.toString();

        Assertions.assertTrue(output.contains("Assets:Bank:DBS"));
        Assertions.assertFalse(output.contains("Assets:Cash"));
    }

    @Test
    public void testPrintBalanceSheetByAccount() {
        List<Posting> postings = new ArrayList<>();
        postings.add(new Posting("Assets:Bank:DBS", 1000.0));
        postings.add(new Posting("Income:Salary", 1000.0));

        Transaction t = new Transaction("10/10/2023", "Salary", postings, "USD");
        list.addTransaction(t);

        outputStreamCaptor.reset();
        list.printBalanceSheet("Assets:Bank");

        String output = outputStreamCaptor.toString();
        Assertions.assertTrue(output.contains("Scope: Assets:Bank"));
        Assertions.assertTrue(output.contains("Assets:Bank:DBS"));
    }

    @Test
    public void testGetAccountBalanceIncludesChildren() {
        List<Posting> postings = new ArrayList<>();
        postings.add(new Posting("Assets:Bank:DBS", 100.0));
        postings.add(new Posting("Income:Salary", 100.0));
        list.addTransaction(new Transaction("01/01/2026", "Salary", postings, "USD"));

        Assertions.assertEquals(100.0, list.getAccountBalance("Assets"), 0.0001);
        Assertions.assertEquals(100.0, list.getAccountBalance("Assets:Bank"), 0.0001);
        Assertions.assertEquals(100.0, list.getAccountBalance("Assets:Bank:DBS"), 0.0001);
    }

    @Test
    public void testFilterTransactionsByAccount_nullReturnsOriginal() {
        List<Transaction> txns = new ArrayList<>();
        txns.add(createTransaction("01/01/2026", "A", 10.0, "debit", "USD"));

        List<Transaction> result = TransactionsList.filterTransactionsByAccount(txns, null);
        Assertions.assertEquals(1, result.size());
    }

    @Test
    public void testFilterTransactionsByDateInclusiveBounds() {
        List<Transaction> txns = new ArrayList<>();
        txns.add(createTransaction("01/01/2026", "A", 10.0, "debit", "USD"));
        txns.add(createTransaction("05/01/2026", "B", 10.0, "debit", "USD"));
        txns.add(createTransaction("10/01/2026", "C", 10.0, "debit", "USD"));

        List<Transaction> result = TransactionsList.filterTransactionsByDate(
                txns, LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 10));

        Assertions.assertEquals(2, result.size());
    }

    @Test
    public void testFilterTransactionsByRegex_caseInsensitive() {
        List<Transaction> txns = new ArrayList<>();
        txns.add(createTransaction("01/01/2026", "Lunch", 10.0, "debit", "USD"));
        txns.add(createTransaction("02/01/2026", "DINNER", 10.0, "debit", "USD"));

        List<Transaction> result = TransactionsList.filterTransactionsByRegex(txns, "dinner");
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("DINNER", result.get(0).getDescription());
    }

    @Test
    public void testFilterTransactionsByRegex_invalidPatternThrows() {
        List<Transaction> txns = new ArrayList<>();
        txns.add(createTransaction("01/01/2026", "Lunch", 10.0, "debit", "USD"));

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> TransactionsList.filterTransactionsByRegex(txns, "["));
    }

    @Test
    public void testDisplayCurrencySetAndGet() {
        list.setDisplayCurrency("usd");
        Assertions.assertEquals("USD", list.getDisplayCurrency());
    }

    @Test
    public void testAddUnbalancedTransactionThrows() {
        List<Posting> postings = new ArrayList<>();
        postings.add(new Posting("Assets:Cash", 10.0));
        postings.add(new Posting("Expenses:Food", 10.0));

        Transaction bad = new Transaction("01/01/2026", "Bad", postings, "USD");

        Assertions.assertThrows(IllegalArgumentException.class, () -> list.addTransaction(bad));
    }
}
