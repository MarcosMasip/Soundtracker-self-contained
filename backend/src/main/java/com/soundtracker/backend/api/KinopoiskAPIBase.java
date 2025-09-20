package com.soundtracker.backend.api;

/**
 * Base abstraction for Kinopoisk API to support swapping real and stub implementations via profiles.
 */
public abstract class KinopoiskAPIBase {
    public abstract String searchMovieById(Long id);
    public abstract String searchMovieByTitle(String title);
    public abstract String searchScreenshotsByMovieId(Long id);
    public abstract String searchFrameByMovieId(Long id);
}
