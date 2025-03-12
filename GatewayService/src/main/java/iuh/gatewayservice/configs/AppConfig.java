package iuh.gatewayservice.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

@Configuration
public class AppConfig {
    @Bean
    public PathMatcher pathMatcher(){
        return new AntPathMatcher();
    }
}
