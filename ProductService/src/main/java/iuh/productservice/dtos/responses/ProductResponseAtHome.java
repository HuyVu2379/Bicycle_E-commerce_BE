/*
 * @ (#) ProductResponseAtHome.java       1.0     5/14/2025
 *
 * Copyright (c) 2025. All rights reserved.
 */

package iuh.productservice.dtos.responses;
/*
 * @author: Luong Tan Dat
 * @date: 5/14/2025
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ProductResponseAtHome {
    private String productId;
    private String productName;
    private double price;
    private double priceReduced;
    private String image;
}
