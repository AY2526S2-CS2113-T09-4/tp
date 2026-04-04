package seedu.ledger67;

import java.util.Map;

/**
 * Represents exchange-rate data loaded from local JSON or fetched from a live API.
 */
public class ExchangeRateData {
    private String base;
    private String date;
    private Map<String, Double> rates;

    public ExchangeRateData() {
    }

    public ExchangeRateData(String base, String date, Map<String, Double> rates) {
        this.base = base;
        this.date = date;
        this.rates = rates;
    }

    public String getBase() {
        return base;
    }

    public String getDate() {
        return date;
    }

    public Map<String, Double> getRates() {
        return rates;
    }

    public boolean isValid() {
        return base != null && !base.isBlank()
                && date != null && !date.isBlank()
                && rates != null && !rates.isEmpty();
    }
}
