package com.barapp.backend.dto.response;

import com.barapp.backend.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private Integer tableNumber;
    private OrderStatus status;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;
}