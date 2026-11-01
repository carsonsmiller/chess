package dataaccess.sql;

import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;

import java.sql.SQLException;
import java.sql.Statement;

public class SQLDatabase {

    public static void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();

        try (var conn = DatabaseManager.getConnection();
             var statement = conn.createStatement()) {

            createUserTable(statement);
            createAuthTable(statement);
            createGameTable(statement);
        } catch (SQLException ex) {
            throw new DataAccessException("failed to configure database", ex);
        }
    }

    private static void createUserTable(Statement statement) throws SQLException {
        var sql = """
                CREATE TABLE IF NOT EXISTS `User` (
                    username VARCHAR(256) NOT NULL,
                    password VARCHAR(256) NOT NULL,
                    email VARCHAR(256) NOT NULL,
                    PRIMARY KEY (username)
                ) ENGINE=InnoDB
                """;
        statement.executeUpdate(sql);
    }

    private static void createAuthTable(Statement statement) throws SQLException {
        var sql = """
                CREATE TABLE IF NOT EXISTS `Auth` (
                    authToken VARCHAR(256) NOT NULL,
                    username VARCHAR(256) NOT NULL,
                    PRIMARY KEY (authToken),
                    FOREIGN KEY (username)
                        REFERENCES `User`(username)
                        ON DELETE CASCADE
                ) ENGINE=InnoDB
                """;
        statement.executeUpdate(sql);
    }

    private static void createGameTable(Statement statement) throws SQLException {
        var sql = """
                CREATE TABLE IF NOT EXISTS `Game` (
                    gameID INT NOT NULL AUTO_INCREMENT,
                    whiteUsername VARCHAR(256),
                    blackUsername VARCHAR(256),
                    gameName VARCHAR(256) NOT NULL,
                    PRIMARY KEY (gameID),
                    FOREIGN KEY (whiteUsername)
                        REFERENCES `User`(username)
                        ON DELETE SET NULL,
                    FOREIGN KEY (blackUsername)
                        REFERENCES `User`(username)
                        ON DELETE SET NULL
                ) ENGINE=InnoDB
                """;
        statement.executeUpdate(sql);
    }
}