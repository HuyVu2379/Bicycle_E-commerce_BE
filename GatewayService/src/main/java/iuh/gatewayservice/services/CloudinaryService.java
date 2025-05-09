package iuh.gatewayservice.services;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;
@Service
public interface CloudinaryService {
   Mono<Map<String, Object>> uploadFile(FilePart filePart, String folder);
}
