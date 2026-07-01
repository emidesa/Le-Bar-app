package com.barapp.backend.mapper;

import com.barapp.backend.dto.response.OrderItemResponse;
import com.barapp.backend.dto.response.OrderResponse;
import com.barapp.backend.entity.Order;
import com.barapp.backend.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(this::toItemResponse)
                .toList();
        return new OrderResponse(
                order.getId(),
                order.getTableNumber(),
                order.getStatus(),
                order.getTotalPrice(),
                order.getCreatedAt(),
                items
        );
    }

    public OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getCocktail().getName(),
                item.getSize().getSize(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getStatus()
        );
    }
}
