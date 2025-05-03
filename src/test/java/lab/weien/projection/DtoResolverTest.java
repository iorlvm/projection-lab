package lab.weien.projection;

import lab.weien.projection.resolver.DtoResolver;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static lab.weien.projection.ProjectionFactory.DYNAMIC_CLASS_NAME_PREFIX;
import static org.junit.jupiter.api.Assertions.*;

class DtoResolverTest {
    @Test
    void should_ResolveSampleDto_Successfully() {
        DtoResolver.changeMode(DtoResolver.Mode.DEFAULT);

        final Class<?> projection = DtoResolver.resolve(SampleDto.class);
        final Method[] method = projection.getMethods();

        assertNotEquals(SampleDto.class, projection);
        assertEquals(1, method.length);
        assertEquals("getProp", method[0].getName());
        assertEquals(String.class, method[0].getReturnType());
    }

    static class SampleDto {
        private String prop;
    }


    @Test
    void should_ResolveSimpleCollectionDto_Successfully() throws NoSuchMethodException {
        DtoResolver.changeMode(DtoResolver.Mode.DEFAULT);

        final Class<?> projection = DtoResolver.resolve(SimpleCollectionDto.class);

        final Method listMethod = projection.getMethod("getList");
        final Method mapMethod = projection.getMethod("getMap");

        assertEquals("java.util.List<java.lang.String>", listMethod.getGenericReturnType().toString());
        assertEquals("java.util.Map<java.lang.String, java.lang.String>", mapMethod.getGenericReturnType().toString());
    }

    static class SimpleCollectionDto {
        List<String> list;
        Map<String, String> map;
    }

    @Test
    void should_ResolveWithSafeMode_Successfully() {
        DtoResolver.changeMode(DtoResolver.Mode.SAFE);

        Class<?> projection = DtoResolver.resolve(SafeModeDto.class);
        assertNotEquals(SafeModeDto.class, projection);

        final Method[] method = new Method[4];
        assertDoesNotThrow(() -> (method[0] = projection.getMethod("getProp1")));
        assertThrows(NoSuchMethodException.class, () -> method[1] = projection.getMethod("getProp2"));
        assertThrows(NoSuchMethodException.class, () -> method[2] = projection.getMethod("getProp3"));
        assertThrows(NoSuchMethodException.class, () -> method[3] = projection.getMethod("getProp4"));

        assertEquals(String.class, method[0].getReturnType());
    }

    static class SafeModeDto {
        @Setter
        @Getter
        private String prop1;

        @Setter
        private String prop2;

        @Getter
        private String prop3;

        private String prop4;
    }

    @Test
    void should_ResolveComplexNestedTypes_Successfully() {
        DtoResolver.changeMode(DtoResolver.Mode.DEFAULT);

        Class<?> projection = DtoResolver.resolve(ComplexNestedDto.class);
        assertNotEquals(ComplexNestedDto.class, projection);

        final Method[] methods = new Method[6];
        assertDoesNotThrow(() -> {
            methods[0] = projection.getMethod("getId");
            methods[1] = projection.getMethod("getNestedList");
            methods[2] = projection.getMethod("getNestedMap");
            methods[3] = projection.getMethod("getDeepNestedMap");
            methods[4] = projection.getMethod("getSimpleGeneric");
            methods[5] = projection.getMethod("getMixedGeneric");
        });

        assertEquals(String.class, methods[0].getReturnType());
        assertTrue(methods[1].getGenericReturnType().toString().startsWith("java.util.List<" + DYNAMIC_CLASS_NAME_PREFIX));
        assertTrue(methods[2].getGenericReturnType().toString().startsWith("java.util.Map<java.lang.String, " + DYNAMIC_CLASS_NAME_PREFIX));
        assertTrue(methods[3].getGenericReturnType().toString().startsWith("java.util.Map<java.lang.String, java.util.Map<java.lang.String, java.util.Map<java.lang.String, " + DYNAMIC_CLASS_NAME_PREFIX));

        assertTrue(methods[4].getReturnType().toString().startsWith("interface " + DYNAMIC_CLASS_NAME_PREFIX));
        assertTrue(methods[5].getReturnType().toString().startsWith("interface " + DYNAMIC_CLASS_NAME_PREFIX));
    }

    static class BaseEntity<ID> {
        ID id;
    }

    static class ComplexNestedDto extends BaseEntity<String> {
        private List<SimpleNested> nestedList;

        private Map<String, SimpleNested> nestedMap;
        private Map<String, Map<String, Map<String, SimpleNested>>> deepNestedMap;

        private TripleGeneric<String, String, String> simpleGeneric;
        private TripleGeneric<String, SimpleNested, TripleGeneric<String, String, String>> mixedGeneric;

        static class SimpleNested {
            private String prop;
        }

        static class TripleGeneric<A, B, C> {
            private A prop1;
            private B prop2;
            private C prop3;
        }
    }
}