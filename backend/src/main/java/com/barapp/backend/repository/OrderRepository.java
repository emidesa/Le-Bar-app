package com.barapp.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barapp.backend.entity.Order;
import com.barapp.backend.enums.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByTableNumber(Integer tableNumber);
    List<Order> findByStatusNot(OrderStatus status);
}