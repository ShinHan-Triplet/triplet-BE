package org.zerock.triplet.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Paths;

@Configuration
@Profile("dev")
public class WebConfigDev implements WebMvcConfigurer {

    @Value("${app.upload.trip-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){
        // http://localhost:8080/static/trips/{filename}
        String loc = "file:"+ Paths.get(uploadDir).toAbsolutePath() + File.separator;
        registry.addResourceHandler("/static/trips/**")
                .addResourceLocations(loc);
    }
}
