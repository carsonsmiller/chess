package service;

import dataaccess.*;

public class ClearService {
    private final UserDAO users; private final GameDAO games; private final AuthDAO auths;
    public ClearService(UserDAO users, GameDAO games, AuthDAO auths) {this.users = users; this.games=games; this.auths=auths;}
    public void clear() throws DataAccessException { users.clear(); games.clear(); auths.clear(); }
}