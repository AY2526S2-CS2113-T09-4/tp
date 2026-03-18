package seedu.duke;

import java.util.HashMap;
import java.util.Map;

public class Duke {
    /**
     * Main entry-point for the Duke application.
     */
    public static void main(String[] args) {
        String logo = " ____        _        \n"
                + "|  _ \\ _   _| | _____ \n"
                + "| | | | | | | |/ / _ \\\n"
                + "| |_| | |_| |   <  __/\n"
                + "|____/ \\__,_|_|\\_\\___|\n";

        System.out.println("Hello from\n" + logo);

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
