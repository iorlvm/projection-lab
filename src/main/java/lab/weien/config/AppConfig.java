package lab.weien.config;

import lab.weien.projection.scan.DtoScanner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean // 配置啟動後自動掃描
    public DtoScanner dtoScanner(ApplicationContext context) {
        return new DtoScanner(context);
    }
}
