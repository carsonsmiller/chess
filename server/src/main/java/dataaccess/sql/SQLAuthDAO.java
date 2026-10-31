package dataaccess.sql;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import model.AuthData;

import java.sql.SQLException;

public class SQLAuthDAO implements AuthDAO {

    @Override
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM 'Auth'";
        try (var conn = DatabaseManager.getConnection();
             var statement = conn.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("failed to clear auth data", ex);
        }
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        if (auth == null || blank(auth.authToken()) || blank(auth.username())) {
            throw new DataAccessException("invalid auth data");
        }

        String sql = "INSERT INTO 'Auth' (authToken, username) VALUES (?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var statement = conn.prepareStatement(sql)) {
            statement.setString(1, auth.authToken());
            statement.setString(2, auth.username());
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("failed to create auth data", ex);
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        if (authToken == null) {
            return null;
        }

        String sql = "SELECT authToken, username FROM 'Auth' WHERE authToken = ?";
        try (var conn = DatabaseManager.getConnection();
             var statement = conn.prepareStatement(sql)) {
            statement.setString(1, authToken);
            try (var result = statement.executeQuery()) {
                if (result.next()) {
                    return new AuthData(result.getString("authToken"), result.getString("username"));
                }
                return null;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("failed to retrieve auth data", ex);
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        if (authToken == null) {
            throw new DataAccessException("auth token not found");
        }

        String sql = "DELETE FROM 'Auth' WHERE authToken = ?";
        try (var conn = DatabaseManager.getConnection();
             var statement = conn.prepareStatement(sql)) {
            statement.setString(1, authToken);
            if (statement.executeUpdate() == 0) {
                throw new DataAccessException("auth token not found");
            }
        } catch (SQLException ex) {
            throw new DataAccessException("failed to delete auth data", ex);
        }
    }

    private boolean blank(String value) {
        return (value == null || value.isBlank());
    }
}