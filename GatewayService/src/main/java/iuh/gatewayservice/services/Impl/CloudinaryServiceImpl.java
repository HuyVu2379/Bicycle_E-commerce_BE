package iuh.gatewayservice.services.Impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import iuh.gatewayservice.services.CloudinaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryServiceImpl implements CloudinaryService {
    private static final Logger logger = LoggerFactory.getLogger(CloudinaryServiceImpl.class);
    private final Cloudinary cloudinary;

    public CloudinaryServiceImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public Mono<Map<String, Object>> uploadFile(FilePart filePart, String folder) {
        return Mono.defer(() -> {
            if (filePart == null) {
                logger.error("FilePart is null");
                return Mono.error(new IllegalArgumentException("FilePart cannot be null"));
            }

            logger.info("Starting upload for file: {}", filePart.filename());

            // Chuyển DataBuffer thành byte array để upload
            return filePart.content()
                    .collectList()
                    .flatMap(dataBuffers -> {
                        try {
                            byte[] bytes = dataBuffers.stream()
                                    .map(dataBuffer -> {
                                        byte[] bufferBytes = new byte[dataBuffer.readableByteCount()];
                                        dataBuffer.read(bufferBytes);
                                        return bufferBytes;
                                    })
                                    .reduce(new byte[0], (a, b) -> {
                                        byte[] result = new byte[a.length + b.length];
                                        System.arraycopy(a, 0, result, 0, a.length);
                                        System.arraycopy(b, 0, result, a.length, b.length);
                                        return result;
                                    });

                            // Cấu hình tùy chọn upload
                            Map<String, Object> options = ObjectUtils.asMap(
                                    "resource_type", "auto",
                                    "folder", folder != null && !folder.isEmpty() ? folder : "default"
                            );

                            // Upload lên Cloudinary
                            Map uploadResult = cloudinary.uploader().upload(bytes, options);
                            logger.info("File uploaded successfully: public_id={}", uploadResult.get("public_id"));

                            @SuppressWarnings("unchecked")
                            Map<String, Object> result = uploadResult;
                            return Mono.just(result);
                        } catch (IOException e) {
                            logger.error("Failed to upload file to Cloudinary: {}", e.getMessage());
                            return Mono.error(new RuntimeException("Failed to upload file to Cloudinary", e));
                        }
                    });
        }).onErrorMap(e -> {
            logger.error("Error processing file upload: {}", e.getMessage());
            return new RuntimeException("Upload failed", e);
        });
    }
}