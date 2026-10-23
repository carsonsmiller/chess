package dataaccess;

import model.UserData;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class MemoryUserDAO implements UserDAO {
    private final Map<String, UserData> users = new ConcurrentHashMap<>();
    @Override public synchronized void clear() { users.clear();}
    @Override public synchronized void createUser(UserData user) throws DataAccessException {
        if (user == null || user.username() == null) throw new DataAccessException("Invalid user");
        if (users.containsKey(user.username())) throw new DataAccessException("Username already taken");
        users.put(user.username(), user);
    }
    @Override public synchronized UserData getUser(String username) { return username == null ? null : users.get(username); }
}