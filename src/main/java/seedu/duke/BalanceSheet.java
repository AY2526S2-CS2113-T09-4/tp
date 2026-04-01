package seedu.duke;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * Builds and prints a balance sheet summary.
 * Also exports to CSV so it can be opened in Excel without external libraries.
 */
public class BalanceSheet {

    private final Map<String, Double> accountTotals;
    private final String scope;
    private final String reportCurrency;
    private final boolean converted;

    public BalanceSheet(Map<String, Double> accountTotals,
                        String scope,
                        String reportCurrency,
                        boolean converted) {
        this.accountTotals = new TreeMap<>(accountTotals);
        this.scope = scope;
        this.reportCurrency = reportCurrency;
        this.converted = converted;
    }

    public void print() {
        double totalAssets = getRootTotal("Assets");
        double totalLiabilities = getRootTotal("Liabilities");
        double baseEquity = getRootTotal("Equity");
        double totalIncome = getRootTotal("Income");
        double totalExpenses = getRootTotal("Expenses");

        double netIncome = totalIncome - totalExpenses;
        double totalEquity = baseEquity + netIncome;

        System.out.println("===== BALANCE SHEET =====");
        System.out.println("Scope: " + scope);
        System.out.println("Report Currency: " + (converted ? reportCurrency : "ORIGINAL TRANSACTION CURRENCIES"));
        System.out.println();

        System.out.println("ASSETS");
        printAccountsUnder("Assets");
        System.out.printf("  Total Assets                                %10.2f%n", totalAssets);
        System.out.println();

        System.out.println("LIABILITIES");
        printAccountsUnder("Liabilities");
        System.out.printf("  Total Liabilities                           %10.2f%n", totalLiabilities);
        System.out.println();

        System.out.println("EQUITY");
        printAccountsUnder("Equity");

        System.out.println("  Retained Earnings Breakdown");
        printAccountsUnder("Income", true, false);
        printAccountsUnder("Expenses", true, true);

        System.out.printf("  Current Period Net Income                   %10.2f%n", netIncome);
        System.out.printf("  Total Equity                                %10.2f%n", totalEquity);
        System.out.println();

        System.out.println("CHECK");
        System.out.printf("  Total Assets                                %10.2f%n", totalAssets);
        System.out.printf("  Liabilities + Total Equity                  %10.2f%n", totalLiabilities + totalEquity);

        if (scope.equalsIgnoreCase("ALL ACCOUNTS")) {
            boolean balanced = Math.abs(totalAssets - (totalLiabilities + totalEquity)) < 0.0001;
            System.out.println("  Equation Status                             " + (balanced ? "PASS" : "FAIL"));
        } else {
            System.out.println("  Equation Status                             FILTERED VIEW (may not balance)");
        }

        System.out.println("=========================");
    }

    public void exportToCsv(String filePath) {
        double totalAssets = getRootTotal("Assets");
        double totalLiabilities = getRootTotal("Liabilities");
        double baseEquity = getRootTotal("Equity");
        double totalIncome = getRootTotal("Income");
        double totalExpenses = getRootTotal("Expenses");

        double netIncome = totalIncome - totalExpenses;
        double totalEquity = baseEquity + netIncome;

        File file = new File(filePath);
        File parent = file.getParentFile();

        try {
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write("Balance Sheet");
                writer.newLine();
                writer.write("Scope," + escapeCsv(scope));
                writer.newLine();
                writer.write("Report Currency," + escapeCsv(converted ? reportCurrency :
                        "ORIGINAL TRANSACTION CURRENCIES"));
                writer.newLine();
                writer.newLine();

                writer.write("Section,Account,Amount");
                writer.newLine();

                writeSection(writer, "Assets", "Assets");
                writer.write("Assets,Total Assets," + format(totalAssets));
                writer.newLine();

                writeSection(writer, "Liabilities", "Liabilities");
                writer.write("Liabilities,Total Liabilities," + format(totalLiabilities));
                writer.newLine();

                writeSection(writer, "Equity", "Equity");
                writeSection(writer, "Retained Earnings (Income)", "Income");
                writeSectionNegative(writer, "Retained Earnings (Expenses)", "Expenses");

                writer.write("Equity,Current Period Net Income," + format(netIncome));
                writer.newLine();
                writer.write("Equity,Total Equity," + format(totalEquity));
                writer.newLine();
                writer.newLine();

                writer.write("Check,Total Assets," + format(totalAssets));
                writer.newLine();
                writer.write("Check,Liabilities + Total Equity," + format(totalLiabilities + totalEquity));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to export balance sheet CSV.");
        }
    }

    private void printAccountsUnder(String root) {
        printAccountsUnder(root, false, false);
    }

    private void printAccountsUnder(String root, boolean indent, boolean negate) {
        String prefix = root + ":";
        for (Map.Entry<String, Double> entry : accountTotals.entrySet()) {
            String account = entry.getKey();
            if (account.startsWith(prefix)) {
                double amount = negate ? -entry.getValue() : entry.getValue();
                System.out.printf("%s%-45s %10.2f%n",
                        indent ? "    " : "  ",
                        account,
                        amount);
            }
        }
    }

    private void writeSection(BufferedWriter writer, String section, String root) throws IOException {
        String prefix = root + ":";
        for (Map.Entry<String, Double> entry : accountTotals.entrySet()) {
            String account = entry.getKey();
            if (account.startsWith(prefix)) {
                writer.write(escapeCsv(section) + "," + escapeCsv(account) + "," + format(entry.getValue()));
                writer.newLine();
            }
        }
    }

    private void writeSectionNegative(BufferedWriter writer, String section, String root) throws IOException {
        String prefix = root + ":";
        for (Map.Entry<String, Double> entry : accountTotals.entrySet()) {
            String account = entry.getKey();
            if (account.startsWith(prefix)) {
                writer.write(escapeCsv(section) + "," + escapeCsv(account) + "," + format(-entry.getValue()));
                writer.newLine();
            }
        }
    }

    private double getRootTotal(String root) {
        double total = 0;
        String prefix = root + ":";
        for (Map.Entry<String, Double> entry : accountTotals.entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                total += entry.getValue();
            }
        }
        return total;
    }

    private String format(double value) {
        return String.format("%.2f", value);
    }

    private String escapeCsv(String text) {
        String safe = text == null ? "" : text;
        if (safe.contains(",") || safe.contains("\"") || safe.contains("\n")) {
            return "\"" + safe.replace("\"", "\"\"") + "\"";
        }
        return safe;
    }
}
