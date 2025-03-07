package iuh.cartservice.dtos.responses;

import lombok.*;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ErrorResponse {
    private int status;
    private String message;
    private boolean success;
    private Map<String, String> details;
    private long timestamp;
    private String path;
}
