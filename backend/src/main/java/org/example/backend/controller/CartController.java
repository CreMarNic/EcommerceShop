package org.example.backend.controller;

import jakarta.validation.Valid;
import org.example.backend.dto.AddCartItemRequest;
import org.example.backend.dto.CartDTO;
import org.example.backend.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartDTO> getCart(@PathVariable Long userId) {
        return new ResponseEntity<>(cartService.getCartByUserId(userId), HttpStatus.OK);
    }

    @PostMapping("/items")
    public ResponseEntity<CartDTO> addItem(
            @PathVariable Long userId,
            @Valid @RequestBody AddCartItemRequest request
    ) {
        CartDTO cart = cartService.addItem(userId, request.getProductId(), request.getQuantity());
        return new ResponseEntity<>(cart, HttpStatus.CREATED);
    }

    @PutMapping("/items")
    public ResponseEntity<CartDTO> updateItem(
            @PathVariable Long userId,
            @Valid @RequestBody AddCartItemRequest request
    ) {
        return new ResponseEntity<>(
                cartService.updateItemQuantity(userId, request.getProductId(), request.getQuantity()),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartDTO> removeItem(@PathVariable Long userId, @PathVariable Long productId) {
        return new ResponseEntity<>(cartService.removeItem(userId, productId), HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(@PathVariable Long userId) {
        cartService.clearCart(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
