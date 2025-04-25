package lab.weien.factory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Objects;

public class ProjectionFactory {
    public static Class<?> create(List<Field> fields) {
        try {
            String dynamicInterfaceName = "lab.weien.dynamic.projection.$DynamicProjection";

            ByteBuddy byteBuddy = new ByteBuddy();
            DynamicType.Builder<?> builder = byteBuddy
                    .makeInterface()
                    .name(dynamicInterfaceName);

            for (Field field : fields) {
                builder = builder.defineMethod(
                                "get" + capitalize(field.getName()),
                                field.getClazz(),
                                Visibility.PUBLIC
                        )
                        .withoutCode()
                        .annotateMethod(
                                AnnotationDescription.Builder.ofType(Value.class)
                                        .define("value", "#{" + field.getAttribute() + "}")
                                        .build()
                        );
            }

            return builder.make()
                    .load(ProjectionFactory.class.getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                    .getLoaded();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate projection interface", e);
        }
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }


    @Getter
    @AllArgsConstructor
    public static class Field {
        private String name;
        private Class<?> clazz;
        private String attribute;

        public Field(String name, Class<?> clazz) {
            this(name, clazz, null);
        }

        public String getAttribute() {
            return "target." + (attribute == null ? name : attribute);
        }

        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Field other)) return false;
            return Objects.equals(this.getName(), other.getName());
        }
    }
}
