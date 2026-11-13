package client;

import org.junit.jupiter.api.*;
import server.Server;
import java.util.List;
import java.util.UUID;
import java.net.ServerSocket;

import model.AuthData;




public class ServerFacadeTests {

    private static Server server;
    private static int port;
    private ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    @BeforeEach
    void clearDatabase() throws ServerFacadeException {
        facade = new ServerFacade(port);
        facade.clearDatabase();
    }

    @Test
    void constructorAcceptsDynamicPort() {Assertions.assertNotNull(facade); }

    @Test
    void constructorRejectsInvalidPort() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new ServerFacade(0));
    }

    @Test
    void requestToUnavailableServerFailsWithFriendlyException() throws Exception {
        int unusedPort;
        try (ServerSocket socket = new ServerSocket(0)) {
            unusedPort = socket.getLocalPort();
        }
        ServerFacade unavailable = new ServerFacade(unusedPort);
        ServerFacadeException error = Assertions.assertThrows(ServerFacadeException.class,
                () -> unavailable.listGames("some-token"));
        Assertions.assertEquals(0, error.getStatusCode());
    }

    @Test
    void registerCreatesAccountAndReturnsAuthData() throws Exception {
        AuthData auth = facade.register(unique("player"), "password", "player@email.com");
        Assertions.assertNotNull(auth.authToken());
        Assertions.assertFalse(auth.authToken().isBlank());
    }

    @Test
    void registerRejectsDuplicateUsername() throws Exception {
        String username = unique("duplicate");
        facade.register(username, "password", "first@email.com");
        ServerFacadeException error = Assertions.assertThrows(ServerFacadeException.class,
                () -> facade.register(username, "password", "second@email.com"));
        Assertions.assertEquals(403, error.getStatusCode());
    }

    @Test
    void registerRejectsBlankFields() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> facade.register(" ", "password", "email@email.com"));
    }

    @Test
    void loginAcceptsValidCredentials() throws Exception {
        String username = unique("login");
        facade.register(username, "password", "login@email.com");
        AuthData auth = facade.login(username, "password");
        Assertions.assertEquals(username, auth.username());
    }

    @Test
    void loginRejectsBlankUsername() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> facade.login("", "password"));
    }

    @Test
    void loginRejectsIncorrectPassword() throws Exception {
        String username = unique("wrongpass");
        facade.register(username, "password", "wrong@email.com");
        ServerFacadeException error = Assertions.assertThrows(ServerFacadeException.class,
                () -> facade.login(username, "incorrect"));
        Assertions.assertEquals(401, error.getStatusCode());
    }

    @Test
    void logoutInvalidatesSession() throws Exception {
        AuthData auth = registerUser();
        facade.logout(auth.authToken());
        ServerFacadeException error = Assertions.assertThrows(ServerFacadeException.class,
                () -> facade.listGames(auth.authToken()));
        Assertions.assertEquals(401, error.getStatusCode());
    }

    @Test
    void logoutRejectsUnknownToken() throws Exception {
        ServerFacadeException error = Assertions.assertThrows(ServerFacadeException.class,
                () -> facade.logout("not-a-valid-token"));
        Assertions.assertEquals(401, error.getStatusCode());
    }

    @Test
    void logoutRejectsBlankToken() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> facade.logout(" "));
    }

    @Test
    void listGamesReturnsEmptyListforNewUser() throws Exception {
        AuthData auth = registerUser();
        List<ServerFacade.GameInfo> games = facade.listGames(auth.authToken());
        Assertions.assertTrue(games.isEmpty());
    }

    @Test
    void listGamesReturnsCreatedGames() throws Exception {
        AuthData auth = registerUser();
        String gameName = unique("friendly");
        int id = facade.createGame(auth.authToken(), gameName);
        List<ServerFacade.GameInfo> games = facade.listGames(auth.authToken());
        Assertions.assertEquals(1, games.size());
        Assertions.assertEquals(id, games.get(0).gameID());
        Assertions.assertEquals(gameName, games.get(0).gameName());
    }

    @Test
    void listGamesRejectsInvalidToken() {
        ServerFacadeException error = Assertions.assertThrows(ServerFacadeException.class,
                () -> facade.listGames("invalid-token"));
        Assertions.assertEquals(401, error.getStatusCode());
    }

    @Test
    void listGamesRejectsBlankToken() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> facade.listGames(" "));
    }

    @Test
    void createGameRejectsBlankName() throws Exception {
        AuthData auth = registerUser();
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> facade.createGame(auth.authToken(), " "));
    }

    @Test
    void createGameRejectsInvalidToken() {
        ServerFacadeException error = Assertions.assertThrows(ServerFacadeException.class,
                () -> facade.createGame("invalid-token", "New game"));
        Assertions.assertEquals(401, error.getStatusCode());
    }

    @Test
    void joinGameAssignsPlayerToWhite() throws Exception {
        AuthData auth = registerUser();
        int gameID = facade.createGame(auth.authToken(), "White test");
        facade.joinGame(auth.authToken(), "WHITE", gameID);
        Assertions.assertEquals(auth.username(), facade.listGames(auth.authToken()).get(0).whiteUsername());

    }

    @Test
    void joinGameAssignsPlayerToBlack() throws Exception {
        AuthData auth = registerUser();
        int gameID = facade.createGame(auth.authToken(), "Black test");
        facade.joinGame(auth.authToken(), "BLACK", gameID);
        Assertions.assertEquals(auth.username(), facade.listGames(auth.authToken()).get(0).blackUsername());

    }

    @Test
    void joinGameRejectsInvalidColor() throws Exception {
        AuthData auth = registerUser();
        int gameID = facade.createGame(auth.authToken(), "Red test");
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> facade.joinGame(auth.authToken(), "red", gameID));
    }

    @Test
    void joinGameRejectsUnknownGame() throws Exception {
        AuthData auth = registerUser();
        ServerFacadeException error = Assertions.assertThrows(ServerFacadeException.class,
                () -> facade.joinGame(auth.authToken(), "WHITE", 99999));
        Assertions.assertEquals(400, error.getStatusCode());
    }

    @Test
    void joinGameRejectsInvalidToken() {
        ServerFacadeException error = Assertions.assertThrows(ServerFacadeException.class,
                () -> facade.joinGame("invalid token", "WHITE", 1));
        Assertions.assertEquals(401, error.getStatusCode());
    }

    @Test
    void clearDatabaseRemovesUsersAndGames() throws Exception {
        AuthData auth = registerUser();
        facade.createGame(auth.authToken(), "to be removed");
        facade.clearDatabase();
        ServerFacadeException error = Assertions.assertThrows(ServerFacadeException.class,
                () -> facade.login(auth.username(), "password"));
        Assertions.assertEquals(401, error.getStatusCode());
        AuthData freshUser = registerUser();
        Assertions.assertTrue(facade.listGames(freshUser.authToken()).isEmpty());
    }

    private String unique(String prefix) {
        return prefix + UUID.randomUUID().toString().substring(0, 8);
    }

    private AuthData registerUser() throws ServerFacadeException {
        return facade.register("player", "password", "player@email.com");
    }

}
