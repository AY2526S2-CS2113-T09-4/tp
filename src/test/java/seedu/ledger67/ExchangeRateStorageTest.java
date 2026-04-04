package seedu.ledger67;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
