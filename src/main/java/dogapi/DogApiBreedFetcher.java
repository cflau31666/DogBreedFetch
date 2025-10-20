package dogapi;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DogApiBreedFetcher implements BreedFetcher {
    private final OkHttpClient client = new OkHttpClient();

    @Override
    public List<String> getSubBreeds(String breed) throws BreedFetcher.BreedNotFoundException {
        if (breed == null || breed.isBlank()) {
            throw new BreedFetcher.BreedNotFoundException("Breed name cannot be empty.");
        }

        final String url = "https://dog.ceo/api/breed/" + breed + "/list";
        final int maxAttempts = 3; // small retry to survive transient network issues
        IOException lastIo = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                Request request = new Request.Builder().url(url).build();
                try (Response response = client.newCall(request).execute()) {
                    if (response.body() == null) {
                        throw new IOException("Empty response body");
                    }

                    String body = response.body().string();
                    JSONObject json = new JSONObject(body);
                    String status = json.optString("status", "");

                    if ("error".equals(status)) {
                        // API says the breed doesn't exist
                        throw new BreedFetcher.BreedNotFoundException("Breed not found: " + breed);
                    }

                    JSONArray msg = json.getJSONArray("message");
                    List<String> result = new ArrayList<>(msg.length());
                    for (int i = 0; i < msg.length(); i++) {
                        result.add(msg.getString(i));
                    }
                    return result; // success
                }
            } catch (IOException io) {
                lastIo = io;
                try {
                    Thread.sleep(150L);
                } catch (InterruptedException ignored) {
                }
            }
        }

        // after retries, map to the required exception type
        throw new BreedFetcher.BreedNotFoundException(
                "Failed to fetch data for '" + breed + "': " + (lastIo != null ? lastIo.getMessage() : "unknown I/O error"));
    }
}