package seedu.ledger67;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class Ledger67 {
    private static final Logger logger = Logger.getLogger(Ledger67.class.getName());
    
    /**
     * Main entry-point for the Ledger67 application.
     */
    public static void main(String[] args) {
        // Initialize enhanced logging
        LoggingConfig.setup();
        
        logger.info(() -> String.format("Ledger67 application starting - timestamp: %s, args: %s",
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            Arrays.toString(args)));
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


        Storage storage = new Storage(Config.TRANSACTIONS_FILE_PATH);
        TransactionsList transactionList = new TransactionsList(storage);

        ExchangeRateStorage exchangeRateStorage = new ExchangeRateStorage(Config.EXCHANGE_RATES_FILE_PATH);
        LiveExchangeRateService liveExchangeRateService = new LiveExchangeRateService();

        ExchangeRateData rateData;
        boolean usingFallbackRates = false;

        try {
            rateData = exchangeRateStorage.load();
            System.out.println("Exchange rates loaded successfully from local storage dated "
                    + rateData.getDate() + ".");
        } catch (RuntimeException e) {
            rateData = createFallbackRateData();
            usingFallbackRates = true;
            System.out.println("Warning: Unable to load exchange rates from file.");
            System.out.println("Using fallback exchange rates instead. Conversions may be outdated.");
            System.out.println("Base currency: " + rateData.getBase());

            for (Map.Entry<String, Double> entry : rateData.getRates().entrySet()) {
                System.out.println("  " + rateData.getBase() + " -> " + entry.getKey() + " = " + entry.getValue());
            }

            System.out.println("Run 'rates refresh' to fetch the latest live exchange rates.");
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

        return new ExchangeRateData("EUR", "fallback-static", rates);
    }
}
