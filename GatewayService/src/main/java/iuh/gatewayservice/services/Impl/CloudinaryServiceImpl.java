package iuh.gatewayservice.services.Impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import iuh.gatewayservice.dtos.UploadFile;
import iuh.gatewayservice.services.CloudinaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class CloudinaryServiceImpl implements CloudinaryService {
    private static final Logger logger = LoggerFactory.getLogger(CloudinaryServiceImpl.class);
    private final Cloudinary cloudinary;

    public CloudinaryServiceImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public Mono<Map<String, Object>> uploadFile(FilePart filePart, String folder) {
        return Mono.fromCallable(() -> Files.createTempFile("upload_", Objects.requireNonNull(filePart.filename())))
                .flatMap(tempPath -> {
                    File tempFile = tempPath.toFile();

                    return filePart.transferTo(tempFile)
                            .then(Mono.fromCallable(() -> {
                                try {
                                    // Upload lên Cloudinary
                                    Map<String, Object> params = new HashMap<>();
                                    params.put("folder", folder);
                                    params.put("resource_type", "auto");

                                    Map<String, Object> result = cloudinary.uploader().upload(tempFile, params);
                                    logger.info("File {} uploaded to Cloudinary, public_id: {}",
                                            filePart.filename(), result.get("public_id"));
                                    return result;
                                } finally {
                                    // Xóa file tạm
                                    try {
                                        Files.deleteIfExists(tempPath);
                                    } catch (IOException e) {
                                        logger.warn("Failed to delete temp file: {}", tempPath, e);
                                    }
                                }
                            }));
                });
    }

    @Override
    public Mono<UploadFile> uploadMultipleFiles(Flux<FilePart> fileParts, String folder) {
        return fileParts
                .flatMap(filePart -> uploadFile(filePart, folder))
                .map(result -> result.get("secure_url") != null ?
                        (String) result.get("secure_url") : (String) result.get("url"))
                .collectList()
                .map(urls -> UploadFile.builder().imageUrls(urls).build())
                .doOnNext(result -> logger.info("Uploaded {} files to Cloudinary", result.getImageUrls().size()));
    }

    @Override
    public Mono<Map<String, Object>> deleteFile(String publicId) {
        return Mono.fromCallable(() -> {
            try {
                Map<String, Object> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                logger.info("File with public_id {} deleted from Cloudinary", publicId);
                return result;
            } catch (IOException e) {
                logger.error("Error deleting file with public_id {}", publicId, e);
                throw e;
            }
        });
    }

    @Override
    public Mono<Map<String, Object>> deleteMultipleFiles(List<String> publicIds) {
        return Mono.fromCallable(() -> {
            try {
                Map<String, Object> result = cloudinary.api().deleteResources(
                        List.of(publicIds.toArray(new String[0])),
                        ObjectUtils.emptyMap()
                );
                logger.info("Deleted {} files from Cloudinary", publicIds.size());
                return result;
            } catch (IOException e) {
                logger.error("Error deleting multiple files", e);
                throw e;
            }
        });
    }

    @Override
    public String extractPublicIdFromUrl(String cloudinaryUrl) {
        if (!StringUtils.hasText(cloudinaryUrl)) {
            return null;
        }

        try {
            // URL format: https://res.cloudinary.com/cloud_name/image/upload/v1234567890/folder/filename.ext
            String[] parts = cloudinaryUrl.split("/upload/");
            if (parts.length > 1) {
                String pathAfterUpload = parts[1];
                // Loại bỏ version nếu có (v1234567890/)
                if (pathAfterUpload.matches("v\\d+/.*")) {
                    pathAfterUpload = pathAfterUpload.replaceFirst("v\\d+/", "");
                }
                // Loại bỏ phần mở rộng file
                return pathAfterUpload.substring(0, pathAfterUpload.lastIndexOf("."));
            }
        } catch (Exception e) {
            logger.warn("Failed to extract public_id from URL: {}", cloudinaryUrl, e);
        }

        return null;
    }

    @Override
    public List<String> extractPublicIdsFromUrls(List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return Collections.emptyList();
        }

        return urls.stream()
                .map(this::extractPublicIdFromUrl)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}