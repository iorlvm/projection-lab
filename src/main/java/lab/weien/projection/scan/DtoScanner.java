package lab.weien.projection.scan;

import lab.weien.projection.annotation.ProjectionDto;
import lab.weien.projection.resolver.DtoResolver;
import lab.weien.projection.utils.LoggerWrapper;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.Environment;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Set;

public class DtoScanner implements CommandLineRunner {
    private static final LoggerWrapper log = new LoggerWrapper(DtoScanner.class, "[DtoScanner] ");

    private final String scanPackage;

    public DtoScanner(String scanPackage) {
        this.scanPackage = scanPackage;
    }

    public DtoScanner(ApplicationContext context) {
        this.scanPackage = getScanPackage(context);
    }

    public String getScanPackage(ApplicationContext context) {
        try {
            Environment env = context.getEnvironment();
            String scanPackage = env.getProperty("projection.scan.package");

            if (scanPackage != null && !scanPackage.trim().isEmpty()) {
                return scanPackage;
            }

            Class<?> mainApplicationClass = context.getBeansWithAnnotation(SpringBootApplication.class)
                    .values().iterator().next().getClass();
            scanPackage = mainApplicationClass.getPackage().getName();
            log.info("Automatically set scan package to: {}", scanPackage);
            return scanPackage;
        } catch (Exception e) {
            log.error("Unable to determine base package name automatically", e);
            throw new IllegalStateException("Failed to determine scan package. Please specify 'projection.scan.package' in configuration", e);
        }
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting to scan for @ProjectionDto classes in package: {}", scanPackage);
        long startTime = System.currentTimeMillis();
        try {
            ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new AnnotationTypeFilter(ProjectionDto.class));

            Set<BeanDefinition> candidates = scanner.findCandidateComponents(scanPackage);

            if (candidates.isEmpty()) {
                log.info("No classes with @ProjectionDto annotation found in package: {}", scanPackage);
                return;
            }

            log.info("Found {} classes with @ProjectionDto annotation", candidates.size());

            for (BeanDefinition beanDefinition : candidates) {
                processProjectionDto(beanDefinition);
            }
        } catch (Exception e) {
            log.error("Error occurred while scanning for @ProjectionDto classes", e);
            throw e;
        } finally {
            long endTime = System.currentTimeMillis();
            log.info("Completed scanning for @ProjectionDto classes in {} ms", endTime - startTime);
        }
    }

    private void processProjectionDto(BeanDefinition beanDefinition) {
        String className = beanDefinition.getBeanClassName();
        try {
            Class<?> clazz = Class.forName(className);
            log.debug("Processing @ProjectionDto class: {}", className);
            DtoResolver.resolve(clazz);
        } catch (ClassNotFoundException e) {
            log.error("Failed to load class: {}", className, e);
        } catch (Exception e) {
            log.error("Error processing class: {}", className, e);
        }
    }
}
