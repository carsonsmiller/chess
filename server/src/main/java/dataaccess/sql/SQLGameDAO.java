package dataaccess.sql;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.GameDAO;
import model.GameData;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SQLGameDAO implements GameDAO {
    private final Gson gson = new Gson();

    @Override
    public void clear() throws DataAccessException {
        String sql = "Delete from `Game`";
        try (var conn = DatabaseManager.getConnection();
             var statement = conn.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("failed to clear games", ex);
        }
    }

    @Override
    public GameData createGame(String gameName) throws DataAccessException {
        if (gameName == null || gameName.isBlank()) {
            throw new DataAccessException("invalid game name");
        }

        ChessGame game = new ChessGame();
        String sql = "INSERT INTO `Game` (whiteUsername, blackUsername, gameName, game) "
                + "VALUES (NULL, NULL, ?, ?)";

        try (var conn = DatabaseManager.getConnection();
             var statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, gameName);
            statement.setString(2, gson.toJson(game));
            statement.executeUpdate();

            try (var keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return new GameData(keys.getInt(1), null, null, gameName, game);
                }
                throw new DataAccessException("failed to create game: no generated game ID");
            }
        } catch (SQLException ex) {
            throw new DataAccessException("failed to create game", ex);
        }
    }
    
    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        String sql = "SELECT gameID, whiteUsername, blackUsername, gameName, game "
                + "FROM 'Game' WHERE gameID = ?";
        try (var conn = DatabaseManager.getConnection();
             var statement = conn.prepareStatement(sql)) {
            statement.setInt(1, gameID);
            try (var result = statement.executeQuery()) {
                return result.next() ? readGame(result) : null;
            }
        } catch (SQLException | JsonParseException ex) {
            throw new DataAccessException("failed to retrieve game", ex);
        }
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        String sql = "SELECT gameID, whiteUsername, blackUsername, gameName, game "
                + "FROM `Game` ORDER BY gameID";
        List<GameData> games = new ArrayList<>();

        try (var conn = DatabaseManager.getConnection();
             var statement = conn.prepareStatement(sql);
             var result = statement.executeQuery()) {
            while (result.next()) {
                games.add(readGame(result));
            }
            return games;
        } catch (SQLException | JsonParseException ex) {
            throw new DataAccessException("failed to lsit games", ex);
        }
    }

    public void updateGame(GameData game) throws DataAccessException {
        if (game == null || game.gameID() <= 0 || game.gameName() == null || game.game() == null) {
            throw new DataAccessException("invalid game");
        }

        String sql = "UPDATE `Game` SET whiteUsername = ?, blackUsername = ?, "
                + "gameName = ?, game = ? WHERE gameID = ?";
        try (var conn = DatabaseManager.getConnection();
             var statement = conn.prepareStatement(sql)) {
            statement.setString(1, game.whiteUsername());
            statement.setString(2, game.blackUsername());
            statement.setString(3, game.gameName());
            statement.setString(4, gson.toJson(game.game()));
            statement.setInt(5, game.gameID());

            if (statement.executeUpdate() == 0) {
                throw new DataAccessException("game not found");
            }
        } catch (SQLException ex) {
            throw new DataAccessException("failed to update game", ex);
        }
    }

    private GameData readGame(java.sql.ResultSet result) throws SQLException {
        String json = result.getString("game");
        ChessGame chessGame = gson.fromJson(json, ChessGame.class);
        if (chessGame == null) {
            throw new JsonParseException("stored game JSON was empty");
        }

        return new GameData(
                result.getInt("gameID"),
                result.getString("whiteUsername"),
                result.getString("blackUsername"),
                result.getString("gameName"),
                chessGame);
    }
}