package lab.weien.projection;

import lab.weien.projection.resolver.DtoResolver;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

class DtoResolverTest {
    @Test
    void resolve_shouldNotThrowExceptionWhenValidDtoClass() {
        final Class<?>[] projection = new Class<?>[1];
        Assertions.assertDoesNotThrow(() -> projection[0] = DtoResolver.resolve(TestDto.class));

        final Method[] method = new Method[8];
        Assertions.assertDoesNotThrow(() -> {
            method[0] = projection[0].getMethod("getId");
            method[1] = projection[0].getMethod("getName");
            method[2] = projection[0].getMethod("getTags");
            method[3] = projection[0].getMethod("getMap");
            method[4] = projection[0].getMethod("getMap2");
            method[5] = projection[0].getMethod("getMap3");
            method[6] = projection[0].getMethod("getInternal2");
            method[7] = projection[0].getMethod("getInternal3");
        });

        Assertions.assertEquals(String.class, method[1].getReturnType());

        Assertions.assertEquals(String.class, method[0].getReturnType());
        Assertions.assertEquals("java.util.Map<java.lang.String, java.lang.Object>", method[5].getGenericReturnType().toString());
        Assertions.assertEquals("java.util.List<java.lang.Object>", method[7].getGenericReturnType().toString());

        Assertions.assertEquals("java.util.List<java.lang.String>", method[2].getGenericReturnType().toString());
        Assertions.assertEquals("java.util.Map<java.lang.String, java.lang.String>", method[3].getGenericReturnType().toString());

        Assertions.assertTrue(method[4].getGenericReturnType().toString().startsWith("java.util.Map<java.lang.String, lab.weien.projection.dynamic.$D"));
        Assertions.assertTrue(method[6].getGenericReturnType().toString().startsWith("java.util.List<lab.weien.projection.dynamic.$D"));
    }

    @Data
    static class Parent<ID> {
        ID id;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    static class TestDto extends Parent<String> {
        private String name;
        private List<Internal> tags;
        private Map<String, String> map;
        private Map<String, Internal> map2;
        private Map<String, Map<String, Map<String, Internal>>> map3;

        private Internal2<String, String, String> internal2;
        private Internal2<String, String, Internal> internal3;

        @Data
        static class Internal {
            private String prop;
        }

        @Data
        static class Internal2<A, B, C> {
            private A prop1;
            private B prop2;
            private C prop3;
        }
    }
}