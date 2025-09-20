package com.soundtracker.backend.provider.music;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soundtracker.backend.model.music.Album;
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
 * Loads album metadata from classpath JSON fixtures when running in mock/offline mode.
 */
@Profile({"mock", "local"})
@Component
public class MockMusicMetadataProvider implements MusicMetadataProvider {

    private final ConcurrentMap<String, Album> cache = new ConcurrentHashMap<>();

    public MockMusicMetadataProvider(ObjectMapper objectMapper) throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:data/music/albums/*.json");
        for (Resource r : resources) {
            Album album = objectMapper.readValue(r.getInputStream(), Album.class);
            if (album.getName() != null) {
                cache.put(album.getName().toLowerCase(), album);
            }
        }
    }

    @Override
    public Optional<Album> findByName(String name) {
        if (name == null) return Optional.empty();
        return Optional.ofNullable(cache.get(name.toLowerCase()));
    }

    @Override
    public List<Album> findAll() {
        return new ArrayList<>(cache.values());
    }
}
