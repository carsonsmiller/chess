package dataaccess.sql;

import dataaccess.DataAccessException;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SQLUserDAOTest {
    private SQLUserDAO users;

    @BeforeEach
    void setUp() throws Exception {
        SQLDatabase.configureDatabase();
        users = new SQLUserDAO();
        users.clear();
    }

    @Test
    void createAndRetrieveUsersStoresBCryptHash() throws Exception {
        users.createUser(new UserData("benjamin", "password123", "ben@ex.com"));

        UserData stored = users.getUser("benjamin");
        assertNotNull(stored);
        assertEquals("benjamin", stored.username());
        assertEquals("ben@ex.com", stored.email());
        assertNotEquals("password123", stored.password());
        assertTrue(stored.password().startsWith("$2"));
    }

    @Test
    void missingUserReturnsNull() throws Exception {
        assertNull(users.getUser("missing"));
        assertNull(users.getUser(null));
    }

    @Test
    void duplicateUsernameFails() throws Exception {
        users.createUser(new UserData("jose", "password123", "jose@ex.com"));
        assertThrows(DataAccessException.class,
                () -> users.createUser(new UserData("jose", "weird", "weird@ex.com")));
    }

    @Test
    void invalidUserFails() {
        assertThrows(DataAccessException.class,
                () -> users.createUser(null));
        assertThrows(DataAccessException.class,
                () -> users.createUser(new UserData("", "other", "other@ex.com")));
    }

    @Test
    void clearRemovesUsers() throws Exception {
        users.createUser(new UserData("ben", "ben123", "ben@ex.com"));
        users.clear();
        assertNull(users.getUser("ben"));
    }
}