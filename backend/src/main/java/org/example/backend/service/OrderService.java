package org.example.backend.service;

import org.example.backend.dto.OrderDTO;
import org.example.backend.model.OrderStatus;

import java.util.List;

public interface OrderService {

    List<OrderDTO> getAllOrders();

    List<OrderDTO> getOrdersByUserId(Long userId);

    OrderDTO getOrderById(Long orderId);

    OrderDTO checkoutCart(Long userId);

    OrderDTO updateOrderStatus(Long orderId, OrderStatus status);

    void deleteOrder(Long orderId);
}

