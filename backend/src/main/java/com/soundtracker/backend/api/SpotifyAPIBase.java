package com.soundtracker.backend.api;

public abstract class SpotifyAPIBase {
    public abstract String searchAlbumByName(String albumName);
    public abstract String getAlbumData(String albumId);
    public abstract String getAlbumTracks(String albumId);
}
