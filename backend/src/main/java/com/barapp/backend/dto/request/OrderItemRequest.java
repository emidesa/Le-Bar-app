package com.barapp.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderItemRequest {
    @NotNull
    private Long cocktailId;

    @NotNull
    private Long sizeId;

    @Min(1)
    private int quantity;
}
