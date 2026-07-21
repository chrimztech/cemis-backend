package zm.unza.tels.cemis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class AppConfig {

    @Value("${app.public-url:http://localhost:5173}")
    private String publicUrl;

    @Bean
    public CorsFilter corsFilter() {
        var config = new CorsConfiguration();
        config.setAllowCredentials(true);
        // The real deployed frontend origin, always allowed.
        config.addAllowedOrigin(publicUrl);
        // Any localhost/127.0.0.1 port — Vite's dev server picks a free port
        // per run (5173, 8081, 8082, ...), so a fixed list keeps breaking.
        config.setAllowedOriginPatterns(List.of(
            publicUrl, "http://localhost:*", "http://127.0.0.1:*"
        ));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setExposedHeaders(List.of("Authorization"));

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
