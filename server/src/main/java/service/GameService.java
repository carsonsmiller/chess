package service;

import dataaccess.*;
import model.*;
import java.util.List;

public class GameService {
    private final GameDAO games; private final UserService users;
    public GameService(GameDAO games, UserService users) {this.games=games; this.users=users;}
    public List<GameData> listGames(String token) throws DataAccessException, ServiceException { users.requireAuth(token); return games.listGames();}
    public int createGame(String token, String gameName) throws DataAccessException, ServiceException {
        users.requireAuth(token); if (gameName == null || gameName.isBlank()) {
            throw new ServiceException(400, "Error: bad request");
        }
        return games.createGame(gameName).gameID();
    }
    public void joinGame(String token, String color, int gameID) throws DataAccessException, ServiceException {
        AuthData auth=users.requireAuth(token);
        if (color == null || !(color.equals("WHITE") || color.equals("BLACK")) || gameID <= 0) {
            throw new ServiceException(400, "Error: bad request");
        }
        GameData game = games.getGame(gameID);

        if (game == null) {
            throw new ServiceException(400, "Error: bad request");
        }

        if (color.equals("WHITE")) {
            if (game.whiteUsername() != null) {
                throw new ServiceException(403, "Error: already taken");
            }
            games.updateGame(new GameData(
                    game.gameID(),
                    auth.username(),
                    game.blackUsername(),
                    game.gameName(),
                    game.game()
            ));
        } else {
            if (game.blackUsername() != null) {
                throw new ServiceException(403, "Error: already taken");
            }
            games.updateGame(new GameData(game.gameID(),
                    game.whiteUsername(),
                    auth.username(),
                    game.gameName(),
                    game.game()
            ));
        }
    }
}