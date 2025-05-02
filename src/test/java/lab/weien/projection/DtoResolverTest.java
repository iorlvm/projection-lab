package lab.weien.projection;

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
            method[6] = projection[0].getMethod("getInternalList");
            method[7] = projection[0].getMethod("getInternalList2");
        });

        Assertions.assertEquals(String.class, method[1].getReturnType());

        // 被擦除的泛形
        Assertions.assertEquals(Object.class, method[0].getReturnType());
        Assertions.assertEquals("java.util.Map<java.lang.String, java.lang.Object>", method[5].getGenericReturnType().toString());
        Assertions.assertEquals("java.util.List<java.lang.Object>", method[7].getGenericReturnType().toString());

        // 解析成功的泛形
        Assertions.assertEquals("java.util.List<java.lang.String>", method[2].getGenericReturnType().toString());
        Assertions.assertEquals("java.util.Map<java.lang.String, java.lang.String>", method[3].getGenericReturnType().toString());

        // 後方為動態類名, 無法預先斷言
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

        // NOTE: 剩餘問題, 自定義泛型 (其實已經有點超越 DTO 可接受的程度了)
//        private Internal2<String, String, String> internal2;
//        private Internal2<String, String, Internal> internal3;

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