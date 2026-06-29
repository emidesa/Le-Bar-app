package com.barapp.backend.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    @NotEmpty
    private List<OrderItemRequest> items;
}
