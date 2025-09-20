package com.soundtracker.backend.provider.music;

import com.soundtracker.backend.model.music.Album;

import java.util.List;
import java.util.Optional;

/**
 * Abstraction for obtaining music (album) metadata from external APIs or offline fixtures.
 */
public interface MusicMetadataProvider {

    Optional<Album> findByName(String name);

    List<Album> findAll();
}
