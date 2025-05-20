package iuh.cartservice.dtos.requests;

import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonCreator;

public class BulkDeleteCartItemRequest extends ArrayList<String> {
    @JsonCreator
    public BulkDeleteCartItemRequest(ArrayList<String> items) {
        super(items);
    }
}
