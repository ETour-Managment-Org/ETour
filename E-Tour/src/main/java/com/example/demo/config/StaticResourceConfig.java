package com.example.demo.config;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(StaticResourceConfig.class);

    private final String configuredDir;

    public StaticResourceConfig(@Value("${etour.images.dir:images}") String configuredDir) {
        this.configuredDir = configuredDir;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        Path resolved = resolveImagesDirectory();
        String fileLocation = resolved.toUri().toString();

        log.info("Serving /images/** from file location : {}", fileLocation);
        log.info("Serving /images/** fallback classpath : classpath:/static/images/");
        log.info("Images directory exists on disk       : {}", resolved.toFile().isDirectory());

        registry.addResourceHandler("/images/**")
                .addResourceLocations(fileLocation, "classpath:/static/images/")
                .setCachePeriod(3600);
    }

    private Path resolveImagesDirectory() {

        Path candidate = Paths.get(configuredDir);

        if (candidate.isAbsolute()) {
            return candidate.normalize();
        }

        Path workingDir = Paths.get("").toAbsolutePath().normalize();
        Path fromWorkingDir = workingDir.resolve(candidate).normalize();

        if (fromWorkingDir.toFile().isDirectory()) {
            return fromWorkingDir;
        }

        Path search = workingDir;
        for (int depth = 0; depth < 4 && search != null; depth++) {
            File attempt = search.resolve(candidate).toFile();
            if (attempt.isDirectory()) {
                return attempt.toPath().normalize();
            }
            search = search.getParent();
        }

        return fromWorkingDir;
    }
}
