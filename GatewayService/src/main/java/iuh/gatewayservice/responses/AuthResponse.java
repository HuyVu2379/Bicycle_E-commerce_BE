package iuh.gatewayservice.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private int statusCode;
    private String message;
    private boolean success = false;
    @JsonProperty("timestamp")
    private long timestamp = System.currentTimeMillis();
}
