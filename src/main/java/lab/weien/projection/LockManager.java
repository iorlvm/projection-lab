package lab.weien.projection;

import java.util.concurrent.ConcurrentHashMap;

public class LockManager<T> {
    private final ConcurrentHashMap<T, Object> lockMap = new ConcurrentHashMap<>();

    private Object getLock(T key) {
        return lockMap.computeIfAbsent(key, k -> new Object());
    }

    /**
     * 執行帶鎖的操作，確保只有單一線程能進入該鍵的臨界區。
     * @param key 鎖的鍵（hash）
     * @param action 執行的操作 (lambda)
     * @return 執行結果
     */
    public <R> R executeWithLock(T key, LockedAction<R> action) {
        Object lock = getLock(key);
        synchronized (lock) {
            try {
                return action.execute();
            } finally {
                lockMap.remove(key);
            }
        }
    }

    @FunctionalInterface
    public interface LockedAction<R> {
        R execute();
    }
}
