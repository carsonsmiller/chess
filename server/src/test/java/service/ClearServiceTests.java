package service;

import dataaccess.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClearServiceTests {
    @Test void clearRemovesStoredData() throws Exception {
        MemoryUserDAO users = new MemoryUserDAO();
        MemoryGameDAO games = new MemoryGameDAO();
        MemoryAuthDAO auths = new MemoryAuthDAO();

        users.createUser(new model.UserData("big ben", "pw", "b@b.com"));
        games.createGame("match");
        auths.createAuth(new model.AuthData("token", "ben"));
        new ClearService(users,games,auths).clear();
        assertNull(users.getUser("big ben"));
        assertTrue(games.listGames().isEmpty());
        assertNull(auths.getAuth("token"));
    }
}

