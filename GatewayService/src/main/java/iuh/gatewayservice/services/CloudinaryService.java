package iuh.gatewayservice.services;

import iuh.gatewayservice.dtos.UploadFile;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
@Service
public interface CloudinaryService {
   Mono<Map<String, Object>> uploadFile(FilePart filePart, String folder);
   Mono<UploadFile> uploadMultipleFiles(Flux<FilePart> fileParts, String folder);
   Mono<Map<String, Object>> deleteFile(String publicId);
   Mono<Map<String, Object>> deleteMultipleFiles(List<String> publicIds);
   String extractPublicIdFromUrl(String cloudinaryUrl);
   List<String> extractPublicIdsFromUrls(List<String> urls);
}
