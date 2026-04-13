package seedu.ledger67;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExchangeRateDataTest {

    @Test
    public void testValidDataReturnsTrue() {
        Map<String, Double> rates = new HashMap<>();
        rates.put("SGD", 1.46);

        ExchangeRateData data = new ExchangeRateData("EUR", "2026-03-25", rates);
        assertTrue(data.isValid());
    }

    @Test
    public void testNullBaseReturnsFalse() {
        Map<String, Double> rates = new HashMap<>();
        rates.put("SGD", 1.46);

        ExchangeRateData data = new ExchangeRateData(null, "2026-03-25", rates);
        assertFalse(data.isValid());
    }

    @Test
    public void testBlankBaseReturnsFalse() {
        Map<String, Double> rates = new HashMap<>();
        rates.put("SGD", 1.46);

        ExchangeRateData data = new ExchangeRateData("   ", "2026-03-25", rates);
        assertFalse(data.isValid());
    }

    @Test
    public void testNullDateReturnsFalse() {
        Map<String, Double> rates = new HashMap<>();
        rates.put("SGD", 1.46);

        ExchangeRateData data = new ExchangeRateData("EUR", null, rates);
        assertFalse(data.isValid());
    }

    @Test
    public void testBlankDateReturnsFalse() {
        Map<String, Double> rates = new HashMap<>();
        rates.put("SGD", 1.46);

        ExchangeRateData data = new ExchangeRateData("EUR", " ", rates);
        assertFalse(data.isValid());
    }

    @Test
    public void testNullRatesReturnsFalse() {
        ExchangeRateData data = new ExchangeRateData("EUR", "2026-03-25", null);
        assertFalse(data.isValid());
    }

    @Test
    public void testEmptyRatesReturnsFalse() {
        ExchangeRateData data = new ExchangeRateData("EUR", "2026-03-25", new HashMap<>());
        assertFalse(data.isValid());
    }
}
