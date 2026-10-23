package service;

import dataaccess.*;
import model.*;
import java.util.UUID;

public class UserService {
    private final UserDAO users; private final AuthDAO auths;
    public UserService(UserDAO users, AuthDAO auths) { this.users=users; this.auths=auths; }
    public AuthData register(String username, String password, String email) throws DataAccessException, ServiceException {
        if (blank(username) || blank(password) || blank(email)) throw new ServiceException(400, "Error: Bad request");
        if (users.getUser(username) != null) throw new ServiceException(403, "Error: already taken");
        users.createUser(new UserData(username, password, email));
        return newAuth(username);
    }
    public AuthData login(String username, String password) throws DataAccessException, ServiceException {
        if (blank(username) || blank(password)) throw new ServiceException(400, "Error: bad request");
        UserData user=users.getUser(username);
        if (user == null || !user.password().equals(password)) throw new ServiceException(401, "Error: Unauthorized");
        return newAuth(username);
    }
    public void logout(String token) throws DataAccessException, ServiceException {
        if (token == null || auths.getAuth(token) == null) throw new ServiceException(401, "Error: Unauthorized");
        auths.deleteAuth(token);
    }
    public AuthData requireAuth(String token) throws DataAccessException, ServiceException {
        AuthData auth = token == null ? null : auths.getAuth(token);
        if (auth == null) throw new ServiceException(401, "Error: Unauthorized");
        return auth;
    }
    private AuthData newAuth(String username) throws DataAccessException {
        AuthData auth = new AuthData(UUID.randomUUID().toString(), username); auths.createAuth(auth); return auth;
    }
    private boolean blank(String s) { return s == null || s.isBlank();}
}