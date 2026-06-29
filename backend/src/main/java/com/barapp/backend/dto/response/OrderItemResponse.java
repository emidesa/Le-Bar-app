package com.barapp.backend.dto.response;

import java.math.BigDecimal;

import com.barapp.backend.enums.CocktailSizeEnum;
import com.barapp.backend.enums.ItemStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderItemResponse {
    private Long id;
    private String cocktailName;
    private CocktailSizeEnum size;
    private int quantity;
    private BigDecimal unitPrice;
    private ItemStatus status;
}