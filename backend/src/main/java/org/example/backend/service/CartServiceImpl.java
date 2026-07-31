package org.example.backend.service;

import org.example.backend.dto.CartDTO;
import org.example.backend.dto.CartItemDTO;
import org.example.backend.exceptions.APIException;
import org.example.backend.exceptions.ResourceNotFoundException;
import org.example.backend.model.Cart;
import org.example.backend.model.CartItem;
import org.example.backend.model.Product;
import org.example.backend.model.User;
import org.example.backend.repository.CartItemRepository;
import org.example.backend.repository.CartRepository;
import org.example.backend.repository.ProductRepository;
import org.example.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartServiceImpl(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Override
    public CartDTO getCartByUserId(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return toDTO(cart);
    }

    @Override
    public CartDTO addItem(Long userId, Long productId, Integer quantity) {
        validateQuantity(quantity);
        Cart cart = getOrCreateCart(userId);
        Product product = getProduct(productId);

        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId);

        if (cartItem == null) {
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(0);
        }

        cartItem.setQuantity(cartItem.getQuantity() + quantity);
        cartItemRepository.save(cartItem);

        return toDTO(cart);
    }

    @Override
    public CartDTO updateItemQuantity(Long userId, Long productId, Integer quantity) {
        validateQuantity(quantity);
        Cart cart = getOrCreateCart(userId);
        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId);

        if (cartItem == null) {
            throw new ResourceNotFoundException("Cart item", "productId", productId);
        }

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);

        return toDTO(cart);
    }

    @Override
    public CartDTO removeItem(Long userId, Long productId) {
        Cart cart = getOrCreateCart(userId);
        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId);

        if (cartItem == null) {
            throw new ResourceNotFoundException("Cart item", "productId", productId);
        }

        cartItemRepository.delete(cartItem);
        return toDTO(cart);
    }

    @Override
    public void clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cartItemRepository.deleteByCartId(cart.getId());
    }

    private Cart getOrCreateCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId);

        if (cart != null) {
            return cart;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return cartRepository.save(new Cart(null, user));
    }

    private Product getProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new APIException("Quantity must be at least 1");
        }
    }

    private CartDTO toDTO(Cart cart) {
        List<CartItemDTO> items = cartItemRepository.findByCartId(cart.getId()).stream()
                .map(this::toDTO)
                .toList();

        BigDecimal total = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartDTO(cart.getId(), cart.getUser().getId(), items, total);
    }

    private CartItemDTO toDTO(CartItem cartItem) {
        Product product = cartItem.getProduct();
        return new CartItemDTO(
                cartItem.getId(),
                product.getId(),
                product.getName(),
                product.getImageUrl(),
                cartItem.getQuantity(),
                product.getPrice()
        );
    }
}
