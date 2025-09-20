package com.soundtracker.backend.api;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Stub Spotify API used in offline profiles (mock, local) to avoid external calls.
 * Returns minimal JSON compatible structures consumed by APIMusicService.
 */
@Component
@Profile({"mock","local"})
public class StubSpotifyAPI extends SpotifyAPIBase {

    @Override
    public String searchAlbumByName(String albumName) {
        return "{\"albums\":{\"items\":[{\"id\":\"offline-album\",\"name\":\"" + albumName + "\",\"images\":[{\"url\":\"\"}],\"total_tracks\":0,\"artists\":[{\"name\":\"Offline Artist\"}],\"external_urls\":{\"spotify\":\"\"}}]}}";
    }

    @Override
    public String getAlbumData(String albumId) {
        return "{\"id\":\"" + albumId + "\",\"name\":\"Offline Album\",\"images\":[{\"url\":\"\"}],\"total_tracks\":0,\"artists\":[{\"name\":\"Offline Artist\"}],\"external_urls\":{\"spotify\":\"\"}}";
    }

    @Override
    public String getAlbumTracks(String albumId) {
        return "{\"items\":[]}";
    }
}
