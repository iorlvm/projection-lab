package lab.weien.projection.resolver;

import java.lang.reflect.Type;

public class DtoResolver {
    private static final Resolver INSTANCE = new ResolverVer2();

    public static Class<?> resolve(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("clazz must not be null");
        } else if (clazz.getTypeParameters().length > 0) {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        return INSTANCE.doResolve(clazz);
    }

    public static void changeMode(Mode mode) {
        INSTANCE.setSafeMode(mode == Mode.SAFE);
    }

    public interface Resolver {
        Class<?> doResolve(Type... type);
        void setSafeMode(boolean safeMode);
    }

    public enum Mode {
        SAFE,
        DEFAULT
    }
}
