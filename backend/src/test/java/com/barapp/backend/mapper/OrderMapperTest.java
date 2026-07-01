package com.barapp.backend.mapper;

import com.barapp.backend.dto.response.OrderItemResponse;
import com.barapp.backend.dto.response.OrderResponse;
import com.barapp.backend.entity.*;
import com.barapp.backend.enums.CocktailSizeEnum;
import com.barapp.backend.enums.ItemStatus;
import com.barapp.backend.enums.OrderStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class OrderMapperTest {

    private final OrderMapper mapper = new OrderMapper();

    @Test
    void toResponse_convertit_commande_avec_items() {
        Order order = Order.builder()
                .id(1L).tableNumber(3).status(OrderStatus.COMMANDEE)
                .totalPrice(BigDecimal.valueOf(9.5)).items(new ArrayList<>()).build();

        Category cat = Category.builder().id(1L).name("Classiques").build();
        Cocktail cocktail = Cocktail.builder().id(1L).name("Mojito")
                .sizes(new ArrayList<>()).ingredients(new ArrayList<>()).category(cat).build();
        CocktailSize size = CocktailSize.builder().id(1L).size(CocktailSizeEnum.M).price(BigDecimal.valueOf(9.5)).build();
        OrderItem item = OrderItem.builder()
                .id(1L).order(order).cocktail(cocktail).size(size)
                .quantity(1).unitPrice(BigDecimal.valueOf(9.5)).status(ItemStatus.ATTENTE).build();
        order.getItems().add(item);

        OrderResponse response = mapper.toResponse(order);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTableNumber()).isEqualTo(3);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.COMMANDEE);
        assertThat(response.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(9.5));
        assertThat(response.getItems()).hasSize(1);
    }

    @Test
    void toItemResponse_convertit_item() {
        Category cat = Category.builder().id(1L).name("Classiques").build();
        Cocktail cocktail = Cocktail.builder().id(1L).name("Mojito")
                .sizes(new ArrayList<>()).ingredients(new ArrayList<>()).category(cat).build();
        CocktailSize size = CocktailSize.builder().id(1L).size(CocktailSizeEnum.L).price(BigDecimal.valueOf(12)).build();
        Order order = Order.builder().id(1L).tableNumber(1).status(OrderStatus.COMMANDEE)
                .totalPrice(BigDecimal.valueOf(12)).items(new ArrayList<>()).build();
        OrderItem item = OrderItem.builder()
                .id(2L).order(order).cocktail(cocktail).size(size)
                .quantity(2).unitPrice(BigDecimal.valueOf(12)).status(ItemStatus.ASSEMBLAGE).build();

        OrderItemResponse response = mapper.toItemResponse(item);

        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getCocktailName()).isEqualTo("Mojito");
        assertThat(response.getSize()).isEqualTo(CocktailSizeEnum.L);
        assertThat(response.getQuantity()).isEqualTo(2);
        assertThat(response.getStatus()).isEqualTo(ItemStatus.ASSEMBLAGE);
    }

    @Test
    void toResponse_commande_sans_items() {
        Order order = Order.builder()
                .id(2L).tableNumber(5).status(OrderStatus.TERMINEE)
                .totalPrice(BigDecimal.ZERO).items(new ArrayList<>()).build();

        OrderResponse response = mapper.toResponse(order);

        assertThat(response.getItems()).isEmpty();
        assertThat(response.getStatus()).isEqualTo(OrderStatus.TERMINEE);
    }
}
