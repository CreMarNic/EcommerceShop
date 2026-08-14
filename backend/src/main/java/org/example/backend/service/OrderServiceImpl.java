package org.example.backend.service;

import org.example.backend.dto.OrderDTO;
import org.example.backend.dto.OrderItemDTO;
import org.example.backend.exceptions.APIException;
import org.example.backend.exceptions.ResourceNotFoundException;
import org.example.backend.model.Cart;
import org.example.backend.model.CartItem;
import org.example.backend.model.Order;
import org.example.backend.model.OrderItem;
import org.example.backend.model.OrderStatus;
import org.example.backend.model.Product;
import org.example.backend.repository.CartItemRepository;
import org.example.backend.repository.CartRepository;
import org.example.backend.repository.OrderItemRepository;
import org.example.backend.repository.OrderRepository;
import org.example.backend.repository.ProductRepository;
import org.example.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            CartRepository cartRepository,
            CartItemRepository cartItemRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Override
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public List<OrderDTO> getOrdersByUserId(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return orderRepository.findByUserId(userId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public OrderDTO getOrderById(Long orderId) {
        return toDTO(findOrderById(orderId));
    }

    @Override
    public OrderDTO checkoutCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId);

        if (cart == null) {
            throw new ResourceNotFoundException("Cart", "userId", userId);
        }

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());

        if (cartItems.isEmpty()) {
            throw new APIException("Cannot checkout an empty cart");
        }

        Order order = new Order();
        order.setUser(cart.getUser());
        order.setStatus(OrderStatus.PENDING);
        order = orderRepository.save(order);

        for (CartItem cartItem : cartItems) {
            Long productId = cartItem.getProduct().getId();
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
            Integer quantity = cartItem.getQuantity();

            if (product.getStock() < quantity) {
                throw new APIException("Not enough stock for product: " + product.getName());
            }

            product.setStock(product.getStock() - quantity);
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(quantity);
            orderItem.setPrice(product.getPrice());
            orderItemRepository.save(orderItem);
        }

        cartItemRepository.deleteAll(cartItems);
        return toDTO(order);
    }

    @Override
    public OrderDTO updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = findOrderById(orderId);
        OrderStatus oldStatus = order.getStatus();

        if (oldStatus == OrderStatus.DELIVERED && status != OrderStatus.DELIVERED) {
            throw new APIException("Delivered orders cannot be changed");
        }

        if (oldStatus == OrderStatus.CANCELLED && status != OrderStatus.CANCELLED) {
            throw new APIException("Cancelled orders cannot be changed");
        }

        if (status == OrderStatus.CANCELLED && oldStatus != OrderStatus.CANCELLED) {
            restoreStock(order);
        }

        order.setStatus(status);
        return toDTO(orderRepository.save(order));
    }

    @Override
    public void deleteOrder(Long orderId) {
        Order order = findOrderById(orderId);
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CANCELLED) {
            throw new APIException("Only pending or cancelled orders can be deleted");
        }

        if (order.getStatus() == OrderStatus.PENDING) {
            restoreStock(order);
        }

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
        orderItemRepository.deleteAll(orderItems);
        orderRepository.delete(order);
    }

    private Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
    }

    private void restoreStock(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        for (OrderItem item : items) {
            Long productId = item.getProduct().getId();
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }
    }

    private OrderDTO toDTO(Order order) {
        List<OrderItemDTO> items = orderItemRepository.findByOrderId(order.getId()).stream()
                .map(this::toDTO)
                .toList();
        BigDecimal total = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new OrderDTO(
                order.getId(),
                order.getUser().getId(),
                order.getCreatedAt(),
                order.getStatus(),
                items,
                total
        );
    }

    private OrderItemDTO toDTO(OrderItem orderItem) {
        Long productId = orderItem.getProduct().getId();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        return new OrderItemDTO(
                orderItem.getId(),
                orderItem.getOrder().getId(),
                product.getId(),
                product.getName(),
                product.getImageUrl(),
                orderItem.getQuantity(),
                orderItem.getPrice()
        );
    }
}
