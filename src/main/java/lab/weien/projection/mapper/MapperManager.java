package lab.weien.projection.mapper;

import lab.weien.projection.utils.LoggerWrapper;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.modelmapper.convention.MatchingStrategies;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MapperManager {
    private static final LoggerWrapper log = new LoggerWrapper(MapperManager.class, "[MapperManager] ");

    private final static Map<Class<?>, ProjectionMapper<?>> CACHE = new ConcurrentHashMap<>();

    public static <T> ProjectionMapper<T> getMapper(Class<?> sourceType, Class<T> targetType) {
        if (sourceType == null || targetType == null) {
            throw new IllegalArgumentException("sourceType and targetType must not be null");
        }
        if (sourceType.equals(targetType)) {
            log.debug("SourceType {} and targetType {} equals.", sourceType, targetType);
            return new PassThroughMapper<T>(targetType);
        }

        if (CACHE.containsKey(targetType)) {
            log.debug("Found cached mapper for {} to {}", sourceType, targetType);
            return (ProjectionMapper<T>) CACHE.get(targetType);
        }

        log.info("Creating default mapper for {} to {}", sourceType, targetType);
        DefaultMapper<T> defaultMapper = new DefaultMapper<>(targetType);
        CACHE.put(targetType, defaultMapper);

        return defaultMapper;
    }

    record PassThroughMapper<T>(Class<T> tClass) implements ProjectionMapper<T> {
        @Override
        public T map(Object source) {
            return tClass.cast(source);
        }
    }

    record DefaultMapper<T>(Class<T> tClass) implements ProjectionMapper<T> {
        private static final ModelMapper modelMapper = new ModelMapper();

        static {
            modelMapper.getConfiguration()
                    .setFieldMatchingEnabled(true)
                    .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE)
                    .setMatchingStrategy(MatchingStrategies.STRICT);
        }

        @Override
        public T map(Object source) {
            try {
                return modelMapper.map(source, tClass);
            } catch (Exception e) {
                throw new RuntimeException("Mapping fail.", e);
            }
        }
    }
}
