package com.barapp.backend.service;

import com.barapp.backend.dto.response.OrderItemResponse;
import com.barapp.backend.dto.response.OrderResponse;
import com.barapp.backend.entity.*;
import com.barapp.backend.enums.CocktailSizeEnum;
import com.barapp.backend.enums.ItemStatus;
import com.barapp.backend.enums.OrderStatus;
import com.barapp.backend.mapper.OrderMapper;
import com.barapp.backend.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.barapp.backend.dto.request.OrderItemRequest;
import com.barapp.backend.dto.request.OrderRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock OrderItemRepository orderItemRepository;
    @Mock CocktailRepository cocktailRepository;
    @Mock CocktailSizeRepository cocktailSizeRepository;
    @Mock OrderMapper orderMapper;

    @InjectMocks
    OrderService orderService;

    private Order buildOrder(OrderStatus status) {
        Order order = Order.builder()
                .id(1L).tableNumber(5).status(status)
                .totalPrice(BigDecimal.ZERO).items(new ArrayList<>()).build();
        return order;
    }

    private OrderItem buildItem(Order order, ItemStatus status) {
        Category cat = Category.builder().id(1L).name("Classiques").build();
        Cocktail cocktail = Cocktail.builder().id(1L).name("Mojito")
                .sizes(new ArrayList<>()).ingredients(new ArrayList<>()).category(cat).build();
        CocktailSize size = CocktailSize.builder().id(1L).size(CocktailSizeEnum.M).price(BigDecimal.valueOf(9.5)).build();
        return OrderItem.builder()
                .id(1L).order(order).cocktail(cocktail).size(size)
                .quantity(1).unitPrice(BigDecimal.valueOf(9.5)).status(status).build();
    }

    @Test
    void getOrdersByTable_retourne_commandes_de_la_table() {
        Order order = buildOrder(OrderStatus.COMMANDEE);
        when(orderRepository.findByTableNumber(5)).thenReturn(List.of(order));
        when(orderMapper.toResponse(order)).thenReturn(new OrderResponse(1L, 5, OrderStatus.COMMANDEE, BigDecimal.ZERO, null, List.of()));

        List<OrderResponse> result = orderService.getOrdersByTable(5);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTableNumber()).isEqualTo(5);
    }

    @Test
    void getPendingOrders_retourne_toutes_les_commandes() {
        Order order = buildOrder(OrderStatus.EN_COURS);
        when(orderRepository.findAll()).thenReturn(List.of(order));

        List<OrderResponse> result = orderService.getPendingOrders();

        assertThat(result).hasSize(1);
    }

    @Test
    void getById_retourne_commande_existante() {
        Order order = buildOrder(OrderStatus.COMMANDEE);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(new OrderResponse(1L, 5, OrderStatus.COMMANDEE, BigDecimal.ZERO, null, List.of()));

        OrderResponse result = orderService.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getById_lance_exception_si_introuvable() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> orderService.getById(99L));
    }

    @Test
    void advanceItemStatus_passe_de_ATTENTE_a_PREPARATION() {
        Order order = buildOrder(OrderStatus.COMMANDEE);
        OrderItem item = buildItem(order, ItemStatus.ATTENTE);
        order.getItems().add(item);

        when(orderItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(orderItemRepository.saveAndFlush(item)).thenReturn(item);
        when(orderItemRepository.countByOrderIdAndStatusNot(1L, ItemStatus.TERMINEE)).thenReturn(1L);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toItemResponse(item)).thenReturn(new OrderItemResponse(1L, "Mojito", CocktailSizeEnum.M, 1, BigDecimal.valueOf(9.5), ItemStatus.PREPARATION_INGREDIENTS));

        OrderItemResponse result = orderService.advanceItemStatus(1L);

        assertThat(result.getStatus()).isEqualTo(ItemStatus.PREPARATION_INGREDIENTS);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.EN_COURS);
    }

    @Test
    void advanceItemStatus_cloture_commande_quand_tous_termines() {
        Order order = buildOrder(OrderStatus.EN_COURS);
        OrderItem item = buildItem(order, ItemStatus.DRESSAGE);
        order.getItems().add(item);

        when(orderItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(orderItemRepository.saveAndFlush(item)).thenReturn(item);
        when(orderItemRepository.countByOrderIdAndStatusNot(1L, ItemStatus.TERMINEE)).thenReturn(0L);
        when(orderRepository.saveAndFlush(any(Order.class))).thenReturn(order);
        when(orderMapper.toItemResponse(item)).thenReturn(new OrderItemResponse(1L, "Mojito", CocktailSizeEnum.M, 1, BigDecimal.valueOf(9.5), ItemStatus.TERMINEE));

        OrderItemResponse result = orderService.advanceItemStatus(1L);

        assertThat(result.getStatus()).isEqualTo(ItemStatus.TERMINEE);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.TERMINEE);
    }

    @Test
    void advanceItemStatus_lance_exception_si_item_introuvable() {
        when(orderItemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> orderService.advanceItemStatus(99L));
    }

    @Test
    void deleteOrder_supprime_commande_terminee() {
        Order order = buildOrder(OrderStatus.TERMINEE);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.deleteOrder(1L);

        verify(orderRepository).delete(order);
    }

    @Test
    void deleteOrder_lance_exception_si_commande_pas_terminee() {
        Order order = buildOrder(OrderStatus.EN_COURS);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () -> orderService.deleteOrder(1L));
        verify(orderRepository, never()).delete(any());
    }

    @Test
    void deleteOrder_lance_exception_si_introuvable() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> orderService.deleteOrder(99L));
    }

    @Test
    void create_sauvegarde_commande_avec_items() {
        Category cat = Category.builder().id(1L).name("Classiques").build();
        Cocktail cocktail = Cocktail.builder().id(1L).name("Mojito")
                .sizes(new ArrayList<>()).ingredients(new ArrayList<>()).category(cat).build();
        CocktailSize size = CocktailSize.builder().id(1L).size(CocktailSizeEnum.M).price(BigDecimal.valueOf(9.5)).build();

        when(cocktailRepository.findById(1L)).thenReturn(Optional.of(cocktail));
        when(cocktailSizeRepository.findById(1L)).thenReturn(Optional.of(size));

        Order savedOrder = Order.builder().id(1L).tableNumber(3).status(OrderStatus.COMMANDEE)
                .totalPrice(BigDecimal.valueOf(9.5)).items(new ArrayList<>()).build();
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderMapper.toResponse(savedOrder)).thenReturn(
                new OrderResponse(1L, 3, OrderStatus.COMMANDEE, BigDecimal.valueOf(9.5), null, List.of()));

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setCocktailId(1L);
        itemRequest.setSizeId(1L);
        itemRequest.setQuantity(1);

        OrderRequest request = new OrderRequest();
        request.setTableNumber(3);
        request.setItems(List.of(itemRequest));

        OrderResponse result = orderService.create(request);

        assertThat(result.getTableNumber()).isEqualTo(3);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void create_lance_exception_si_cocktail_introuvable() {
        when(cocktailRepository.findById(99L)).thenReturn(Optional.empty());

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setCocktailId(99L);
        itemRequest.setSizeId(1L);
        itemRequest.setQuantity(1);

        OrderRequest request = new OrderRequest();
        request.setTableNumber(1);
        request.setItems(List.of(itemRequest));

        assertThrows(EntityNotFoundException.class, () -> orderService.create(request));
    }
}
