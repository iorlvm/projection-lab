package lab.weien.projection;

import lab.weien.projection.impl.ResolverImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DtoResolver {
    private static final Resolver INSTANCE = new ResolverImpl();

    public static Class<?> resolve(Class<?> clazz) {
        return INSTANCE.resolve(clazz);
    }

    public interface Resolver {
        Class<?> resolve(Class<?> clazz);
    }
}
