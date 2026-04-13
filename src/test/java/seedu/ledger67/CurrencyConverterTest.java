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

    private ExchangeRateData createRateData() {
        Map<String, Double> rates = new HashMap<>();
        rates.put("SGD", 1.46);
        rates.put("USD", 1.09);
        return new ExchangeRateData("EUR", "2026-03-19", rates);
    }

    @Test
    public void testConversionSuccess() {
        CurrencyConverter converter = new CurrencyConverter(createRateData());

        double result = converter.convert(100, "USD", "SGD");

        double expected = (100.0 / 1.09) * 1.46;
        assertEquals(expected, result, 0.0001);
    }

    @Test
    public void testSameCurrencyConversion() {
        CurrencyConverter converter = new CurrencyConverter(createRateData());

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

    @Test
    public void testGetBaseCurrencyAndRateDate() {
        CurrencyConverter converter = new CurrencyConverter(createRateData());

        assertEquals("EUR", converter.getBaseCurrency());
        assertEquals("2026-03-19", converter.getRateDate());
    }

    @Test
    public void testConvert_fromBaseCurrency() {
        CurrencyConverter converter = new CurrencyConverter(createRateData());

        double result = converter.convert(100, "EUR", "SGD");

        assertEquals(146.0, result, 0.0001);
    }

    @Test
    public void testConvert_toBaseCurrency() {
        CurrencyConverter converter = new CurrencyConverter(createRateData());

        double result = converter.convert(109, "USD", "EUR");

        assertEquals(100.0, result, 0.0001);
    }

    @Test
    public void testUpdateRates_invalidDataThrows() {
        CurrencyConverter converter = new CurrencyConverter(createRateData());

        assertThrows(IllegalArgumentException.class, () -> converter.updateRates(null));
        assertThrows(IllegalArgumentException.class,
                () -> converter.updateRates(new ExchangeRateData("EUR", "", new HashMap<>())));
    }
}
