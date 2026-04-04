package seedu.ledger67;

import org.junit.jupiter.api.Test;


import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests currency conversion behaviour.
 */
public class CurrencyConverterTest {

    @Test
    public void testConversionSuccess() {
        Map<String, Double> rates = new HashMap<>();
        rates.put("SGD", 1.46);
        rates.put("USD", 1.09);

        ExchangeRateData rateData = new ExchangeRateData("EUR", "2026-03-19", rates);
        CurrencyConverter converter = new CurrencyConverter(rateData);

        double result = converter.convert(100, "USD", "SGD");

        double expected = (100.0 / 1.09) * 1.46;
        assertEquals(expected, result, 0.0001);
    }

    @Test
    public void testSameCurrencyConversion() {
        Map<String, Double> rates = new HashMap<>();
        rates.put("SGD", 1.46);
        rates.put("USD", 1.09);

        ExchangeRateData rateData = new ExchangeRateData("EUR", "2026-03-19", rates);
        CurrencyConverter converter = new CurrencyConverter(rateData);

        double result = converter.convert(100, "USD", "USD");

        assertEquals(100.0, result, 0.0001);
    }

    @Test
    public void testInvalidCurrency() {
        Map<String, Double> rates = new HashMap<>();
        rates.put("SGD", 1.46);

        ExchangeRateData rateData = new ExchangeRateData("EUR", "2026-03-19", rates);
        CurrencyConverter converter = new CurrencyConverter(rateData);

        assertThrows(IllegalArgumentException.class, () ->
                converter.convert(100, "USD", "SGD"));
    }
}
