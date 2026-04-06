package seedu.ledger67;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Tag;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance tests for Ledger67 application.
 * Tests performance with large datasets and measures execution time.
 */
@Tag("performance")
class PerformanceTest {
    
    private static final int SMALL_DATASET = 100;
    private static final int MEDIUM_DATASET = 1000;
    private static final int LARGE_DATASET = 10000;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Random RANDOM = new Random(42); // Fixed seed for reproducibility
    
    private List<Transaction> testTransactions;
    
    @BeforeEach
    void setUp() {
        testTransactions = new ArrayList<>();
    }
    
    /**
     * Generates a list of random transactions for testing.
     */
    private List<Transaction> generateTransactions(int count) {
        List<Transaction> transactions = new ArrayList<>();
        String[] currencies = {"SGD", "USD", "EUR"};
        String[] accountRoots = {"Assets", "Liabilities", "Equity", "Income", "Expenses"};
        String[] subcategories = {"Cash", "Bank", "Loan", "Salary", "Food", "Transport"};
        
        for (int i = 0; i < count; i++) {
            // Generate random date within last 365 days
            LocalDate date = LocalDate.now().minusDays(RANDOM.nextInt(365));
            
            // Generate random description
            String description = "Transaction " + i + " - " + 
                (RANDOM.nextBoolean() ? "Purchase" : "Payment");
            
            // Generate random currency
            String currency = currencies[RANDOM.nextInt(currencies.length)];
            
            // Generate 2-4 postings
            List<Posting> postings = new ArrayList<>();
            int numPostings = 2 + RANDOM.nextInt(3); // 2-4 postings
            
            double totalAmount = 0;
            for (int j = 0; j < numPostings - 1; j++) {
                String account = accountRoots[RANDOM.nextInt(accountRoots.length)] + 
                    ":" + subcategories[RANDOM.nextInt(subcategories.length)];
                double amount = (RANDOM.nextDouble() * 1000) - 500; // -500 to 500
                postings.add(new Posting(account, amount));
                totalAmount += amount;
            }
            
            // Add final posting to balance the transaction
            String finalAccount = accountRoots[RANDOM.nextInt(accountRoots.length)] + 
                ":" + subcategories[RANDOM.nextInt(subcategories.length)];
            postings.add(new Posting(finalAccount, -totalAmount));
            
            transactions.add(new Transaction(
                date.format(DATE_FORMATTER),
                description,
                postings,
                currency
            ));
        }
        
        return transactions;
    }
    
    /**
     * Performance test for filtering transactions by date.
     */
    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testFilterByDatePerformance() {
        testTransactions = generateTransactions(LARGE_DATASET);
        
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        
        long startTime = System.nanoTime();
        List<Transaction> filtered = TransactionsList.filterTransactionsByDate(
            testTransactions, startDate, endDate);
        long endTime = System.nanoTime();
        
        long durationMs = (endTime - startTime) / 1_000_000;
        
        System.out.printf("Filtered %d transactions by date in %d ms%n", 
            LARGE_DATASET, durationMs);
        
        // Performance assertion: should complete within 2 seconds for 10k transactions
        assertTrue(durationMs < 2000, 
            "Date filtering took too long: " + durationMs + "ms for " + LARGE_DATASET + " transactions");
        
        // Verify some results
        assertNotNull(filtered);
        assertTrue(filtered.size() <= testTransactions.size());
    }
    
    /**
     * Performance test for filtering transactions by account.
     */
    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testFilterByAccountPerformance() {
        testTransactions = generateTransactions(LARGE_DATASET);
        
        String accountFilter = "Assets";
        
        long startTime = System.nanoTime();
        List<Transaction> filtered = TransactionsList.filterTransactionsByAccount(
            testTransactions, accountFilter);
        long endTime = System.nanoTime();
        
        long durationMs = (endTime - startTime) / 1_000_000;
        
        System.out.printf("Filtered %d transactions by account in %d ms%n", 
            LARGE_DATASET, durationMs);
        
        // Performance assertion
        assertTrue(durationMs < 2000, 
            "Account filtering took too long: " + durationMs + "ms for " + LARGE_DATASET + " transactions");
        
        // Verify results
        assertNotNull(filtered);
    }
    
    /**
     * Performance test for filtering transactions by regex.
     */
    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testFilterByRegexPerformance() {
        testTransactions = generateTransactions(LARGE_DATASET);
        
        String regex = ".*Purchase.*";
        
        long startTime = System.nanoTime();
        List<Transaction> filtered = TransactionsList.filterTransactionsByRegex(
            testTransactions, regex);
        long endTime = System.nanoTime();
        
        long durationMs = (endTime - startTime) / 1_000_000;
        
        System.out.printf("Filtered %d transactions by regex in %d ms%n", 
            LARGE_DATASET, durationMs);
        
        // Performance assertion
        assertTrue(durationMs < 2000, 
            "Regex filtering took too long: " + durationMs + "ms for " + LARGE_DATASET + " transactions");
        
        // Verify results
        assertNotNull(filtered);
    }
    
    /**
     * Performance test for layered filtering (multiple filters).
     */
    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testLayeredFilteringPerformance() {
        testTransactions = generateTransactions(LARGE_DATASET);
        
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        String accountFilter = "Expenses";
        String regex = ".*Food.*";
        
        long startTime = System.nanoTime();
        
        // Apply filters in sequence (simulating layered filtering)
        List<Transaction> filtered = testTransactions;
        filtered = TransactionsList.filterTransactionsByDate(filtered, startDate, endDate);
        filtered = TransactionsList.filterTransactionsByAccount(filtered, accountFilter);
        filtered = TransactionsList.filterTransactionsByRegex(filtered, regex);
        
        long endTime = System.nanoTime();
        
        long durationMs = (endTime - startTime) / 1_000_000;
        
        System.out.printf("Layered filtering of %d transactions in %d ms%n", 
            LARGE_DATASET, durationMs);
        
        // Performance assertion
        assertTrue(durationMs < 3000, 
            "Layered filtering took too long: " + durationMs + "ms for " + LARGE_DATASET + " transactions");
        
        // Verify results
        assertNotNull(filtered);
    }
    
    /**
     * Performance test for adding transactions to TransactionsList.
     */
    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testAddTransactionsPerformance() {
        // Create a mock storage for testing
        Storage mockStorage = new Storage("test_performance.json") {
            @Override
            public List<Transaction> load() {
                return new ArrayList<>();
            }
            
            @Override
            public void save(List<Transaction> transactions) {
                // Do nothing for performance test
            }
        };
        
        TransactionsList transactionsList = new TransactionsList(mockStorage);
        
        List<Transaction> transactionsToAdd = generateTransactions(MEDIUM_DATASET);
        
        long startTime = System.nanoTime();
        
        for (Transaction transaction : transactionsToAdd) {
            transactionsList.addTransaction(transaction);
        }
        
        long endTime = System.nanoTime();
        
        long durationMs = (endTime - startTime) / 1_000_000;
        double avgTimePerTransaction = (double) durationMs / MEDIUM_DATASET;
        
        System.out.printf("Added %d transactions in %d ms (avg: %.3f ms/transaction)%n", 
            MEDIUM_DATASET, durationMs, avgTimePerTransaction);
        
        // Performance assertion
        assertTrue(durationMs < 5000, 
            "Adding transactions took too long: " + durationMs + "ms for " + MEDIUM_DATASET + " transactions");
        
        // Verify all transactions were added
        assertEquals(MEDIUM_DATASET, transactionsList.getTransactions().size());
    }
    
    /**
     * Performance test for balance sheet generation.
     */
    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testBalanceSheetGenerationPerformance() {
        // Create a mock storage for testing
        Storage mockStorage = new Storage("test_balance_sheet.json") {
            @Override
            public List<Transaction> load() {
                return new ArrayList<>();
            }
            
            @Override
            public void save(List<Transaction> transactions) {
                // Do nothing for performance test
            }
        };
        
        TransactionsList transactionsList = new TransactionsList(mockStorage);
        transactionsList.getTransactions().addAll(generateTransactions(MEDIUM_DATASET));
        
        long startTime = System.nanoTime();
        
        // Generate balance sheet
        transactionsList.printBalanceSheet();
        
        long endTime = System.nanoTime();
        
        long durationMs = (endTime - startTime) / 1_000_000;
        
        System.out.printf("Generated balance sheet for %d transactions in %d ms%n", 
            MEDIUM_DATASET, durationMs);
        
        // Performance assertion
        assertTrue(durationMs < 3000, 
            "Balance sheet generation took too long: " + durationMs + "ms for " + MEDIUM_DATASET + " transactions");
    }
    
    /**
     * Performance test for transaction validation.
     */
    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testTransactionValidationPerformance() {
        List<Transaction> transactions = generateTransactions(SMALL_DATASET);
        
        long startTime = System.nanoTime();
        
        for (Transaction transaction : transactions) {
            TransactionValidator.ValidationResult result = 
                TransactionValidator.validateTransaction(transaction);
            assertNotNull(result);
        }
        
        long endTime = System.nanoTime();
        
        long durationMs = (endTime - startTime) / 1_000_000;
        double avgTimePerValidation = (double) durationMs / SMALL_DATASET;
        
        System.out.printf("Validated %d transactions in %d ms (avg: %.3f ms/validation)%n", 
            SMALL_DATASET, durationMs, avgTimePerValidation);
        
        // Performance assertion
        assertTrue(avgTimePerValidation < 10, 
            "Transaction validation too slow: " + avgTimePerValidation + "ms per transaction");
    }
    
    /**
     * Memory usage test for large datasets.
     */
    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testMemoryUsage() {
        // Get initial memory usage
        Runtime runtime = Runtime.getRuntime();
        runtime.gc(); // Suggest garbage collection
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // Create large dataset
        List<Transaction> largeDataset = generateTransactions(LARGE_DATASET);
        
        // Get memory after creating dataset
        runtime.gc();
        long afterCreationMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = afterCreationMemory - initialMemory;
        
        double memoryPerTransaction = (double) memoryUsed / LARGE_DATASET;
        
        System.out.printf("Memory usage for %d transactions: %d bytes (%.1f bytes/transaction)%n", 
            LARGE_DATASET, memoryUsed, memoryPerTransaction);
        
        // Memory assertion: should use less than 1KB per transaction on average
        assertTrue(memoryPerTransaction < 1024, 
            "Memory usage too high: " + memoryPerTransaction + " bytes per transaction");
        
        // Verify we can still work with the dataset
        assertNotNull(largeDataset);
        assertEquals(LARGE_DATASET, largeDataset.size());
    }
    
    /**
     * Stress test: multiple operations on large dataset.
     */
    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    @Tag("stress")
    void testStressOperations() {
        // Create a mock storage for testing
        Storage mockStorage = new Storage("test_stress.json") {
            @Override
            public List<Transaction> load() {
                return new ArrayList<>();
            }
            
            @Override
            public void save(List<Transaction> transactions) {
                // Do nothing for performance test
            }
        };
        
        TransactionsList transactionsList = new TransactionsList(mockStorage);
        List<Transaction> transactions = generateTransactions(LARGE_DATASET);
        
        System.out.println("Starting stress test with " + LARGE_DATASET + " transactions...");
        
        // 1. Add all transactions
        long addStart = System.nanoTime();
        for (Transaction transaction : transactions) {
            transactionsList.addTransaction(transaction);
        }
        long addEnd = System.nanoTime();
        
        // 2. Filter operations
        long filterStart = System.nanoTime();
        List<Transaction> filteredByDate = TransactionsList.filterTransactionsByDate(
            transactionsList.getTransactions(), 
            LocalDate.now().minusDays(7), 
            LocalDate.now()
        );
        List<Transaction> filteredByAccount = TransactionsList.filterTransactionsByAccount(
            filteredByDate, 
            "Assets"
        );
        long filterEnd = System.nanoTime();
        
        // 3. Balance sheet
        long balanceStart = System.nanoTime();
        transactionsList.printBalanceSheet();
        long balanceEnd = System.nanoTime();
        
        // Calculate times
        long addTimeMs = (addEnd - addStart) / 1_000_000;
        long filterTimeMs = (filterEnd - filterStart) / 1_000_000;
        long balanceTimeMs = (balanceEnd - balanceStart) / 1_000_000;
        long totalTimeMs = addTimeMs + filterTimeMs + balanceTimeMs;
        
        System.out.println("Stress test results:");
        System.out.printf("  Add: %d ms%n", addTimeMs);
        System.out.printf("  Filter: %d ms%n", filterTimeMs);
        System.out.printf("  Balance: %d ms%n", balanceTimeMs);
        System.out.printf("  Total: %d ms%n", totalTimeMs);
        
        // Performance assertions
        assertTrue(totalTimeMs < 15000, 
            "Stress test took too long: " + totalTimeMs + "ms");
        assertTrue(addTimeMs < 8000, 
            "Adding transactions too slow: " + addTimeMs + "ms");
        assertTrue(filterTimeMs < 4000, 
            "Filtering too slow: " + filterTimeMs + "ms");
        assertTrue(balanceTimeMs < 3000, 
            "Balance sheet generation too slow: " + balanceTimeMs + "ms");
        
        // Verify data integrity
        assertEquals(LARGE_DATASET, transactionsList.getTransactions().size());
        assertNotNull(filteredByAccount);
    }
}