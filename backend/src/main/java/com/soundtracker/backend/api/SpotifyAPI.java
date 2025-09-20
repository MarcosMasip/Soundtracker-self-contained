package com.soundtracker.backend.api;

import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

/**
 * Класс для взаимодействия с Spotify API
 */
@Component
@Profile("live")
public class SpotifyAPI extends SpotifyAPIBase {

    @Value("${spotify.client.secret}")
    String clientSecret;

    @Value("${spotify.client.id}")
    String clientId;

    private static final String TOKEN_URL = "https://accounts.spotify.com/api/token";
    private static final String API_URL = "https://api.spotify.com/v1";
    private final OkHttpClient okHttpClient;
    private String accessToken;


    public SpotifyAPI() {
        okHttpClient = new OkHttpClient();
    }

    /**
     * Получение токена доступа к Spotify API
     *
     * @throws IOException если возникают проблемы при выполнении запроса
     */
    private void fetchAccessToken() throws IOException {
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        String requestBody = "grant_type=client_credentials&client_id=" + clientId + "&client_secret=" + clientSecret;

        RequestBody body = RequestBody.create(requestBody, mediaType);
        Request request = new Request.Builder()
                .url(TOKEN_URL)
                .post(body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        Response response = okHttpClient.newCall(request).execute();
        String responseData = Objects.requireNonNull(response.body()).string();
        this.accessToken = responseData.substring(responseData.indexOf(":\"") + 2, responseData.indexOf("\","));
    }

    /**
     * Получение информации об альбоме по его идентификатору
     *
     * @param albumId идентификатор альбома
     * @return строковое представление JSON найденного альбома
     * @throws IOException если возникают проблемы при чтении ответа от внешнего API
     */
    @Override
    public String getAlbumData(String albumId) {
        if (accessToken == null) {
            try { fetchAccessToken(); } catch (IOException e) { throw new RuntimeException(e); }
        }
        String url = API_URL + "/albums/" + albumId;
        try { return makeRequest(url); } catch (IOException e) { throw new RuntimeException(e); }
    }

    /**
     * Получение списка треков альбома по его идентификатору
     *
     * @param albumId идентификатор альбома
     * @return строковое представление JSON списка треков альбома
     * @throws IOException если возникают проблемы при чтении ответа от внешнего API
     */
    @Override
    public String getAlbumTracks(String albumId) {
        if (accessToken == null) {
            try { fetchAccessToken(); } catch (IOException e) { throw new RuntimeException(e); }
        }
        String url = API_URL + "/albums/" + albumId + "/tracks?market=US&limit=40&offset=0";
        try { return makeRequest(url); } catch (IOException e) { throw new RuntimeException(e); }
    }

    /**
     * Поиск альбома по его названию
     *
     * @param albumName название альбома
     * @return строковое представление JSON найденного альбома
     * @throws IOException если возникают проблемы при чтении ответа от внешнего API
     */
    @Override
    public String searchAlbumByName(String albumName) {
        if (accessToken == null) {
            try { fetchAccessToken(); } catch (IOException e) { throw new RuntimeException(e); }
        }
        String url = API_URL + "/search?q=" + albumName + "&type=album&limit=1&offset=0";
        try { return makeRequest(url); } catch (IOException e) { throw new RuntimeException(e); }
    }

    /**
     * Создает и выполняет HTTP запрос к Spotify API
     *
     * @param url URL для выполнения запроса
     * @return ответ от сервера в формате JSON
     * @throws IOException если возникают проблемы при выполнении запроса
     */
    private String makeRequest(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();
        return Objects.requireNonNull(okHttpClient.newCall(request).execute().body()).string();
    }
}
