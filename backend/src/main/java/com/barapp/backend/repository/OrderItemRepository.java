package com.barapp.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barapp.backend.entity.OrderItem;
import com.barapp.backend.enums.ItemStatus;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);
    boolean existsByOrderIdAndStatusNot(Long orderId, ItemStatus status);
}
