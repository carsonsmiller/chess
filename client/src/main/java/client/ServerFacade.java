package client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import model.AuthData;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class ServerFacade {
    private final String serverUrl;
    private final HttpClient httpClient;
    private final Gson gson;

    public ServerFacade(int port) {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535");
        }
        serverUrl = "http://localhost:" + port;
        httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        gson = new Gson();
    }

    public AuthData register(String username, String password, String email) throws ServerFacadeException {
        return send("POST", "/user", null, Map.of(
                "username", requireText(username, "Username"),
                "password", requireText(password, "Password"),
                "email", requireText(email, "Email")),
                AuthData.class);
    }

    public AuthData login(String username, String password) throws ServerFacadeException {
        return send("POST", "/session", null, Map.of(
                "username", requireText(username, "Username"),
                "password", requireText(password, "Password")),
                AuthData.class);
    }

    public void logout(String authToken) throws ServerFacadeException {
        send("DELETE", "/session", requireText(authToken, "Authentication"), null, EmptyResult.class);
    }

    public List<GameInfo> listGames(String authToken) throws ServerFacadeException {
        GamesResult result = send("GET", "/game", requireText(authToken, "Authentication"), null,
                GamesResult.class);
        if (result == null || result.games() == null) {
            throw new ServerFacadeException("The server returned an invalid game list.", 200);
        }
        return result.games();
    }

    public void joinGame(String authToken, String playerColor, int gameID) throws ServerFacadeException {
        requireText(authToken, "Authentication");
        if (gameID <= 0) {
            throw new IllegalArgumentException("Game number must be positive");
        }
        if (!"WHITE".equals(playerColor) && !"BLACK".equals(playerColor)) {
            throw new IllegalArgumentException("Choose WHITE or BLACK");
        }
        send("PUT", "/game", authToken, Map.of(
                "playerColor", playerColor,
                "gameID", gameID),
                EmptyResult.class);
    }

    public int createGame(String authToken, String gameName) throws ServerFacadeException {
        CreateGameResult result = send("POST", "/game", requireText(authToken, "Authentication"), Map.of(
                "gameName", requireText(gameName, "Game name")),
                CreateGameResult.class);
        if (result == null || result.gameID() <= 0) {
            throw new ServerFacadeException("The server could not create that game.", 200);
        }
        return result.gameID();
    }

    public void clearDatabase() throws ServerFacadeException {
        send("DELETE", "/db", null, null, EmptyResult.class);
    }

    private String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " cannot be blank");
        }
        return value;
    }

    public record GameInfo(int gameID, String whiteUsername, String blackUsername, String gameName) { }
    private record GamesResult(List<GameInfo> games) { }
    private record CreateGameResult(int gameID) { }
    private static final class EmptyResult { }


    //this send will suck
    private <T> T send(String method, String path, String authToken, Object body, Class<T> responseType)
        throws ServerFacadeException {
                HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                        .timeout(Duration.ofSeconds(10))
                        .header("Accept", "application/json");
                if (authToken != null) {
                    builder.header("authorization", authToken);
                }
                if (body != null) {
                    builder.header("Content-Type", "application/json");
                    builder.method(method, HttpRequest.BodyPublishers.ofString(gson.toJson(body)));
                } else {
                    builder.method(method, HttpRequest.BodyPublishers.noBody());
                }

                final HttpResponse<String> response;
                try {
                    response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new ServerFacadeException("The request was interrupted. Try again", ex);
                } catch (IOException | IllegalArgumentException ex) {
                    throw new ServerFacadeException("Unable to reach the Chess server. Make sure it is running.", ex);
                }

                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw new ServerFacadeException(errorMessage(response), response.statusCode());
                }
                if (responseType == null || responseType == EmptyResult.class || response.body() == null
                || response.body().isBlank()) {
                    return null;
                }
                try {
                    return gson.fromJson(response.body(), responseType);
                } catch (JsonParseException ex) {
                    throw new ServerFacadeException("The server returned an unexpected response.", ex);
                }
    }

    private String errorMessage(HttpResponse<String> response) {
        try {
            JsonObject error = gson.fromJson(response.body(), JsonObject.class);
            if (error != null && error.has("message")
                && !error.get("message").isJsonNull()) {
                return error.get("message").getAsString();
            }
        } catch (JsonParseException ignored) {

        }
        return "Server request failed with status " + response.statusCode();
    }
 }
