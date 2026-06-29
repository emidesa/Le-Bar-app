package com.barapp.backend.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    // Numéro de table transmis depuis le QR code
    @NotNull
    private Integer tableNumber;

    @NotEmpty
    private List<OrderItemRequest> items;
}
