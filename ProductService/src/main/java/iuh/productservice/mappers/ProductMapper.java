package iuh.productservice.mappers;

import iuh.productservice.dtos.responses.ProductResponse;
import iuh.productservice.entities.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductResponse productToProductResponse(Product product);
}
