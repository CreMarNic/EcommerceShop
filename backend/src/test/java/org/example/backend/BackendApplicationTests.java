package org.example.backend;

import org.example.backend.dto.CartDTO;
import org.example.backend.dto.OrderDTO;
import org.example.backend.dto.ProductDTO;
import org.example.backend.dto.UserCreateRequest;
import org.example.backend.dto.UserResponse;
import org.example.backend.exceptions.APIException;
import org.example.backend.model.OrderStatus;
import org.example.backend.model.Product;
import org.example.backend.model.User;
import org.example.backend.repository.ProductRepository;
import org.example.backend.repository.UserRepository;
import org.example.backend.service.CartService;
import org.example.backend.service.OrderService;
import org.example.backend.service.ProductService;
import org.example.backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class BackendApplicationTests {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void contextLoads() {
    }

    @Test
    void createUserHashesPasswordAndDoesNotReturnPassword() {
        String email = "test-user-" + System.nanoTime() + "@example.com";
        UserResponse response = userService.createUser(new UserCreateRequest("Test User", email, "secret123"));

        User savedUser = userRepository.findById(response.getId()).orElseThrow();

        assertEquals(email, response.getEmail());
        assertNotEquals("secret123", savedUser.getPassword());
        assertTrue(passwordEncoder.matches("secret123", savedUser.getPassword()));
    }

    @Test
    void productCrudWorks() {
        ProductDTO createdProduct = productService.createProduct(new Product(
                null,
                "Test Product",
                "A product used by integration tests",
                new BigDecimal("12.50"),
                10,
                "images/products/test.jpg",
                4.5,
                7
        ));

        ProductDTO updatedProduct = productService.updateProduct(new Product(
                null,
                "Updated Product",
                "Updated description",
                new BigDecimal("15.00"),
                8,
                "images/products/test.jpg",
                4.0,
                9
        ), createdProduct.getId());

        assertEquals("Updated Product", updatedProduct.getName());
        assertEquals(new BigDecimal("15.00"), updatedProduct.getPrice());
        assertEquals(8, updatedProduct.getStock());
    }

    @Test
    void checkoutCartCreatesOrderReducesStockAndClearsCart() {
        UserResponse user = userService.createUser(new UserCreateRequest(
                "Cart User",
                "cart-user-" + System.nanoTime() + "@example.com",
                "secret123"
        ));
        ProductDTO product = productService.createProduct(new Product(
                null,
                "Checkout Product",
                "Checkout test product",
                new BigDecimal("20.00"),
                5,
                "images/products/test.jpg",
                5.0,
                1
        ));

        cartService.addItem(user.getId(), product.getId(), 2);
        OrderDTO order = orderService.checkoutCart(user.getId());
        CartDTO cart = cartService.getCartByUserId(user.getId());
        ProductDTO savedProduct = productService.getProductById(product.getId());

        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(new BigDecimal("40.00"), order.getTotal());
        assertEquals(3, savedProduct.getStock());
        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void cancellingOrderRestoresStock() {
        UserResponse user = userService.createUser(new UserCreateRequest(
                "Cancel User",
                "cancel-user-" + System.nanoTime() + "@example.com",
                "secret123"
        ));
        ProductDTO product = productService.createProduct(new Product(
                null,
                "Cancel Product",
                "Cancel test product",
                new BigDecimal("5.00"),
                3,
                "images/products/test.jpg",
                3.5,
                2
        ));

        cartService.addItem(user.getId(), product.getId(), 2);
        OrderDTO order = orderService.checkoutCart(user.getId());
        orderService.updateOrderStatus(order.getId(), OrderStatus.CANCELLED);
        ProductDTO savedProduct = productService.getProductById(product.getId());

        assertEquals(3, savedProduct.getStock());
    }

    @Test
    void checkoutFailsWhenStockIsTooLow() {
        UserResponse user = userService.createUser(new UserCreateRequest(
                "Stock User",
                "stock-user-" + System.nanoTime() + "@example.com",
                "secret123"
        ));
        ProductDTO product = productService.createProduct(new Product(
                null,
                "Low Stock Product",
                "Low stock test product",
                new BigDecimal("9.99"),
                1,
                "images/products/test.jpg",
                4.0,
                3
        ));

        cartService.addItem(user.getId(), product.getId(), 2);

        assertThrows(APIException.class, () -> orderService.checkoutCart(user.getId()));
        assertEquals(1, productRepository.findById(product.getId()).orElseThrow().getStock());
    }
}
