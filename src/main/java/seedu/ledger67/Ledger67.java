package seedu.ledger67;

import java.util.HashMap;
import java.util.Map;

public class Ledger67 {
    /**
     * Main entry-point for the Duke application.
     */
    public static void main(String[] args) {
        String rawLogo = """
                 /$$                       /$$                                /$$$$$$  /$$$$$$$$
                | $$                      | $$                               /$$__  $$|_____ $$/
                | $$        /$$$$$$   /$$$$$$$  /$$$$$$   /$$$$$$   /$$$$$$ | $$  %S%/     /$$/
                | $$       /$$__  $$ /$$__  $$ /$$__  $$ /$$__  $$ /$$__  $$| $$$$$$$     /$$/
                | $$      | $$$$$$$$| $$  | $$| $$  %S% $$| $$$$$$$$| $$  %S%/| $$__  $$   /$$/
                | $$      | $$_____/| $$  | $$| $$  | $$| $$_____/| $$      | $$  %S% $$  /$$/
                | $$$$$$$$|  $$$$$$$|  $$$$$$$|  $$$$$$$|  $$$$$$$| $$      |  $$$$$$/ /$$/
                |________/ %S%_______/ %S%_______/ %S%____  $$ %S%_______/|__/       %S%______/ |__/
                                               /$$  %S% $$
                                              |  $$$$$$/
                                               %S%______/
                        """;
        String logo = rawLogo.replace("%S%", "\\");

        System.out.println("Hello from\n" + logo);
        System.out.println("Ledger67 is a double-entry book-keeping CLI" +
            " program that ensures your accounts are BALANCED!");
        System.out.println("Balanced means that the LEFT side = RIGHT side of this equation:");
        System.out.println("Assets = Equity - Liabilities + (Income - Expenses)");
        
        System.out.println("Valid Root Accounts: assets, liabilities, equity, income, expenses");
        System.out.println("Example: Assets:Cash or Income:Job");


        Storage storage = new Storage("data/ledger.txt");
        TransactionsList transactionList = new TransactionsList(storage);

        ExchangeRateStorage exchangeRateStorage = new ExchangeRateStorage("data/exchange-rates.json");
        LiveExchangeRateService liveExchangeRateService = new LiveExchangeRateService();

        ExchangeRateData rateData;
        try {
            rateData = exchangeRateStorage.load();
        } catch (RuntimeException e) {
            rateData = createFallbackRateData();
        }

        CurrencyConverter converter = new CurrencyConverter(rateData);
        transactionList.setCurrencyConverter(converter);

        Parser parser = new Parser(transactionList, converter, exchangeRateStorage, liveExchangeRateService);
        parser.start();

        System.out.println("--- Transaction Manager Exited ---");
    }

    private static ExchangeRateData createFallbackRateData() {
        Map<String, Double> rates = new HashMap<>();
        rates.put("SGD", 1.46);
        rates.put("USD", 1.09);

        return new ExchangeRateData("EUR", "fallback", rates);
    }
}
