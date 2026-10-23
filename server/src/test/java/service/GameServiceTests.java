package service;

import dataaccess.*;
import model.AuthData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class GameServiceTests {
    private MemoryUserDAO users;
    private MemoryAuthDAO auths;
    private MemoryGameDAO games;

    private UserService userService;
    private GameService service;
    private String token;

    @BeforeEach void setup() throws Exception {
        users = new MemoryUserDAO();
        auths = new MemoryAuthDAO();
        games = new MemoryGameDAO();
        userService = new UserService(users,auths);
        service = new GameService(games, userService);
        token = userService.register("bigger ben", "pw", "big@b.com").authToken();
    }

    @Test void invalidRequestFail() throws Exception {
        assertEquals(401, assertThrows(ServiceException.class,()->service.listGames("bad-token")).statusCode());
        assertEquals(400, assertThrows(ServiceException.class, ()->service.createGame(token, "")).statusCode());

        int id=service.createGame(token, "match");
        service.joinGame(token,"WHITE",id);

        assertEquals(403, assertThrows(ServiceException.class,()->service.joinGame(token,"WHITE",id)).statusCode());
        assertEquals(400, assertThrows(ServiceException.class,()->service.joinGame(token,"Rook",id)).statusCode());
        assertEquals(400, assertThrows(ServiceException.class,()->service.joinGame(token, "BLACK",999)).statusCode());
        assertEquals(401, assertThrows(ServiceException.class,()->service.joinGame("bad-token","BLACK",id)).statusCode());
    }

    @Test void seatHandling() throws Exception {
        int id=service.createGame(token,"another match");
        service.joinGame(token,"BLACK",id);
        assertEquals("bigger ben", games.getGame(id).blackUsername());
        assertEquals(403, assertThrows(ServiceException.class,()->service.joinGame(token,"BLACK",id)).statusCode());
    }
}