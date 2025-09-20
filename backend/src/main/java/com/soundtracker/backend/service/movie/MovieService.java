package com.soundtracker.backend.service.movie;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soundtracker.backend.model.movie.Movie;
import com.soundtracker.backend.model.music.Album;
import com.soundtracker.backend.provider.movie.MovieMetadataProvider;
import com.soundtracker.backend.provider.music.MusicMetadataProvider;
import com.soundtracker.backend.repository.movie.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Сервис для работы с кино
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MovieService {

    private final ObjectMapper objectMapper;
    private final MovieRepository movieRepository;
    private final MovieMetadataProvider movieMetadataProvider;
    private final MusicMetadataProvider musicMetadataProvider;

    /**
     * Получение информации о кино из базы данных
     *
     * @param requestPath путь запроса
     * @return объект Movie, содержащий данные о кино
     */
    public Optional<Movie> getMovieInfoFromDatabase(String requestPath) {
        // Expected format: /info?id=<id>
        if (requestPath != null && requestPath.contains("id=")) {
            try {
                Long id = Long.valueOf(requestPath.substring(requestPath.indexOf("id=") + 3));
                return movieRepository.findById(id)
                        .or(() -> movieMetadataProvider.findById(id));
            } catch (NumberFormatException e) {
                log.warn("Invalid id in requestPath: {}", requestPath);
            }
        }
        return Optional.empty();
    }

    /**
     * Получение информации о кино из внешнего API
     *
     * @param id идентификатор кино
     * @return объект Movie, содержащий данные о кино
     */
    public Optional<Movie> getMovieInfoFromApi(Long id) {
        // In mock/local mode providers already represent 'API'; in live mode provider delegates outward.
        return movieMetadataProvider.findById(id)
                .or(() -> movieRepository.findById(id));
    }

    /**
     * Получение кино по URL
     *
     * @param url URL для получения кино
     * @return объект Movie, содержащий данные о кино
     */
    private Optional<Movie> getMovieFromJson(String json) {
        try {
            Movie movie = objectMapper.readValue(json, Movie.class);
            return Optional.of(movie);
        } catch (JsonProcessingException e) {
            return Optional.empty();
        }
    }

    /**
     * Отправка HTTP GET запроса к указанному URL
     *
     * @param url URL для отправки GET запроса
     * @return ответ от сервера
     */
    // HTTP self-calls removed; interactions are now direct through repository/providers.

    /**
     * Обновление информации о кино
     *
     * @param movie информация о кино
     * @return ответ от сервера о результате обновления
     */
    public ResponseEntity<String> updatingMovieResponse(Movie movie) {
        try {
            String movieJson = objectMapper.writeValueAsString(movie);
            return updateMovie(movieJson);
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while updating movie.");
        }
    }

    /**
     * Обновление информации о кино
     *
     * @param movieJson информация о кино в формате JSON
     * @return ответ от сервера о результате обновления
     */
    public ResponseEntity<String> updateMovie(String movieJson) {
        return getMovieFromJson(movieJson)
                .map(movie -> {
                    movieRepository.save(movie);
                    return ResponseEntity.ok("Movie updated.");
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid movie JSON"));
    }

    /**
     * Отправка HTTP PUT запроса с переданным телом запроса к указанному URL
     *
     * @param url         URL для отправки PUT запроса
     * @param requestBody тело запроса
     * @return ответ от сервера
     */
    // Removed HTTP PUT indirection; using direct persistence.

    /**
     * Отправка HTTP запроса
     *
     * @param request запрос
     * @return ответ от сервера
     */
    // Removed OkHttp execution; no longer required for internal operations.

    /**
     * Установка альбома для кино
     *
     * @param id идентификатор кино
     * @return ответ от сервера о результате установки альбома
     */
    public ResponseEntity<String> setAlbum(Long id, String albumName) throws JsonProcessingException {
        Optional<Movie> optionalMovie = movieRepository.findById(id);
        if (optionalMovie.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Movie with id: " + id + " not found.");
        }
        Movie movie = optionalMovie.get();
        Album album = musicMetadataProvider.findByName(albumName).orElse(null);
        if (album == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Album '" + albumName + "' not found in metadata provider.");
        }
        movie.setAlbum(album);
        movieRepository.save(movie);
        return ResponseEntity.ok("Album set.");
    }
}