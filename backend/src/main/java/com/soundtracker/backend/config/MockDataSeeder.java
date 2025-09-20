package com.soundtracker.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soundtracker.backend.model.movie.Movie;
import com.soundtracker.backend.model.music.Album;
import com.soundtracker.backend.provider.movie.MovieMetadataProvider;
import com.soundtracker.backend.provider.music.MusicMetadataProvider;
import com.soundtracker.backend.repository.movie.MovieRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds the database with mock data when running in mock/local profile and the database is empty.
 */
@Profile({"mock", "local"})
@Component
@RequiredArgsConstructor
@Slf4j
public class MockDataSeeder implements CommandLineRunner {

    private final MovieRepository movieRepository;
    private final MovieMetadataProvider movieMetadataProvider;
    private final MusicMetadataProvider musicMetadataProvider;
    private final ObjectMapper objectMapper; // reserved for future complex merging

    @Override
    @Transactional
    public void run(String... args) {
        long count = movieRepository.count();
        if (count > 0) {
            log.info("Skipping mock data seeding: movies table already has {} records", count);
            return;
        }
        log.info("Seeding mock dataset (movies + albums)...");

        List<Movie> movies = movieMetadataProvider.findAll();
        if (movies.isEmpty()) {
            log.warn("No movie fixtures found on classpath under data/movies/*.json. Add fixtures to enable offline dataset.");
            return;
        }
        // Persist movies (cascade handles related entities)
        for (Movie movie : movies) {
            // Attempt to enrich with album if present in music provider under matching enTitle or ruTitle
            musicMetadataProvider.findAll().stream()
                    .filter(a -> a.getMovie() == null)
                    .filter(a -> a.getName() != null && (
                            a.getName().equalsIgnoreCase(movie.getEnTitle()) ||
                                    a.getName().equalsIgnoreCase(movie.getRuTitle())))
                    .findFirst()
                    .ifPresent(movie::setAlbum);
            movieRepository.save(movie);
        }
        log.info("Seeded {} movies.", movies.size());

        // Optionally log album coverage
        long albumsLinked = movies.stream().filter(m -> m.getAlbum() != null).count();
        log.info("Linked albums to {} movies.", albumsLinked);
    }
}
