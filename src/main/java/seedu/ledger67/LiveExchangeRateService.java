package seedu.ledger67;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.StringJoiner;

/**
 * Fetches live exchange rates from a public API.
 */
public class LiveExchangeRateService {
    private static final String API_URL = "https://api.frankfurter.dev/v1/latest";
    private final HttpClient client;
    private final Gson gson;

    public LiveExchangeRateService() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.gson = new Gson();
    }

    public ExchangeRateData fetchLatest(String baseCurrency, List<String> symbols) {
        StringJoiner joiner = new StringJoiner(",");
        for (String symbol : symbols) {
            joiner.add(symbol);
        }

        String url = API_URL
                + "?base=" + URLEncoder.encode(baseCurrency, StandardCharsets.UTF_8)
                + "&symbols=" + URLEncoder.encode(joiner.toString(), StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(8))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Live exchange-rate request failed.");
            }

            ExchangeRateData data = gson.fromJson(response.body(), ExchangeRateData.class);

            if (data == null || !data.isValid()) {
                throw new RuntimeException("Live exchange-rate response is invalid.");
            }

            return data;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Unable to fetch live exchange rates.");
        }
    }
}
