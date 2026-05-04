package oopproject.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

// Fetches live currency rates from an online exchange-rate API
public class ExchangeRateService {

    private static final String API_URL = "https://open.er-api.com/v6/latest/USD";

    // Returns the latest USD to LBP rate from the API
    public double fetchUsdToLbpRate() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Could not connect to exchange rate service.");
        }

        String body = response.body();

        String searchKey = "\"LBP\":";
        int startIndex = body.indexOf(searchKey);

        if (startIndex == -1) {
            throw new IOException("LBP rate was not found in the API response.");
        }

        startIndex += searchKey.length();
        int endIndex = body.indexOf(",", startIndex);

        if (endIndex == -1) {
            endIndex = body.indexOf("}", startIndex);
        }

        String rateText = body.substring(startIndex, endIndex).trim();

        return Double.parseDouble(rateText);
    }
}