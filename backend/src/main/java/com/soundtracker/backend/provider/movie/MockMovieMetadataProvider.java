package com.soundtracker.backend.provider.movie;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soundtracker.backend.model.movie.Movie;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Loads movie metadata from classpath JSON fixtures when running in mock/offline mode.
 */
@Profile({"mock", "local"})
@Component
public class MockMovieMetadataProvider implements MovieMetadataProvider {

    private final ConcurrentMap<Long, Movie> cache = new ConcurrentHashMap<>();

    public MockMovieMetadataProvider(ObjectMapper objectMapper) throws IOException {
        // Eagerly load all fixtures under /data/movies/*.json (excluding index if present)
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:data/movies/*.json");
        for (Resource r : resources) {
            if (!r.getFilename().equalsIgnoreCase("index.json")) {
                Movie m = objectMapper.readValue(r.getInputStream(), Movie.class);
                if (m.getId() != null) {
                    cache.put(m.getId(), m);
                }
            }
        }
    }

    @Override
    public Optional<Movie> findById(Long id) {
        return Optional.ofNullable(cache.get(id));
    }

    @Override
    public List<Movie> findAll() {
        return new ArrayList<>(cache.values());
    }
}
