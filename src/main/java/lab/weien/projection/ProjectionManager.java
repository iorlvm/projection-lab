package lab.weien.projection;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ProjectionManager {
    private static final ConcurrentHashMap<String, Class<?>> projectionCache = new ConcurrentHashMap<>();

    static void register(String key, Class<?> projectionClass) {
        projectionCache.put(key, projectionClass);
    }

    static Class<?> get(String key) {
        return projectionCache.get(key);
    }

    static String toHash(ProjectionBuilder builder) {
        byte[] bytes = builder.toString().getBytes();
        return UUID.nameUUIDFromBytes(bytes).toString().replace("-", "");
    }
}
