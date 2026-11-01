package dataaccess.sql;

import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SQLAuthDAOTest {
    private SQLAuthDAO auths;
    private SQLUserDAO users;

    @BeforeEach
    void setUp() throws Exception {
        SQLDatabase.configureDatabase();
        auths = new SQLAuthDAO();
        users = new SQLUserDAO();
        users.clear();
        auths.clear();
        users.createUser(new UserData("ben", "bigben123", "ben@ex.com"));
    }

    @Test
    void createAndRetrieveAuth() throws Exception {
        AuthData auth = new AuthData("token-1", "ben");
        auths.createAuth(auth);
        assertEquals(auth, auths.getAuth("token-1"));
    }

    @Test
    void missingAuthReturnsNull() throws Exception {
        assertNull(auths.getAuth("missing"));
        assertNull(auths.getAuth(null));
    }

    @Test
    void duplicateTokenFails() throws Exception {
        auths.createAuth(new AuthData("token-1", "ben"));
        assertThrows(DataAccessException.class,
                () -> auths.createAuth(new AuthData("token-1", "ben")));
    }

    @Test
    void unknownUserCantOwnAuth() {
        assertThrows(DataAccessException.class,
                () -> auths.createAuth(new AuthData("token-2", "hans")));
    }

    @Test
    void deleteAuthRemovesToken() throws Exception {
        auths.createAuth(new AuthData("token-1", "ben"));
        auths.deleteAuth("token-1");
        assertNull(auths.getAuth("token-1"));
    }

    @Test
    void deletingMissingAuthFails() {
        assertThrows(DataAccessException.class,
                () -> auths.deleteAuth("missing"));
        assertThrows(DataAccessException.class,
                () -> auths.deleteAuth(null));
    }

    @Test
    void clearRemovesAuths() throws Exception {
        auths.createAuth(new AuthData("token-1", "ben"));
        auths.clear();
        assertNull(auths.getAuth("token-1"));
    }
}