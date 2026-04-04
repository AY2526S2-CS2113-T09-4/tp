package seedu.ledger67;

import java.util.Map;

/**
 * Converts between currencies using base-relative exchange rates.
 */
public class CurrencyConverter {
    private ExchangeRateData rateData;

    public CurrencyConverter(ExchangeRateData rateData) {
        updateRates(rateData);
    }

    public void updateRates(ExchangeRateData rateData) {
        if (rateData == null || !rateData.isValid()) {
            throw new IllegalArgumentException("Exchange-rate data is invalid.");
        }
        this.rateData = rateData;
    }

    public String getBaseCurrency() {
        return rateData.getBase();
    }

    public String getRateDate() {
        return rateData.getDate();
    }

    public double convert(double amount, String fromCurrency, String toCurrency) {
        String from = CurrencyValidator.validateAndGet(fromCurrency);
        String to = CurrencyValidator.validateAndGet(toCurrency);

        if (from.equals(to)) {
            return amount;
        }

        double fromRate = getRate(from);
        double toRate = getRate(to);

        double amountInBase = amount / fromRate;
        return amountInBase * toRate;
    }

    private double getRate(String currency) {
        if (currency.equals(rateData.getBase())) {
            return 1.0;
        }

        Map<String, Double> rates = rateData.getRates();
        Double rate = rates.get(currency);

        if (rate == null) {
            throw new IllegalArgumentException("Unsupported currency.");
        }

        return rate;
    }
}
