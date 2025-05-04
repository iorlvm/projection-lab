package lab.weien.projection.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerWrapper {
    private final String prefix;
    private final Logger log;

    public LoggerWrapper(String name) {
        this(name, "");
    }

    public LoggerWrapper(String name, String prefix) {
        this.log = LoggerFactory.getLogger(name);
        this.prefix = prefix;
    }

    public LoggerWrapper(Class<?> clazz) {
        this(clazz.getName(), "");
    }

    public LoggerWrapper(Class<?> clazz, String prefix) {
        this(clazz.getName(), prefix);
    }

    public void info(String message, Object... args) {
        log.info(prefix + message, args);
    }

    public void error(String message, Object... args) {
        log.error(prefix + message, args);
    }

    public void debug(String message, Object... args) {
        log.debug(prefix + message, args);
    }

    public void warn(String message, Object... args) {
        log.warn(prefix + message, args);
    }

    public void trace(String message, Object... args) {
        log.trace(prefix + message, args);
    }
}
