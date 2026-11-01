package dataaccess.sql;

import chess.ChessGame;
import dataaccess.DataAccessException;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SQLGameDAOTest {
    private SQLGameDAO games;
    private SQLUserDAO users;

    @BeforeEach
    void setUp() throws Exception {
        SQLDatabase.configureDatabase();
        games = new SQLGameDAO();
        users = new SQLUserDAO();
        users.clear();
        games.clear();
        users.createUser(new UserData("ben", "password123", "ben@ex.com"));
    }

    @Test
    void createAndRetrieveGamePersistsInitialChessGame() throws Exception {
        GameData created = games.createGame("Test Game");
        GameData retrieved = games.getGame(created.gameID());

        assertNotNull(retrieved);
        assertEquals(created.gameID(), retrieved.gameID());
        assertEquals("Test Game", retrieved.gameName());
        assertNull(retrieved.whiteUsername());
        assertNull(retrieved.blackUsername());
        assertEquals(new ChessGame(), retrieved.game());
    }

    @Test
    void listGamesReturnsAllGamesInIdOrder() throws Exception {
        GameData first = games.createGame("First");
        GameData second = games.createGame("Second");

        var result = games.listGames();
        assertEquals(2, result.size());
        assertEquals(first.gameID(), result.get(0).gameID());
        assertEquals(second.gameID(), result.get(1).gameID());
    }

    @Test
    void missingGameReturnsNull() throws Exception {
        assertNull(games.getGame(-1));
    }

    @Test
    void invalidGameNameFails() {
        assertThrows(DataAccessException.class,
                () -> games.createGame(""));
        assertThrows(DataAccessException.class,
                () -> games.createGame(null));
    }

    @Test
    void updateMissingGameFails() {
        GameData missing = new GameData(111111, null, null, "Missing", new ChessGame());
        assertThrows(DataAccessException.class,
                () -> games.updateGame(missing));
    }

    @Test
    void updateWithUnknownPlayerFails() throws Exception {
        GameData created = games.createGame("Test Game");
        GameData invalid = new GameData(created.gameID(), "not-a-user", null,
                created.gameName(), created.game());
        assertThrows(DataAccessException.class,
                () -> games.updateGame(invalid));
    }

    @Test
    void clearRemovesGames() throws Exception {
        GameData created = games.createGame("Test Game");
        games.clear();
        assertNull(games.getGame(created.gameID()));
        assertTrue(games.listGames().isEmpty());
    }
}