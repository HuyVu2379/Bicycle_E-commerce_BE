package iuh.gatewayservice.controllers;

import iuh.gatewayservice.dtos.UploadFile;
import iuh.gatewayservice.responses.MessageResponse;
import iuh.gatewayservice.responses.SuccessEntityResponse;
import iuh.gatewayservice.services.Impl.CloudinaryServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/media")
public class UploadFileController {
    private static final Logger logger = LoggerFactory.getLogger(UploadFileController.class);
    private final CloudinaryServiceImpl cloudinaryService;

    @Autowired
    public UploadFileController(CloudinaryServiceImpl cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    /**
     * Upload một file ảnh
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<UploadFile>> uploadSingleFile(
            @RequestPart("file") FilePart filePart,
            @RequestParam(value = "folder", defaultValue = "Bicycle-E-commerce") String folder) {

        logger.info("Received request to upload file: {}", filePart.filename());

        return cloudinaryService.uploadFile(filePart, folder)
                .map(result -> {
                    String url = result.get("secure_url") != null ?
                            (String) result.get("secure_url") : (String) result.get("url");

                    UploadFile response = UploadFile.builder()
                            .imageUrls(List.of(url))
                            .build();

                    return ResponseEntity.ok(response);
                })
                .doOnError(e -> logger.error("Error uploading file: {}", filePart.filename(), e))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    /**
     * Upload nhiều file ảnh
     */
    @PostMapping(value = "/upload/multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<MessageResponse<UploadFile>>> uploadMultipleFiles(
            @RequestPart("files") Flux<FilePart> fileParts,
            @RequestParam(value = "folder", defaultValue = "Bicycle-E-commerce") String folder) {

        logger.info("Received request to upload multiple files");

        return cloudinaryService.uploadMultipleFiles(fileParts, folder)
                .map(result -> SuccessEntityResponse.ok("Files has upload successfully", result))
                .doOnError(e -> logger.error("Error uploading multiple files", e))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    /**
     * Xóa một file ảnh theo URL
     */
    @DeleteMapping("/delete")
    public Mono<ResponseEntity<MessageResponse<Map<String, Object>>>> deleteFile(@RequestParam("url") String url) {
        logger.info("Received request to delete file with URL: {}", url);

        String publicId = cloudinaryService.extractPublicIdFromUrl(url);
        if (publicId == null) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid Cloudinary URL"));
        }

        return cloudinaryService.deleteFile(publicId)
                .map(result -> SuccessEntityResponse.ok("File deleted successfully", result))
                .doOnError(e -> logger.error("Error deleting file with URL: {}", url, e))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    /**
     * Xóa nhiều file ảnh theo danh sách URL
     */
    @DeleteMapping("/delete/multiple")
    public Mono<ResponseEntity<MessageResponse<Map<String, Object>>>> deleteMultipleFiles(@RequestBody UploadFile request) {
        List<String> urls = request.getImageUrls();
        logger.info("Received request to delete {} files", urls.size());

        List<String> publicIds = cloudinaryService.extractPublicIdsFromUrls(urls);
        if (publicIds.isEmpty()) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "No valid Cloudinary URLs provided"));
        }

        return cloudinaryService.deleteMultipleFiles(publicIds)
                .map(ids -> SuccessEntityResponse.ok("Files deleted successfully", ids))
                .doOnError(e -> logger.error("Error deleting multiple files", e))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }
}
