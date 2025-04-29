package lab.weien.projection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class ProjectionBuilderTest {
    @Test
    void build_shouldThrowExceptionWhenNoFieldsDefined() {
        ProjectionBuilder projectionBuilder = new ProjectionBuilder();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, projectionBuilder::build);
        assertEquals("ProjectionBuilder: No fields defined. Please add fields before calling build() method.", exception.getMessage());
        System.out.println("Expected exception thrown when no fields defined.");
    }

    @Test
    void build_shouldReturnCachedClass() {
        Class<?> first = new ProjectionBuilder()
                .addField("name", String.class)
                .addField("price", Long.class, "target.product.price")
                .build();

        Class<?> second = new ProjectionBuilder()
                .addField("name", String.class)
                .addField("price", Long.class, "target.product.price")
                .build();

        assertSame(first, second);
    }

    @Test
    void build_shouldReturnCachedClass_fromEntityAndField() {
        Class<?> first = new ProjectionBuilder()
                .fromEntity(TestEntity.class, "name", "price", "tags")
                .build();

        Class<?> second = new ProjectionBuilder()
                .addField("name", String.class)
                .addField("price", Long.class)
                .addField("tags", List.class, List.of(String.class))
                .build();

        assertSame(first, second);
    }

    @Test
    void build_shouldReturnDifferentClass_whenDifferentFields() {
        Class<?> first = new ProjectionBuilder()
                .fromEntity(TestEntity.class, "tags")
                .build();

        Class<?> second = new ProjectionBuilder()
                .addField("tags", Set.class, List.of(String.class))
                .build();

        Class<?> third = new ProjectionBuilder()
                .addField("tags", List.class, List.of(Double.class))
                .build();

        assertNotSame(first, second);
        assertNotSame(first, third);
        assertNotSame(second, third);
    }

    @Test
    void concurrentBuild_shouldReturnSameClass() throws InterruptedException, ExecutionException {
        int threadCount = 1000;
        long startTime = System.nanoTime();
        try (
                ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        ) {
            List<Future<Class<?>>> futures = new ArrayList<>();

            ProjectionBuilder builderTemplate = new ProjectionBuilder()
                    .fromEntity(TestEntity.class, "name", "price", "count", "tags");

            Callable<Class<?>> task = builderTemplate::build;

            for (int i = 0; i < threadCount; i++) {
                futures.add(executorService.submit(task));
            }

            Set<Class<?>> resultSet = new HashSet<>();
            for (Future<Class<?>> future : futures) {
                resultSet.add(future.get());
            }

            executorService.shutdown();
            while (!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
                System.out.println("Waiting for termination of executor service... ");
            }

            assertEquals(1, resultSet.size());
            Class<?> resultClass = resultSet.iterator().next();
            assertNotNull(resultClass);
        }
        long endTime = System.nanoTime();
        long totalTimeMillis = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        System.out.println("Concurrent build test finished. Total execution time: " + totalTimeMillis + " ms");
    }

    @Test
    void input_shouldNotThrowExceptionWhenValidField() {
        ProjectionBuilder builder = new ProjectionBuilder();

        String fieldName = "fieldName";
        String valueExpression = "target.name";

        List<Class<?>> validTypes = List.of(
                String.class, Long.class, Double.class, Double.class, TestEntity.class
        );

        for (Class<?> validType : validTypes) {
            Assertions.assertDoesNotThrow(
                    () -> {
                        builder.addField(fieldName, validType);
                        builder.addField(fieldName, validType, valueExpression);
                        builder.addField(fieldName, List.class, List.of(validType));
                        builder.addField(fieldName, List.class, List.of(validType), valueExpression);
                        builder.addField(fieldName, Set.class, List.of(validType, String.class));
                        builder.addField(fieldName, Set.class, List.of(validType, String.class, Double.class), valueExpression);
                    }
            );
        }

        List<String> validValueExpressions = List.of(
                "  target.name",
                "target.name  ",
                "    target.name    ",
                "target.tags.name",
                "target.price + target.count",
                "target.price - target.count",
                "target.price * target.count",
                "target.price / target.count"
        );

        for (String validValueExpression : validValueExpressions) {
            Assertions.assertDoesNotThrow(
                    () -> {
                        builder.addField(fieldName, String.class, validValueExpression);
                        builder.addField(fieldName, List.class, List.of(String.class), validValueExpression);
                    }
            );
        }

        System.out.println("All valid fields tested. No exception thrown.");
    }

    @Test
    void input_shouldThrowExceptionWhenInvalidField() {
        ProjectionBuilder builder = new ProjectionBuilder();

        String fieldName = "fieldName";

        List<String> invalidValueExpressions = List.of(
                "target",                       // 缺少 .field
                "target.product.",              // 結尾不完整
                "product.name",                 // 沒有以 target 開頭
                "target..name",                 // 非法的連續點符號
                "target.product.price + ",      // 運算符後缺值
                "target.name @ target.value"    // 非法運算符 "@"
        );

        for (String invalidExpression : invalidValueExpressions) {
            Assertions.assertThrows(IllegalArgumentException.class, () ->
                            builder.addField(fieldName, String.class, invalidExpression),
                    "IllegalArgumentException was expected for invalid value expression in addField: " + invalidExpression
            );
            Assertions.assertThrows(IllegalArgumentException.class, () ->
                            builder.addField(fieldName, List.class, List.of(String.class), invalidExpression),
                    "IllegalArgumentException was expected for invalid value expression in addField: " + invalidExpression
            );
        }

        System.out.println("All invalid fields tested. Expected exceptions thrown.");
    }

    static class TestEntity {
        private String name;
        private Long price;
        private Integer count;
        private List<String> tags;
    }
}