package server;

import com.google.gson.JsonSyntaxException;
import io.javalin.*;
import com.google.gson.Gson;
import dataaccess.*;
import model.*;
import service.*;
import java.util.*;
import com.google.gson.JsonParseException;

public class Server {

    private final Javalin javalin;
    private final Gson gson = new Gson();
    private final UserDAO userDAO = new MemoryUserDAO();
    private final GameDAO gameDAO = new MemoryGameDAO();
    private final AuthDAO authDAO = new MemoryAuthDAO();
    private final UserService userService = new UserService(userDAO, authDAO);
    private final GameService gameService = new GameService(gameDAO, userService);
    private final ClearService clearService = new ClearService(userDAO, gameDAO, authDAO);

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));
        javalin.exception(ServiceException.class, (e, ctx) -> error(ctx, e.statusCode(), e.getMessage()));
        javalin.exception(Exception.class, (e, ctx) -> {
            if (e instanceof DataAccessException) error(ctx, 500, "Error: " + safeMessage(e));
            else if (e instanceof JsonParseException || e instanceof IllegalStateException) error(ctx, 400, "Error: bad request");
            else error(ctx, 500, "Error: " + safeMessage(e));
        });
        javalin.delete("/db", ctx -> { clearService.clear(); ctx.status(200).result("{}"); });
        javalin.post("/user", ctx -> {
           UserRequest req = body(ctx.body(), UserRequest.class);
           AuthData result = userService.register(req == null ? null : req.username(), req == null ? null : req.password(), req == null ? null : req.email());
           ctx.status(200).json(result);
        });
        javalin.post("/session", ctx -> {
           UserRequest req = body(ctx.body(), UserRequest.class);
           AuthData result = userService.login(req == null ? null : req.username(), req == null ? null : req.password());
           ctx.status(200).json(result);
        });
        javalin.delete("/session", ctx -> {userService.logout(ctx.header("authorization")); ctx.status(200).result("{}"); });
        javalin.get("/game", ctx -> {
            List<GameData> games = gameService.listGames(ctx.header("authorization"));
            List<GameSummary> summaries = games.stream().map(g -> new GameSummary(g.gameID(), g.whiteUsername(), g.blackUsername(), g.gameName())).toList();
            ctx.status(200).json(new GamesResult(summaries));
        });
        javalin.post("/game", ctx -> {
            CreateGameRequest req = body(ctx.body(), CreateGameRequest.class);
            int id = gameService.createGame(ctx.header("authorization"), req == null ? null : req.gameName());
            ctx.status(200).json(new CreateGameResult(id));
        });
        javalin.put("/game", ctx-> {
            JoinGameRequest req = body(ctx.body(), JoinGameRequest.class);
            if (req == null || req.gameID() == null) throw new ServiceException(400, "Error: bad request");
            gameService.joinGame(ctx.header("authorization"), req.playerColor(), req.gameID());
            ctx.status(200).result("{}");
        });
        // Register your endpoints and exception handlers here.
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }
    public void stop() {
        javalin.stop();
    }
    private <T> T body(String json, Class<T> type) throws ServiceException {
        if (json == null || json.isBlank()) throw new ServiceException(400, "Error: bad request");
        try {return gson.fromJson(json, type); } catch (JsonParseException | IllegalStateException e) {
            throw new ServiceException(400, "Error: bad request");
        }
    }
    private void error(io.javalin.http.Context ctx, int status, String message) {
        ctx.status(status).json(new ErrorResult(message));
    }

    private String safeMessage(Exception e) {
        return e.getMessage() == null ? "internal error" : e.getMessage();
    }
    private record UserRequest(String username, String password, String email) {}
    private record CreateGameRequest(String gameName) {}
    private record JoinGameRequest(String playerColor, Integer gameID) {}
    private record CreateGameResult(int gameID) {}
    private record GameSummary(int gameID, String whiteUsername, String blackUsername, String gameName) {}
    private record GamesResult(List<GameSummary> games) {}
    private record ErrorResult(String message) {}

}
