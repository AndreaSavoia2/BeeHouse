package com.prj.beehouse.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration class responsible for Cross-Origin Resource Sharing (CORS) settings.
 * <p>
 * This configuration enables cross-origin requests for all application endpoints
 * and defines the allowed HTTP methods and caching behavior for preflight requests.
 */
@Configuration
@EnableWebMvc
public class CorsConfig implements WebMvcConfigurer {

    /**
     * Configures CORS mappings for the application.
     * <p>
     * The current configuration:
     * <ul>
     *     <li>Applies to all endpoints ({@code /**}).</li>
     *     <li>Allows requests from any origin — see {@code SCALABILITY_NOTES.md}, this
     *         must be restricted to the actual frontend origin before production.</li>
     *     <li>Permits the HTTP methods GET, POST, PUT, PATCH, DELETE, and OPTIONS
     *         (OPTIONS is required for browser preflight requests).</li>
     *     <li>Allows any request header, so the frontend can send Authorization,
     *         Content-Type, etc.</li>
     *     <li>Disables credential sharing.</li>
     *     <li>Caches preflight request results for one hour.</li>
     * </ul>
     *
     * @param registry the {@link CorsRegistry} used to register CORS mappings
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }

}
