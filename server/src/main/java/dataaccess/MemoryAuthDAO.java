package dataaccess;

import model.AuthData;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class MemoryAuthDAO implements AuthDAO {
    private final Map<String, AuthData> auths = new ConcurrentHashMap<>();
    @Override public synchronized void clear() { auths.clear();}
    @Override public synchronized void createAuth(AuthData auth) throws DataAccessException {
        if (auth == null || auth.authToken() == null || auth.username() == null) throw new DataAccessException("Invalid auth");
        if (auths.putIfAbsent(auth.authToken(), auth) != null) throw new DataAccessException("Auth already exists");
    }
    @Override public synchronized AuthData getAuth(String token) { return token == null ? null : auths.get(token); }
    @Override public synchronized void deleteAuth(String token) throws DataAccessException {
        if (token == null || auths.remove(token) == null) throw new DataAccessException("Auth not found");
    }
}