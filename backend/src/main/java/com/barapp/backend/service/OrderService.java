package com.barapp.backend.service;

import com.barapp.backend.dto.request.OrderRequest;
import com.barapp.backend.dto.response.*;
import com.barapp.backend.entity.*;
import com.barapp.backend.enums.ItemStatus;
import com.barapp.backend.enums.OrderStatus;
import com.barapp.backend.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

// Service principal qui gère les commandes côté client (QR code) et côté barmaker
@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CocktailRepository cocktailRepository;
    private final CocktailSizeRepository cocktailSizeRepository;

    // Création d'une nouvelle commande depuis le numéro de table scanné via QR code
    public OrderResponse create(OrderRequest request) {
        Order order = Order.builder().tableNumber(request.getTableNumber()).build();

        // Pour chaque item du panier, je sauvegarde le prix unitaire au moment de la commande
        request.getItems().forEach(i -> {
            Cocktail cocktail = cocktailRepository.findById(i.getCocktailId())
                    .orElseThrow(() -> new EntityNotFoundException("Cocktail non trouvé"));
            CocktailSize size = cocktailSizeRepository.findById(i.getSizeId())
                    .orElseThrow(() -> new EntityNotFoundException("Taille non trouvée"));

            OrderItem item = OrderItem.builder()
                    .order(order)
                    .cocktail(cocktail)
                    .size(size)
                    .quantity(i.getQuantity())
                    .unitPrice(size.getPrice())
                    .build();
            order.getItems().add(item);
            // Calcul du prix total : prix unitaire x quantité pour chaque item
            order.setTotalPrice(order.getTotalPrice().add(size.getPrice().multiply(java.math.BigDecimal.valueOf(i.getQuantity()))));
        });

        return toResponse(orderRepository.save(order));
    }

    // Récupère les commandes d'une table pour que le client puisse suivre leur avancement
    public List<OrderResponse> getOrdersByTable(Integer tableNumber) {
        return orderRepository.findByTableNumber(tableNumber).stream().map(this::toResponse).toList();
    }

    // Récupère toutes les commandes pour le barmaker (y compris les terminées)
    public List<OrderResponse> getPendingOrders() {
        return orderRepository.findAll().stream().map(this::toResponse).toList();
    }

    // Détail d'une commande spécifique
    public OrderResponse getById(Long id) {
        return toResponse(orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Commande non trouvée")));
    }

    // Le barmaker fait avancer un cocktail à l'étape suivante
    // Si tous les cocktails sont terminés, la commande globale passe en TERMINEE
    public OrderItemResponse advanceItemStatus(Long itemId) {
        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item non trouvé"));

        // Progression des étapes dans l'ordre défini par le sujet
        ItemStatus next = switch (item.getStatus()) {
            case ATTENTE -> ItemStatus.PREPARATION_INGREDIENTS;
            case PREPARATION_INGREDIENTS -> ItemStatus.ASSEMBLAGE;
            case ASSEMBLAGE -> ItemStatus.DRESSAGE;
            case DRESSAGE -> ItemStatus.TERMINEE;
            case TERMINEE -> ItemStatus.TERMINEE;
        };
        item.setStatus(next);
        orderItemRepository.saveAndFlush(item); // flush immédiat pour que la requête suivante voie le nouveau statut

        // Dès que le barmaker touche un item, la commande passe EN_COURS
        Order order = item.getOrder();
        if (order.getStatus() == OrderStatus.COMMANDEE) {
            order.setStatus(OrderStatus.EN_COURS);
            orderRepository.save(order);
        }

        // Je vérifie si tous les items de la commande sont terminés pour clore la commande
        long remaining = orderItemRepository.countByOrderIdAndStatusNot(order.getId(), ItemStatus.TERMINEE);
        if (remaining == 0) {
            order.setStatus(OrderStatus.TERMINEE);
            orderRepository.saveAndFlush(order);
        }

        return toItemResponse(item);
    }

    // Conversion de l'entité Order en DTO pour la réponse API
    private OrderResponse toResponse(Order o) {
        List<OrderItemResponse> items = o.getItems().stream().map(this::toItemResponse).toList();
        return new OrderResponse(o.getId(), o.getTableNumber(), o.getStatus(), o.getTotalPrice(), o.getCreatedAt(), items);
    }

    // Conversion d'un item de commande en DTO
    private OrderItemResponse toItemResponse(OrderItem i) {
        return new OrderItemResponse(i.getId(), i.getCocktail().getName(), i.getSize().getSize(), i.getQuantity(), i.getUnitPrice(), i.getStatus());
    }
}