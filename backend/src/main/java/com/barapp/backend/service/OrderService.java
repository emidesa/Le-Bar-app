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
import java.util.List;

// Service principal qui gère les commandes côté client et côté barmaker
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final CocktailRepository cocktailRepository;
    private final CocktailSizeRepository cocktailSizeRepository;

    // Création d'une nouvelle commande : je récupère le client, les cocktails et je calcule le total
    public OrderResponse create(Long clientId, OrderRequest request) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        Order order = Order.builder().client(client).build();

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

        order.setStatus(OrderStatus.EN_COURS);
        return toResponse(orderRepository.save(order));
    }

    // Récupère les commandes d'un client pour qu'il puisse suivre leur avancement
    public List<OrderResponse> getClientOrders(Long clientId) {
        return orderRepository.findByClientId(clientId).stream().map(this::toResponse).toList();
    }

    // Récupère toutes les commandes non terminées pour le barmaker
    public List<OrderResponse> getPendingOrders() {
        return orderRepository.findByStatusNot(OrderStatus.TERMINEE).stream().map(this::toResponse).toList();
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
        orderItemRepository.save(item);

        // Je vérifie si tous les items de la commande sont terminés pour clore la commande
        boolean allDone = !orderItemRepository.existsByOrderIdAndStatusNot(item.getOrder().getId(), ItemStatus.TERMINEE);
        if (allDone) {
            Order order = item.getOrder();
            order.setStatus(OrderStatus.TERMINEE);
            orderRepository.save(order);
        }

        return toItemResponse(item);
    }

    // Conversion de l'entité Order en DTO pour la réponse API
    private OrderResponse toResponse(Order o) {
        List<OrderItemResponse> items = o.getItems().stream().map(this::toItemResponse).toList();
        return new OrderResponse(o.getId(), o.getStatus(), o.getTotalPrice(), o.getCreatedAt(), items);
    }

    // Conversion d'un item de commande en DTO
    private OrderItemResponse toItemResponse(OrderItem i) {
        return new OrderItemResponse(i.getId(), i.getCocktail().getName(), i.getSize().getSize(), i.getQuantity(), i.getUnitPrice(), i.getStatus());
    }
}