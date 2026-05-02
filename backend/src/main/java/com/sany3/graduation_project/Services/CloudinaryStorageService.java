package com.sany3.graduation_project.Services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class CloudinaryStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "application/pdf"
    );

    private final Cloudinary cloudinary;

    public CloudinaryStorageService(@Value("${cloudinary.url:}") String cloudinaryUrl) {
        this.cloudinary = cloudinaryUrl == null || cloudinaryUrl.isBlank()
                ? null
                : new Cloudinary(cloudinaryUrl);
    }

    public String upload(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is required");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Only JPG, PNG, WEBP, and PDF files are allowed");
        }

        if (cloudinary == null) {
            throw new IllegalStateException("CLOUDINARY_URL is not configured");
        }

        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "resource_type", "auto"
                    )
            );

            Object secureUrl = result.get("secure_url");
            if (secureUrl == null) {
                throw new IllegalStateException("Cloudinary did not return a secure URL");
            }

            return secureUrl.toString();
        } catch (IOException ex) {
            log.error("Failed to read uploaded file", ex);
            throw new IllegalStateException("Could not read uploaded file");
        } catch (RuntimeException ex) {
            log.error("Failed to upload file to Cloudinary", ex);
            throw new IllegalStateException("Could not upload file to Cloudinary");
        }
    }
}
