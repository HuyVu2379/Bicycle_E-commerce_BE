package iuh.productservice.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddressResponse {
    private Data data;

    public String getAddressId() {
        return data.getAddressId();
    }

    @lombok.Data
    public static class Data {
        private String addressId;
    }
}
