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
    public List<String> getSubBreeds(String breed)
            throws BreedFetcher.BreedNotFoundException {

        if (breed == null || breed.isBlank()) {
            throw new BreedFetcher.BreedNotFoundException("Breed name cannot be empty.");
        }

        final String url = "https://dog.ceo/api/breed/" + breed + "/list";

        try {
            Request request = new Request.Builder().url(url).build();

            // try-with-resources so Response is closed
            try (Response response = client.newCall(request).execute()) {
                if (response.body() == null) {
                    throw new IOException("Empty response body");
                }

                String body = response.body().string();
                JSONObject json = new JSONObject(body);
                String status = json.optString("status", "");

                if ("error".equals(status)) {
                    // API’s 404 case: breed doesn’t exist
                    throw new BreedFetcher.BreedNotFoundException("Breed not found: " + breed);
                }

                // success: pull the sub-breed array
                JSONArray message = json.getJSONArray("message");
                List<String> result = new ArrayList<>(message.length());
                for (int i = 0; i < message.length(); i++) {
                    result.add(message.getString(i));
                }
                return result;
            }
        } catch (IOException e) {
            // Map any I/O/parse problem to the required exception
            throw new BreedFetcher.BreedNotFoundException(
                    "Failed to fetch data for '" + breed + "': " + e.getMessage());
        }
    }
}