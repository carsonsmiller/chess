package service;

import dataaccess.*;
import model.AuthData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class UserServiceTests {
    private MemoryUserDAO users;
    private MemoryAuthDAO auths;
    private UserService service;

    @BeforeEach void setup() {
        users = new MemoryUserDAO();
        auths = new MemoryAuthDAO();
        service = new UserService(users,auths);
    }

    @Test void registerAndLoginSucceed() throws Exception {
        AuthData registered = service.register("ben","pw","b@b.com");
        assertEquals("ben", registered.username());
        assertNotNull(registered.authToken());
        AuthData loggedIn = service.login("ben","pw");
        assertEquals("ben",loggedIn.username());
        assertNotEquals(registered.authToken(), loggedIn.authToken());
    }

    @Test void duplicateRegisterFail() throws Exception {
        service.register("ben","pw","b@b.com");
        ServiceException ex = assertThrows(ServiceException.class,()->service.register("ben", "pw", "b@b.com"));
        assertEquals(403, ex.statusCode());
    }

    @Test void badRegisterAndLoginFail() {
        assertEquals(400, assertThrows(ServiceException.class, ()->service.register("","pw","b@b.com")).statusCode());
        assertEquals(401, assertThrows(ServiceException.class, ()->service.login("missing","pw")).statusCode());
    }

    @Test void logoutAndAuthValidation() throws Exception {
        AuthData auth = service.register("ben", "pw", "a@b.com");
        assertEquals("ben",service.requireAuth(auth.authToken()).username());
        service.logout(auth.authToken());
        assertEquals(401,assertThrows(ServiceException.class,()->service.requireAuth(auth.authToken())).statusCode());
    }
}