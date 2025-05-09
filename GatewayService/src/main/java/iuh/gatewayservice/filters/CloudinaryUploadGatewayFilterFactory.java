package iuh.gatewayservice.filters;

import iuh.gatewayservice.services.CloudinaryService;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;

@Component
public class CloudinaryUploadGatewayFilterFactory extends AbstractGatewayFilterFactory<CloudinaryUploadGatewayFilterFactory.Config> {
    private static final Logger logger = LoggerFactory.getLogger(CloudinaryUploadGatewayFilterFactory.class);
    private final CloudinaryService cloudinaryService;

    public CloudinaryUploadGatewayFilterFactory(CloudinaryService cloudinaryService) {
        super(Config.class);
        this.cloudinaryService = cloudinaryService;
    }

    @Data
    public static class Config {
        private String fileParamName = "avatar";
        private String folder = "Bicycle-E-commerce";
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            logger.info("Processing request in Cloudinary filter: {}", request.getURI());

            MediaType contentType = request.getHeaders().getContentType();
            if (!MediaType.MULTIPART_FORM_DATA.isCompatibleWith(contentType)) {
                logger.debug("Request is not multipart/form-data, forwarding original request");
                return chain.filter(exchange);
            }

            logger.info("Processing multipart request with content type: {}", contentType);

            return ServerRequest.create(exchange, Collections.emptyList())
                    .multipartData()
                    .doOnNext(parts -> logger.debug("Multipart parts received: {}", parts.keySet()))
                    .flatMap(parts -> processMultipartData(parts, config, exchange))
                    .onErrorResume(e -> {
                        logger.error("Error processing multipart data: {}", e.getMessage(), e);
                        exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
                        return Mono.just(exchange);
                    })
                    .flatMap(modifiedExchange -> {
                        logger.debug("Forwarding modified exchange to next filter");
                        return chain.filter(modifiedExchange);
                    });
        };
    }

    private Mono<ServerWebExchange> processMultipartData(
            MultiValueMap<String, Part> parts, Config config, ServerWebExchange exchange) {

        if (!parts.containsKey(config.getFileParamName())) {
            logger.debug("No file found with param name: {}, forwarding original request", config.getFileParamName());
            return Mono.just(exchange);
        }

        Part part = parts.getFirst(config.getFileParamName());
        if (!(part instanceof FilePart)) {
            logger.warn("Part is not a FilePart: {}", part.getClass().getName());
            exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
            return Mono.just(exchange);
        }

        FilePart filePart = (FilePart) part;
        logger.info("Processing file part: {}", filePart.name());

        return cloudinaryService.uploadFile(filePart, config.getFolder())
                .flatMap(uploadResult -> {
                    logger.info("File uploaded successfully: public_id={}", uploadResult.get("public_id"));

                    ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                            .header("X-Cloudinary-Public-Id", (String) uploadResult.get("public_id"))
                            .header("X-Cloudinary-Url", (String) uploadResult.get("url"))
                            .header("X-Cloudinary-Secure-Url", (String) uploadResult.get("secure_url"))
                            .build();

                    ServerWebExchange modifiedExchange = exchange.mutate()
                            .request(modifiedRequest)
                            .build();
                    modifiedExchange.getAttributes().put("cloudinaryUploadResult", uploadResult);

                    return Mono.just(modifiedExchange);
                })
                .onErrorResume(e -> {
                    logger.error("Failed to upload file to Cloudinary: {}", e.getMessage(), e);
                    exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                    return Mono.just(exchange);
                });
    }
}