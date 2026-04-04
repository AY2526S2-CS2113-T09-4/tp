package seedu.ledger67;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Handles loading and saving exchange-rate JSON locally.
 */
public class ExchangeRateStorage {
    private final String filePath;
    private final Gson gson;

    public ExchangeRateStorage(String filePath) {
        this.filePath = filePath;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public ExchangeRateData load() {
        File file = new File(filePath);

        if (!file.exists()) {
            throw new RuntimeException("Exchange rates file not found.");
        }

        try (FileReader reader = new FileReader(file)) {
            ExchangeRateData data = gson.fromJson(reader, ExchangeRateData.class);
            if (data == null || !data.isValid()) {
                throw new RuntimeException("Exchange rates file is invalid.");
            }
            return data;
        } catch (IOException e) {
            throw new RuntimeException("Unable to load exchange rates from file.");
        }
    }

    public void save(ExchangeRateData data) {
        if (data == null || !data.isValid()) {
            throw new IllegalArgumentException("Exchange-rate data is invalid.");
        }

        File file = new File(filePath);
        File parent = file.getParentFile();

        try {
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(data, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to save exchange rates to file.");
        }
    }
}
