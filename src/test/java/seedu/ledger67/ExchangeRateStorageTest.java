package seedu.ledger67;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests loading exchange rates from a JSON file.
 */
public class ExchangeRateStorageTest {
    private static final String TEST_FILE_PATH = "data/test-rates.json";

    @AfterEach
    public void tearDown() {
        File file = new File(TEST_FILE_PATH);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    public void testLoadRatesFromJson() throws Exception {
        File file = new File(TEST_FILE_PATH);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (FileWriter writer = new FileWriter(TEST_FILE_PATH)) {
            writer.write("{\"base\":\"EUR\",\"date\":\"2026-03-19\",\"rates\":{\"SGD\":1.46,\"USD\":1.09}}");
        }

        ExchangeRateStorage storage = new ExchangeRateStorage(TEST_FILE_PATH);
        ExchangeRateData data = storage.load();

        assertEquals("EUR", data.getBase());
        assertEquals("2026-03-19", data.getDate());
        assertEquals(1.46, data.getRates().get("SGD"));
        assertEquals(1.09, data.getRates().get("USD"));
    }

    @Test
    public void testLoadMissingFileThrows() {
        ExchangeRateStorage storage = new ExchangeRateStorage(TEST_FILE_PATH);

        assertThrows(RuntimeException.class, storage::load);
    }

    @Test
    public void testLoadInvalidJsonThrows() throws Exception {
        File file = new File(TEST_FILE_PATH);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (FileWriter writer = new FileWriter(TEST_FILE_PATH)) {
            writer.write("{invalid json}");
        }

        ExchangeRateStorage storage = new ExchangeRateStorage(TEST_FILE_PATH);
        assertThrows(RuntimeException.class, storage::load);
    }

    @Test
    public void testSaveAndLoadRoundTrip() {
        ExchangeRateStorage storage = new ExchangeRateStorage(TEST_FILE_PATH);

        Map<String, Double> rates = new HashMap<>();
        rates.put("SGD", 1.46);
        rates.put("USD", 1.09);
        ExchangeRateData data = new ExchangeRateData("EUR", "2026-03-30", rates);

        storage.save(data);
        ExchangeRateData loaded = storage.load();

        assertEquals("EUR", loaded.getBase());
        assertEquals("2026-03-30", loaded.getDate());
        assertEquals(1.46, loaded.getRates().get("SGD"));
        assertEquals(1.09, loaded.getRates().get("USD"));
    }

    @Test
    public void testSaveInvalidDataThrows() {
        ExchangeRateStorage storage = new ExchangeRateStorage(TEST_FILE_PATH);

        assertThrows(IllegalArgumentException.class, () -> storage.save(null));
        assertThrows(IllegalArgumentException.class,
                () -> storage.save(new ExchangeRateData("EUR", "", new HashMap<>())));
    }
}
