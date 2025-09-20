package com.soundtracker.backend.api;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Offline stub for Kinopoisk API used in mock/local profiles.
 * Provides minimal JSON structures to satisfy parsing logic without external calls.
 */
@Component
@Profile({"mock","local"})
public class StubKinopoiskAPI extends KinopoiskAPIBase {

    private static final String MOVIE_TEMPLATE = "{\n" +
            "  \"id\": %d,\n" +
            "  \"name\": \"Offline Movie %d\",\n" +
            "  \"alternativeName\": \"\",\n" +
            "  \"year\": 0,\n" +
            "  \"description\": \"Offline stub movie\",\n" +
            "  \"movieLength\": 0,\n" +
            "  \"poster\": { \"url\": \"\" },\n" +
            "  \"genres\": [],\n" +
            "  \"persons\": [],\n" +
            "  \"type\": \"offline\"\n" +
            "}";

    private static final String WRAPPED_DOCS_TEMPLATE = "{\"docs\":[%s]}";

    @Override
    public String searchMovieById(Long id) {
        return String.format(MOVIE_TEMPLATE, id, id);
    }

    @Override
    public String searchMovieByTitle(String title) {
        // Provide one synthetic doc so APIMovieService can parse index 0 safely.
        String movie = String.format(MOVIE_TEMPLATE, 0, 0).replace("Offline Movie 0", "Offline: " + title);
        return String.format(WRAPPED_DOCS_TEMPLATE, movie);
    }

    @Override
    public String searchScreenshotsByMovieId(Long id) {
        return "{\"docs\":[]}"; // no screenshots offline
    }

    @Override
    public String searchFrameByMovieId(Long id) {
        return "{\"docs\":[]}";
    }
}
package com.soundtracker.backend.api;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Stub implementation used in offline profiles (mock, local) to avoid external HTTP calls.
 * Returns minimal JSON structures expected by downstream parsing logic.
 */
@Component
@Profile({"mock","local"})
public class StubKinopoiskAPI extends KinopoiskAPIBase {

    @Override
    public String searchMovieById(Long id) {
        // Return empty structure with docs array consistent with expected schema branches.
        return "{\"id\": " + id + ", \"name\": \"Offline Stub Movie\", \"alternativeName\": \"Offline Stub\", \"year\": 1970, \"description\": \"Stub description offline\", \"movieLength\": 0, \"poster\": { \"url\": \"\" }, \"genres\": [], \"persons\": [], \"type\": \"movie\"}";
    }

    @Override
    public String searchMovieByTitle(String title) {
        return "{\"docs\":[{\"id\":999999,\"name\":\"" + title + "\",\"alternativeName\":\"\",\"year\":1970,\"description\":\"Stub description offline\",\"movieLength\":0,\"poster\":{\"url\":\"\"},\"genres\":[],\"persons\":[],\"type\":\"movie\"}]}";
    }

    @Override
    public String searchScreenshotsByMovieId(Long id) {
        return "{\"docs\":[]}";
    }

    @Override
    public String searchFrameByMovieId(Long id) {
        return "{\"docs\":[]}";
    }
}
