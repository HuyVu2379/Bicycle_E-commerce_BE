/*
 * @ (#) CreateMomoResponse.java       1.0     4/25/2025
 *
 * Copyright (c) 2025. All rights reserved.
 */

package iuh.paymentservice.dtos.responses;
/*
 * @author: Luong Tan Dat
 * @date: 4/25/2025
 */

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MomoPaymentResponse {
    private String paymentUrl;
}
