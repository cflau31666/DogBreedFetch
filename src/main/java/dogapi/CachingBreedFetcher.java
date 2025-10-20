package dogapi;

import java.util.*;

/**
 * This BreedFetcher caches fetch request results to improve performance and
 * lessen the load on the underlying data source. An implementation of BreedFetcher
 * must be provided. The number of calls to the underlying fetcher are recorded.
 *
 * If a call to getSubBreeds produces a BreedNotFoundException, then it is NOT cached
 * in this implementation. The provided tests check for this behaviour.
 *
 * The cache maps the name of a breed to its list of sub breed names.
 */

public class CachingBreedFetcher implements BreedFetcher {
    private final BreedFetcher fetcher;
    private final Map<String, List<String>> cache = new HashMap<>();
    private int callsMade = 0;

    public CachingBreedFetcher(BreedFetcher fetcher) {
        this.fetcher = fetcher;
    }

    @Override
    public List<String> getSubBreeds(String breed)
            throws BreedFetcher.BreedNotFoundException {

        String key = breed.toLowerCase();

        // Return from cache if present
        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        // Count the attempt BEFORE calling the delegate so exceptions still count
        callsMade++;
        try {
            List<String> result = fetcher.getSubBreeds(breed);
            // Cache a defensive copy
            cache.put(key, new ArrayList<>(result));
            return result;
        } catch (BreedFetcher.BreedNotFoundException e) {
            // Do NOT cache failures; just propagate
            throw e;
        }
    }

    public int getCallsMade() {
        return callsMade;
    }
}