package com.soundtracker.backend.provider.movie;

import com.soundtracker.backend.model.movie.Movie;

import java.util.List;
import java.util.Optional;

/**
 * Abstraction for obtaining movie metadata from either an external API or offline fixtures.
 */
public interface MovieMetadataProvider {

    Optional<Movie> findById(Long id);

    List<Movie> findAll();
}
