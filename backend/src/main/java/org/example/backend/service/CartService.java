package org.example.backend.service;

import org.example.backend.dto.CartDTO;

public interface CartService {

    CartDTO getCartByUserId(Long userId);

    CartDTO addItem(Long userId, Long productId, Integer quantity);

    CartDTO updateItemQuantity(Long userId, Long productId, Integer quantity);

    CartDTO removeItem(Long userId, Long productId);

    void clearCart(Long userId);
}
