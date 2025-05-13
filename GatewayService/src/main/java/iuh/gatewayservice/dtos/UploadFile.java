package iuh.gatewayservice.dtos;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UploadFile {
    private List<String> imageUrls;
}
