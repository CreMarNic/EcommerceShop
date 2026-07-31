package org.example.backend;

import org.example.backend.controller.CartController;
import org.example.backend.controller.OrderController;
import org.example.backend.controller.ProductController;
import org.example.backend.controller.UserController;
import org.example.backend.dto.AddCartItemRequest;
import org.example.backend.dto.CartDTO;
import org.example.backend.dto.OrderDTO;
import org.example.backend.dto.ProductDTO;
import org.example.backend.dto.UserCreateRequest;
import org.example.backend.dto.UserResponse;
import org.example.backend.exceptions.APIException;
import org.example.backend.exceptions.GlobalExceptionHandler;
import org.example.backend.exceptions.ResourceNotFoundException;
import org.example.backend.model.Order;
import org.example.backend.model.OrderStatus;
import org.example.backend.model.Product;
import org.example.backend.model.User;
import org.example.backend.repository.OrderRepository;
import org.example.backend.repository.ProductRepository;
import org.example.backend.repository.UserRepository;
import org.example.backend.service.CartService;
import org.example.backend.service.OrderService;
import org.example.backend.service.ProductService;
import org.example.backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
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
    private OrderRepository orderRepository;

    @Autowired
    private ProductController productController;

    @Autowired
    private UserController userController;

    @Autowired
    private CartController cartController;

    @Autowired
    private OrderController orderController;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void contextLoads() {
    }

    @Test
    void createUserHashesPasswordAndDoesNotReturnPassword() {
        String email = "test-user-" + System.nanoTime() + "@example.com";
        UserCreateRequest request = new UserCreateRequest("Test User", email, "secret123");

        UserResponse response = userService.createUser(request);

        User savedUser = userRepository.findById(response.getId()).orElseThrow();

        assertEquals(email, response.getEmail());
        assertNotEquals("secret123", savedUser.getPassword());
        assertTrue(passwordEncoder.matches("secret123", savedUser.getPassword()));
    }

    @Test
    void productCrudWorks() {
        Product product = createProduct("Test Product", "12.50", 10);

        ProductDTO createdProduct = productService.createProduct(product);

        Product updatedProductRequest = createProduct("Updated Product", "15.00", 8);
        ProductDTO updatedProduct = productService.updateProduct(updatedProductRequest, createdProduct.getId());

        assertTrue(updatedProduct.getName().startsWith("Updated Product"));
        assertEquals(new BigDecimal("15.00"), updatedProduct.getPrice());
        assertEquals(8, updatedProduct.getStock());
    }

    @Test
    void checkoutCartCreatesOrderReducesStockAndClearsCart() {
        UserResponse user = createTestUser("Cart User");
        ProductDTO product = productService.createProduct(createProduct("Checkout Product", "20.00", 5));

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
        UserResponse user = createTestUser("Cancel User");
        ProductDTO product = productService.createProduct(createProduct("Cancel Product", "5.00", 3));

        cartService.addItem(user.getId(), product.getId(), 2);
        OrderDTO order = orderService.checkoutCart(user.getId());

        orderService.updateOrderStatus(order.getId(), OrderStatus.CANCELLED);

        ProductDTO savedProduct = productService.getProductById(product.getId());

        assertEquals(3, savedProduct.getStock());
    }

    @Test
    void checkoutFailsWhenStockIsTooLow() {
        UserResponse user = createTestUser("Stock User");
        ProductDTO product = productService.createProduct(createProduct("Low Stock Product", "9.99", 1));

        cartService.addItem(user.getId(), product.getId(), 2);

        assertThrows(APIException.class, () -> orderService.checkoutCart(user.getId()));
        assertEquals(1, productRepository.findById(product.getId()).orElseThrow().getStock());
    }

    @Test
    void userCrudAndDuplicateEmailValidationWorks() {
        String email = "user-crud-" + System.nanoTime() + "@example.com";
        UserCreateRequest createRequest = new UserCreateRequest("User Crud", email, "secret123");

        UserResponse createdUser = userService.createUser(createRequest);

        UserResponse foundUser = userService.getUserById(createdUser.getId());
        List<UserResponse> users = userService.getAllUsers();

        assertEquals("User Crud", foundUser.getName());
        assertTrue(userListContainsId(users, createdUser.getId()));

        String updatedEmail = "updated-user-crud-" + System.nanoTime() + "@example.com";
        UserCreateRequest updateRequest = new UserCreateRequest("Updated User", updatedEmail, "newSecret123");

        UserResponse updatedUser = userService.updateUser(updateRequest, createdUser.getId());

        assertEquals("Updated User", updatedUser.getName());
        assertEquals(updatedEmail, updatedUser.getEmail());
        assertThrows(APIException.class, () -> userService.createUser(
                new UserCreateRequest("Duplicate User", updatedEmail, "secret123")
        ));

        UserResponse secondUser = createTestUser("Second User");

        assertThrows(APIException.class, () -> userService.updateUser(
                new UserCreateRequest("Bad Update", updatedEmail, "secret123"),
                secondUser.getId()
        ));

        userService.deleteUser(secondUser.getId());
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(secondUser.getId()));
    }

    @Test
    void productReadDeleteAndMissingProductWork() {
        Product productRequest = createProduct("Read Delete Product", "11.25", 12);

        ProductDTO product = productService.createProduct(productRequest);

        ProductDTO foundProduct = productService.getProductById(product.getId());
        List<ProductDTO> products = productService.getAllProducts();

        assertEquals(product.getId(), foundProduct.getId());
        assertTrue(productListContainsId(products, product.getId()));

        productService.deleteProduct(product.getId());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(product.getId()));
        assertThrows(ResourceNotFoundException.class, () -> productService.deleteProduct(product.getId()));
    }

    @Test
    void cartAddUpdateRemoveClearAndValidationWork() {
        UserResponse user = createTestUser("Cart Crud User");
        ProductDTO product = productService.createProduct(createProduct("Cart Crud Product", "11.25", 20));

        CartDTO emptyCart = cartService.getCartByUserId(user.getId());
        CartDTO cartWithItem = cartService.addItem(user.getId(), product.getId(), 2);
        CartDTO cartWithMoreItems = cartService.addItem(user.getId(), product.getId(), 3);
        CartDTO updatedCart = cartService.updateItemQuantity(user.getId(), product.getId(), 4);
        CartDTO cartAfterRemove = cartService.removeItem(user.getId(), product.getId());

        assertNotNull(emptyCart.getId());
        assertEquals(1, cartWithItem.getItems().size());
        assertEquals(5, cartWithMoreItems.getItems().get(0).getQuantity());
        assertEquals(4, updatedCart.getItems().get(0).getQuantity());
        assertTrue(cartAfterRemove.getItems().isEmpty());

        cartService.addItem(user.getId(), product.getId(), 1);
        cartService.clearCart(user.getId());

        assertTrue(cartService.getCartByUserId(user.getId()).getItems().isEmpty());
        assertThrows(APIException.class, () -> cartService.addItem(user.getId(), product.getId(), 0));
        assertThrows(APIException.class, () -> cartService.updateItemQuantity(user.getId(), product.getId(), null));
        assertThrows(ResourceNotFoundException.class, () -> cartService.updateItemQuantity(user.getId(), product.getId(), 1));
        assertThrows(ResourceNotFoundException.class, () -> cartService.removeItem(user.getId(), product.getId()));
        assertThrows(ResourceNotFoundException.class, () -> cartService.getCartByUserId(-999999L));
        assertThrows(ResourceNotFoundException.class, () -> cartService.addItem(user.getId(), -999999L, 1));
    }

    @Test
    void orderQueriesStatusChangesAndErrorsWork() {
        UserResponse user = createTestUser("Order User");
        ProductDTO product = productService.createProduct(createProduct("Order Product", "11.25", 10));

        assertThrows(ResourceNotFoundException.class, () -> orderService.checkoutCart(user.getId()));

        cartService.getCartByUserId(user.getId());
        assertThrows(APIException.class, () -> orderService.checkoutCart(user.getId()));

        cartService.addItem(user.getId(), product.getId(), 2);
        OrderDTO order = orderService.checkoutCart(user.getId());

        assertEquals(order.getId(), orderService.getOrderById(order.getId()).getId());
        assertTrue(orderListContainsId(orderService.getAllOrders(), order.getId()));
        assertTrue(orderListContainsId(orderService.getOrdersByUserId(user.getId()), order.getId()));

        OrderDTO paidOrder = orderService.updateOrderStatus(order.getId(), OrderStatus.PAID);
        assertEquals(OrderStatus.PAID, paidOrder.getStatus());
        assertThrows(APIException.class, () -> orderService.deleteOrder(order.getId()));

        OrderDTO deliveredOrder = orderService.updateOrderStatus(order.getId(), OrderStatus.DELIVERED);
        assertEquals(OrderStatus.DELIVERED, deliveredOrder.getStatus());
        assertThrows(APIException.class, () -> orderService.updateOrderStatus(order.getId(), OrderStatus.PENDING));

        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderById(-999999L));
        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrdersByUserId(-999999L));

        User savedUser = userRepository.findById(user.getId()).orElseThrow();
        Order emptyOrder = new Order();
        emptyOrder.setUser(savedUser);
        emptyOrder.setStatus(OrderStatus.PENDING);
        emptyOrder = orderRepository.save(emptyOrder);

        orderService.deleteOrder(emptyOrder.getId());

        Long deletedOrderId = emptyOrder.getId();
        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderById(deletedOrderId));
    }

    @Test
    void controllersReturnExpectedHttpStatuses() {
        UserCreateRequest userRequest = createUserRequest("Controller User");
        ResponseEntity<UserResponse> createdUserResponse = userController.createUser(userRequest);
        UserResponse user = createdUserResponse.getBody();

        assertEquals(HttpStatus.CREATED, createdUserResponse.getStatusCode());
        assertNotNull(user);
        assertEquals(HttpStatus.OK, userController.getAllUsers().getStatusCode());
        assertEquals(HttpStatus.OK, userController.getUserById(user.getId()).getStatusCode());
        assertEquals(HttpStatus.OK, userController.updateUser(
                new UserCreateRequest("Controller User Updated", user.getEmail(), "secret456"),
                user.getId()
        ).getStatusCode());

        Product product = createProduct("Controller Product", "11.25", 7);
        ResponseEntity<ProductDTO> createdProductResponse = productController.createProduct(product);
        ProductDTO createdProduct = createdProductResponse.getBody();

        assertEquals(HttpStatus.CREATED, createdProductResponse.getStatusCode());
        assertNotNull(createdProduct);
        assertEquals(HttpStatus.OK, productController.getAllProducts().getStatusCode());
        assertEquals(HttpStatus.OK, productController.getProductById(createdProduct.getId()).getStatusCode());
        assertEquals(HttpStatus.OK, productController.updateProduct(
                createProduct("Controller Product Updated", "11.25", 6),
                createdProduct.getId()
        ).getStatusCode());

        AddCartItemRequest cartItemRequest = new AddCartItemRequest(createdProduct.getId(), 2);
        assertEquals(HttpStatus.OK, cartController.getCart(user.getId()).getStatusCode());
        assertEquals(HttpStatus.CREATED, cartController.addItem(user.getId(), cartItemRequest).getStatusCode());
        assertEquals(HttpStatus.OK, cartController.updateItem(user.getId(), new AddCartItemRequest(createdProduct.getId(), 1)).getStatusCode());
        assertEquals(HttpStatus.OK, cartController.removeItem(user.getId(), createdProduct.getId()).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, cartController.clearCart(user.getId()).getStatusCode());

        cartController.addItem(user.getId(), new AddCartItemRequest(createdProduct.getId(), 1));
        ResponseEntity<OrderDTO> checkoutResponse = orderController.checkoutCart(user.getId());
        OrderDTO order = checkoutResponse.getBody();

        assertEquals(HttpStatus.CREATED, checkoutResponse.getStatusCode());
        assertNotNull(order);
        assertEquals(HttpStatus.OK, orderController.getAllOrders().getStatusCode());
        assertEquals(HttpStatus.OK, orderController.getOrderById(order.getId()).getStatusCode());
        assertEquals(HttpStatus.OK, orderController.getOrdersByUserId(user.getId()).getStatusCode());
        assertEquals(HttpStatus.OK, orderController.updateOrderStatus(order.getId(), OrderStatus.CANCELLED).getStatusCode());

        UserResponse deleteUser = createTestUser("Delete Controller User");
        ProductDTO deleteProduct = productService.createProduct(createProduct("Delete Controller Product", "11.25", 3));

        assertEquals(HttpStatus.NO_CONTENT, userController.deleteUser(deleteUser.getId()).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, productController.deleteProduct(deleteProduct.getId()).getStatusCode());
    }

    @Test
    void globalExceptionHandlerCreatesSimpleErrorResponses() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<Map<String, Object>> notFoundResponse = handler.handleResourceNotFoundException(
                new ResourceNotFoundException("Product", "id", 123L)
        );
        ResponseEntity<Map<String, Object>> apiResponse = handler.handleAPIException(
                new APIException("Something went wrong")
        );

        assertEquals(HttpStatus.NOT_FOUND, notFoundResponse.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, apiResponse.getStatusCode());
        assertFalse((Boolean) notFoundResponse.getBody().get("status"));
        assertEquals("Something went wrong", apiResponse.getBody().get("message"));
    }

    private UserResponse createTestUser(String name) {
        UserCreateRequest request = createUserRequest(name);
        return userService.createUser(request);
    }

    private UserCreateRequest createUserRequest(String name) {
        String email = name.toLowerCase().replace(" ", "-") + "-" + System.nanoTime() + "@example.com";
        return new UserCreateRequest(name, email, "secret123");
    }

    private Product createProduct(String name, String price, Integer stock) {
        return new Product(
                null,
                name + " " + System.nanoTime(),
                "Test product description",
                new BigDecimal(price),
                stock,
                "images/products/test.jpg"
        );
    }

    private boolean userListContainsId(List<UserResponse> users, Long userId) {
        for (UserResponse user : users) {
            if (user.getId().equals(userId)) {
                return true;
            }
        }

        return false;
    }

    private boolean productListContainsId(List<ProductDTO> products, Long productId) {
        for (ProductDTO product : products) {
            if (product.getId().equals(productId)) {
                return true;
            }
        }

        return false;
    }

    private boolean orderListContainsId(List<OrderDTO> orders, Long orderId) {
        for (OrderDTO order : orders) {
            if (order.getId().equals(orderId)) {
                return true;
            }
        }

        return false;
    }
}
