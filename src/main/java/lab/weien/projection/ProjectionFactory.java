package lab.weien.projection;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Objects;

@Slf4j
public class ProjectionFactory {
    private static final String CLASS_NAME_PREFIX = "lab.weien.projection.dynamic.$D";

    static Class<?> create(List<Field> fields, String hash) {
        try {
            String dynamicInterfaceName = CLASS_NAME_PREFIX + hash;

            ByteBuddy byteBuddy = new ByteBuddy();
            DynamicType.Builder<?> builder = byteBuddy
                    .makeInterface()
                    .name(dynamicInterfaceName);

            // TODO: - 泛型解析問題
            //  1. 目前動態生成的介面泛型資訊會丟失
            //  2. 暫時跳過處理自己生成的介面（CLASS_NAME_PREFIX開頭）的泛型解析
            //  3. 未來需要：
            //    - 完善泛型資訊的保存
            //    - 改進 resolveGeneric 方法來正確處理複雜泛型
            //    - 支援動態生成介面的泛型參數
            for (Field field : fields) {
                // TODO: 問題定位點, 自行生成的泛型介面訊息丟失所以避免二次解析自己生成的介面 (未來要支援的話必須要先解決解析問題)
                boolean doSolve = field.genericTypes() != null &&
                        !field.genericTypes().isEmpty() &&
                        !field.type().getName().startsWith(CLASS_NAME_PREFIX);
                if (doSolve) {
                    log.info("Field[name={}, type={}, genericTypes={}]", field.name(), field.type(), field.genericTypes());
                    TypeDescription.Generic fieldGenericType = TypeDescription.Generic.Builder
                            .parameterizedType(field.type(), field.genericTypes())
                            .build();

                    builder = builder.defineMethod(
                                    "get" + capitalize(field.name()),
                                    fieldGenericType,
                                    Visibility.PUBLIC
                            )
                            .withoutCode()
                            .annotateMethod(defineValueAnnotation(field.valueExpression()));
                } else {
                    builder = builder.defineMethod(
                                    "get" + capitalize(field.name()),
                                    field.type(),
                                    Visibility.PUBLIC
                            )
                            .withoutCode()
                            .annotateMethod(defineValueAnnotation(field.valueExpression()));
                }
            }

            return builder
                    .make()
                    .load(ProjectionFactory.class.getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                    .getLoaded();
        } catch (Exception e) {
            Class<?> res = ProjectionManager.get(hash);
            if (res != null) {
                log.error("Lock mechanism failed! Projection interface was created by another thread unexpectedly: {}", res.getName(), e);
                return res;
            }

            throw new RuntimeException("Failed to generate projection interface", e);
        }
    }

    private static AnnotationDescription defineValueAnnotation(String valueExpression) {
        return AnnotationDescription.Builder.ofType(Value.class)
                .define("value", "#{" + valueExpression + "}")
                .build();
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public record Field(String name, Class<?> type, List<Class<?>> genericTypes,
                        String valueExpression) implements Comparable<Field> {
        @Override
        public String valueExpression() {
            return valueExpression != null ? valueExpression : ("target." + name);
        }

        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Field other)) return false;
            return Objects.equals(this.name(), other.name());
        }

        @Override
        public int compareTo(Field other) {
            if (other == null) return 1;
            if (this == other) return 0;
            String otherStr = other.toString();
            String thisStr = this.toString();
            return thisStr.compareTo(otherStr);
        }
    }
}
