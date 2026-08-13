package com.etour.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/uploads")
public class UploadController {
    private static final Logger log = LoggerFactory.getLogger(UploadController.class);

    private static final List<String> ALLOWED = List.of("png", "jpg", "jpeg", "webp", "gif");
    private static final long MAX_BYTES = 5L * 1024 * 1024;

    private final String configuredDir;

    public UploadController(@Value("${etour.images.dir:images}") String configuredDir) {
        this.configuredDir = configuredDir;
    }

    @PostMapping("/image")
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file was uploaded");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("Image must be 5 MB or smaller");
        }

        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String ext = extensionOf(original);

        if (!ALLOWED.contains(ext)) {
            throw new IllegalArgumentException(
                    "Only " + String.join(", ", ALLOWED) + " images are accepted");
        }

        String stored = UUID.randomUUID().toString().replace("-", "") + "." + ext;

        Path folder = resolveToursFolder();
        Files.createDirectories(folder);

        Path target = folder.resolve(stored).normalize();
        if (!target.startsWith(folder)) {
            throw new IllegalArgumentException("Invalid file name");
        }

        try (var in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }

        String url = "/images/tours/" + stored;
        log.info("Uploaded {} ({} bytes) as {}", original, file.getSize(), url);

        return new ResponseEntity<>(
                Map.of("url", url, "fileName", stored, "originalName", original),
                HttpStatus.CREATED);
    }

    private String extensionOf(String name) {
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) {
            return "";
        }
        return name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private Path resolveToursFolder() {
        Path candidate = Paths.get(configuredDir);

        if (candidate.isAbsolute()) {
            return candidate.resolve("tours").normalize();
        }

        Path workingDir = Paths.get("").toAbsolutePath().normalize();
        Path fromWorkingDir = workingDir.resolve(candidate).normalize();

        if (fromWorkingDir.toFile().isDirectory()) {
            return fromWorkingDir.resolve("tours").normalize();
        }

        Path search = workingDir;
        for (int depth = 0; depth < 4 && search != null; depth++) {
            Path attempt = search.resolve(candidate);
            if (attempt.toFile().isDirectory()) {
                return attempt.resolve("tours").normalize();
            }
            search = search.getParent();
        }

        return fromWorkingDir.resolve("tours").normalize();
    }
}
