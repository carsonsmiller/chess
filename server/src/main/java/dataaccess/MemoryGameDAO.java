package dataaccess;

import chess.ChessGame;
import model.GameData;
import java.util.*;

public class MemoryGameDAO implements GameDAO {
    private final Map<Integer, GameData> games = new LinkedHashMap<>();
    private int nextID = 1;
    @Override public synchronized void clear() {
        games.clear();
        nextID = 1;
    }
    @Override public synchronized GameData createGame(String gameName) throws DataAccessException {
        if (gameName == null || gameName.isBlank()) throw new DataAccessException("Invalid game name");
        GameData game = new GameData(nextID++, null, null, gameName, new ChessGame());
        games.put(game.gameID(),game);
        return game;
    }
    @Override public synchronized GameData getGame(int gameID) { return games.get(gameID);}
    @Override public synchronized List<GameData> listGames() {return new ArrayList<>(games.values());}
    @Override public synchronized void updateGame(GameData game) throws DataAccessException {
        if (game == null || !games.containsKey(game.gameID())) throw new DataAccessException("Game not found");
        games.put(game.gameID(), game);
    }
}