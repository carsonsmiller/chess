package dataaccess.sql;

import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.UserDAO;
import model.UserData;
import java.sql.SQLException;

public class SQLUserDAO implements UserDAO {

    @Override
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM `User`";
        try (var conn = DatabaseManager.getConnection();
             var statement = conn.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException ex) {
                throw new DataAccessException("failed to clear users", ex);
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        if (user == null || blank(user.username()) || blank(user.password()) || blank(user.email())) {
            throw new DataAccessException("invalid user");
        }

        String sql = "INSERT INTO `User` (username, password, email) VALUES (?, ?, ?)";

        try (var conn = DatabaseManager.getConnection();
             var statement = conn.prepareStatement(sql)) {
            statement.setString(1, user.username());
            statement.setString(2, user.password());
            statement.setString(3, user.email());
            statement.executeUpdate(); //I'm tired of this grandpa
        } catch (SQLException ex) {
            throw new DataAccessException("failed to create user", ex);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        if (username == null) {
            return null;
        }

        String sql = "SELECT username, password, email FROM `User` WHERE username = ?";
        try (var conn = DatabaseManager.getConnection();
             var statement = conn.prepareStatement(sql)) {
            statement.setString(1, username);
            try (var result = statement.executeQuery()) {
                if (result.next()) {
                    return new UserData(
                            result.getString("username"),
                            result.getString("password"),
                            result.getString("email"));
                }
                return null;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("failed to retrieve user", ex);
        }
    }

    private boolean blank(String value) {return value == null || value.isBlank();}
}