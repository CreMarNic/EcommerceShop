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


@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:ecommerce-test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false"
})
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
    void createUserHashesPasswordAndDoesNotReturnPassword() {

        // given: a request to create a new user.

        String email = "test-user-" + uniqueTestValue() + "@example.com";
        UserCreateRequest request = new UserCreateRequest("Test User", email, "secret123");

        // when: create a user using the real UserService.

        UserResponse response = userService.createUser(request);

        // Read the saved user directly from the database so we can inspect the stored password.
        User savedUser = userRepository.findById(response.getId()).orElseThrow();

        // then: the response has the same email we used.

        assertEquals(email, response.getEmail());

        // then: the stored password is not the plain text password.

        assertNotEquals("secret123", savedUser.getPassword());

        // then: the password encoder can still match "secret123" against the hashed password.

        assertTrue(passwordEncoder.matches("secret123", savedUser.getPassword()));
    }

    @Test
    void productCrudWorks() {

        // given: a new product request.

        Product product = createProduct("Test Product", "12.50", 10);

        // when: create a product.

        ProductDTO createdProduct = productService.createProduct(product);

        // when: update the product we just created.

        Product updatedProductRequest = createProduct("Updated Product", "15.00", 8);
        ProductDTO updatedProduct = productService.updateProduct(updatedProductRequest, createdProduct.getId());

        // then: the product should have the new values.

        assertTrue(updatedProduct.getName().startsWith("Updated Product"));
        assertEquals(new BigDecimal("15.00"), updatedProduct.getPrice());
        assertEquals(8, updatedProduct.getStock());
    }

    @Test
    void checkoutCartCreatesOrderReducesStockAndClearsCart() {

        //  given: a user, a product with stock, and the product in the user's cart.

        UserResponse user = createTestUser("Cart User");
        ProductDTO product = productService.createProduct(createProduct("Checkout Product", "20.00", 5));

        cartService.addItem(user.getId(), product.getId(), 2);

        // when: the user checks out the cart.

        OrderDTO order = orderService.checkoutCart(user.getId());
        CartDTO cart = cartService.getCartByUserId(user.getId());
        ProductDTO savedProduct = productService.getProductById(product.getId());

        // then: an order should be created with the expected status and total.

        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(new BigDecimal("40.00"), order.getTotal());

        // then: product started with 5 in stock, user bought 2, so remaining stock should be 3.

        assertEquals(3, savedProduct.getStock());

        // then: after checkout, the cart should not still contain old items.

        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void cancellingOrderRestoresStock() {

        // given: a user has checked out an order for 2 products.

        UserResponse user = createTestUser("Cancel User");
        ProductDTO product = productService.createProduct(createProduct("Cancel Product", "5.00", 3));

        cartService.addItem(user.getId(), product.getId(), 2);
        OrderDTO order = orderService.checkoutCart(user.getId());

        // when: the order is cancelled.

        orderService.updateOrderStatus(order.getId(), OrderStatus.CANCELLED);

        ProductDTO savedProduct = productService.getProductById(product.getId());

        // then: product started with 3, checkout used 2, and cancellation adds those 2 back.

        assertEquals(3, savedProduct.getStock());
    }

    @Test
    void deletingPendingOrderDeletesOrderItemsAndRestoresStock() {

        // given: a user has checked out an order, so the order has order items in the database.

        UserResponse user = createTestUser("Delete Order User");
        ProductDTO product = productService.createProduct(createProduct("Delete Order Product", "6.50", 4));

        cartService.addItem(user.getId(), product.getId(), 2);
        OrderDTO order = orderService.checkoutCart(user.getId());

        // when: the pending order is deleted.

        orderService.deleteOrder(order.getId());

        ProductDTO savedProduct = productService.getProductById(product.getId());

        // then: the order should be gone.

        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderById(order.getId()));

        // then: product started with 4, checkout used 2, and deleting the pending order adds those 2 back.

        assertEquals(4, savedProduct.getStock());
    }

    @Test
    void checkoutFailsWhenStockIsTooLow() {

        // given: the product has only 1 item in stock, but the user tries to buy 2.

        UserResponse user = createTestUser("Stock User");
        ProductDTO product = productService.createProduct(createProduct("Low Stock Product", "9.99", 1));

        cartService.addItem(user.getId(), product.getId(), 2);

        // when / then: checkout should fail with APIException.

        assertThrows(APIException.class, () -> orderService.checkoutCart(user.getId()));

        // then: stock should still be 1 because checkout failed.

        assertEquals(1, productRepository.findById(product.getId()).orElseThrow().getStock());
    }

    @Test
    void userCrudAndDuplicateEmailValidationWorks() {
        /*
         * given: a new user request.
         *
         * This test checks:
         * - Creating a user.
         * - Finding a user by ID.
         * - Listing users.
         * - Updating a user.
         * - Preventing duplicate emails.
         * - Deleting a user.
         */
        String email = "user-crud-" + uniqueTestValue() + "@example.com";
        UserCreateRequest createRequest = new UserCreateRequest("User Crud", email, "secret123");

        // when: create the user, find the user by ID, and list all users.
        UserResponse createdUser = userService.createUser(createRequest);

        UserResponse foundUser = userService.getUserById(createdUser.getId());
        List<UserResponse> users = userService.getAllUsers();

        // then: the created user should be found.
        assertEquals("User Crud", foundUser.getName());
        assertTrue(userListContainsId(users, createdUser.getId()));

        // when: update the user with a new name and email.
        String updatedEmail = "updated-user-crud-" + uniqueTestValue() + "@example.com";
        UserCreateRequest updateRequest = new UserCreateRequest("Updated User", updatedEmail, "newSecret123");

        UserResponse updatedUser = userService.updateUser(updateRequest, createdUser.getId());

        // then: the user should have the updated values.
        assertEquals("Updated User", updatedUser.getName());
        assertEquals(updatedEmail, updatedUser.getEmail());

        // when / then: creating another user with the same email should fail.
        assertThrows(APIException.class, () -> userService.createUser(
                new UserCreateRequest("Duplicate User", updatedEmail, "secret123")
        ));

        UserResponse secondUser = createTestUser("Second User");

        // when / then: updating another user to use an email that already exists should also fail.
        assertThrows(APIException.class, () -> userService.updateUser(
                new UserCreateRequest("Bad Update", updatedEmail, "secret123"),
                secondUser.getId()
        ));

        // when: delete the second user.
        userService.deleteUser(secondUser.getId());

        // then: after deleting the second user, trying to find that user should fail.
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(secondUser.getId()));
    }

    @Test
    void productReadDeleteAndMissingProductWork() {
        /*
         * given: a new product request.
         *
         * This test checks:
         * - Creating a product.
         * - Reading one product by ID.
         * - Finding the product in the full product list.
         * - Deleting the product.
         * - Making sure deleted or missing products throw ResourceNotFoundException.
        */
        Product productRequest = createProduct("Read Delete Product", "11.25", 12);

        // when: create the product, find it by ID, and list all products.
        ProductDTO product = productService.createProduct(productRequest);

        ProductDTO foundProduct = productService.getProductById(product.getId());
        List<ProductDTO> products = productService.getAllProducts();

        // then: the created product should be found.
        assertEquals(product.getId(), foundProduct.getId());
        assertTrue(productListContainsId(products, product.getId()));

        // when: delete the product.
        productService.deleteProduct(product.getId());

        // then: after delete, the product should not be found anymore.
        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(product.getId()));
        assertThrows(ResourceNotFoundException.class, () -> productService.deleteProduct(product.getId()));
    }

    @Test
    void cartAddUpdateRemoveClearAndValidationWork() {
        /*
         * given: a user and a product.
         *
         * This test checks all cart actions:
         * - Get an empty cart.
         * - Add a product.
         * - Add the same product again.
         * - Update quantity.
         * - Remove product.
         * - Clear cart.
         * - Check invalid quantities and missing records.
         */
        UserResponse user = createTestUser("Cart Crud User");
        ProductDTO product = productService.createProduct(createProduct("Cart Crud Product", "11.25", 20));

        // when: use the normal cart actions.
        CartDTO emptyCart = cartService.getCartByUserId(user.getId());
        CartDTO cartWithItem = cartService.addItem(user.getId(), product.getId(), 2);
        CartDTO cartWithMoreItems = cartService.addItem(user.getId(), product.getId(), 3);
        CartDTO updatedCart = cartService.updateItemQuantity(user.getId(), product.getId(), 4);
        CartDTO cartAfterRemove = cartService.removeItem(user.getId(), product.getId());

        // then: each cart action should return the expected cart data.
        assertNotNull(emptyCart.getId());
        assertEquals(1, cartWithItem.getItems().size());
        assertEquals(5, cartWithMoreItems.getItems().get(0).getQuantity());
        assertEquals(4, updatedCart.getItems().get(0).getQuantity());
        assertTrue(cartAfterRemove.getItems().isEmpty());

        // when: add one item again, then clear the whole cart.
        cartService.addItem(user.getId(), product.getId(), 1);
        cartService.clearCart(user.getId());

        // then: the cart should be empty.
        assertTrue(cartService.getCartByUserId(user.getId()).getItems().isEmpty());

        // when / then: quantity must be at least 1.
        assertThrows(APIException.class, () -> cartService.addItem(user.getId(), product.getId(), 0));
        assertThrows(APIException.class, () -> cartService.updateItemQuantity(user.getId(), product.getId(), null));

        // when / then: the cart item was removed, so updating/removing it again should fail.
        assertThrows(ResourceNotFoundException.class, () -> cartService.updateItemQuantity(user.getId(), product.getId(), 1));
        assertThrows(ResourceNotFoundException.class, () -> cartService.removeItem(user.getId(), product.getId()));

        // when / then: fake IDs should fail because the user or product does not exist.
        assertThrows(ResourceNotFoundException.class, () -> cartService.getCartByUserId(-999999L));
        assertThrows(ResourceNotFoundException.class, () -> cartService.addItem(user.getId(), -999999L, 1));
    }

    @Test
    void orderQueriesStatusChangesAndErrorsWork() {
        /*
         * given: a user and a product.
         *
         * This test checks several order rules:
         * - Checkout needs an existing cart.
         * - Checkout cannot happen with an empty cart.
         * - Orders can be found by ID.
         * - Orders can be listed.
         * - Status can change from PENDING to PAID to DELIVERED.
         * - Delivered orders cannot be changed back.
         * - Only pending or cancelled orders can be deleted.
         */
        UserResponse user = createTestUser("Order User");
        ProductDTO product = productService.createProduct(createProduct("Order Product", "11.25", 10));

        // when / then: user exists, but no cart exists yet, so checkout should fail.
        assertThrows(ResourceNotFoundException.class, () -> orderService.checkoutCart(user.getId()));

        // when: create an empty cart.
        cartService.getCartByUserId(user.getId());

        // then: checkout should still fail because the cart has no items.
        assertThrows(APIException.class, () -> orderService.checkoutCart(user.getId()));

        // when: add the product to the cart and check out.
        cartService.addItem(user.getId(), product.getId(), 2);
        OrderDTO order = orderService.checkoutCart(user.getId());

        // then: the order should be found by ID and in order lists.
        assertEquals(order.getId(), orderService.getOrderById(order.getId()).getId());
        assertTrue(orderListContainsId(orderService.getAllOrders(), order.getId()));
        assertTrue(orderListContainsId(orderService.getOrdersByUserId(user.getId()), order.getId()));

        // when: update the order status to PAID.
        OrderDTO paidOrder = orderService.updateOrderStatus(order.getId(), OrderStatus.PAID);

        // then: the order status should be PAID.
        assertEquals(OrderStatus.PAID, paidOrder.getStatus());

        // when / then: PAID orders should not be deleted.
        assertThrows(APIException.class, () -> orderService.deleteOrder(order.getId()));

        // when: update the order status to DELIVERED.
        OrderDTO deliveredOrder = orderService.updateOrderStatus(order.getId(), OrderStatus.DELIVERED);

        // then: the order status should be DELIVERED.
        assertEquals(OrderStatus.DELIVERED, deliveredOrder.getStatus());

        // when / then: DELIVERED orders should not be changed back to PENDING.
        assertThrows(APIException.class, () -> orderService.updateOrderStatus(order.getId(), OrderStatus.PENDING));

        // when / then: fake IDs should produce ResourceNotFoundException.
        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderById(-999999L));
        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrdersByUserId(-999999L));

        /*
         * given: a simple empty order saved directly with the repository.
         * This lets us test the successful delete path without creating more cart/order items.
         */
        User savedUser = userRepository.findById(user.getId()).orElseThrow();
        Order emptyOrder = new Order();
        emptyOrder.setUser(savedUser);
        emptyOrder.setStatus(OrderStatus.PENDING);
        emptyOrder = orderRepository.save(emptyOrder);

        // when: delete the pending order.
        orderService.deleteOrder(emptyOrder.getId());

        // then: the deleted order should not be found anymore.
        Long deletedOrderId = emptyOrder.getId();
        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderById(deletedOrderId));
    }

    @Test
    void controllersReturnExpectedHttpStatuses() {
        /*
         * given: controller methods are called directly from this test.
         *
         * A controller returns ResponseEntity.
         * ResponseEntity contains:
         * - HTTP status, like 200 OK or 201 CREATED.
         * - Optional response body, like UserResponse or ProductDTO.
         */
        UserCreateRequest userRequest = createUserRequest("Controller User");

        // when: create a user through the controller.
        ResponseEntity<UserResponse> createdUserResponse = userController.createUser(userRequest);
        UserResponse user = createdUserResponse.getBody();

        // then: the user controller should return successful HTTP statuses.
        assertEquals(HttpStatus.CREATED, createdUserResponse.getStatusCode());
        assertNotNull(user);
        assertEquals(HttpStatus.OK, userController.getAllUsers().getStatusCode());
        assertEquals(HttpStatus.OK, userController.getUserById(user.getId()).getStatusCode());
        assertEquals(HttpStatus.OK, userController.updateUser(
                new UserCreateRequest("Controller User Updated", user.getEmail(), "secret456"),
                user.getId()
        ).getStatusCode());

        Product product = createProduct("Controller Product", "11.25", 7);

        // when: create a product through the controller.
        ResponseEntity<ProductDTO> createdProductResponse = productController.createProduct(product);
        ProductDTO createdProduct = createdProductResponse.getBody();

        // then: the product controller should return successful HTTP statuses.
        assertEquals(HttpStatus.CREATED, createdProductResponse.getStatusCode());
        assertNotNull(createdProduct);
        assertEquals(HttpStatus.OK, productController.getAllProducts().getStatusCode());
        assertEquals(HttpStatus.OK, productController.getProductById(createdProduct.getId()).getStatusCode());
        assertEquals(HttpStatus.OK, productController.updateProduct(
                createProduct("Controller Product Updated", "11.25", 6),
                createdProduct.getId()
        ).getStatusCode());

        AddCartItemRequest cartItemRequest = new AddCartItemRequest(createdProduct.getId(), 2);

        // when / then: the cart controller should return the expected HTTP statuses.
        assertEquals(HttpStatus.OK, cartController.getCart(user.getId()).getStatusCode());
        assertEquals(HttpStatus.CREATED, cartController.addItem(user.getId(), cartItemRequest).getStatusCode());
        assertEquals(HttpStatus.OK, cartController.updateItem(user.getId(), new AddCartItemRequest(createdProduct.getId(), 1)).getStatusCode());
        assertEquals(HttpStatus.OK, cartController.removeItem(user.getId(), createdProduct.getId()).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, cartController.clearCart(user.getId()).getStatusCode());

        cartController.addItem(user.getId(), new AddCartItemRequest(createdProduct.getId(), 1));
        ResponseEntity<OrderDTO> checkoutResponse = orderController.checkoutCart(user.getId());
        OrderDTO order = checkoutResponse.getBody();

        // then: the order controller should return successful HTTP statuses.
        assertEquals(HttpStatus.CREATED, checkoutResponse.getStatusCode());
        assertNotNull(order);
        assertEquals(HttpStatus.OK, orderController.getAllOrders().getStatusCode());
        assertEquals(HttpStatus.OK, orderController.getOrderById(order.getId()).getStatusCode());
        assertEquals(HttpStatus.OK, orderController.getOrdersByUserId(user.getId()).getStatusCode());
        assertEquals(HttpStatus.OK, orderController.updateOrderStatus(order.getId(), OrderStatus.CANCELLED).getStatusCode());

        UserResponse deleteUser = createTestUser("Delete Controller User");
        ProductDTO deleteProduct = productService.createProduct(createProduct("Delete Controller Product", "11.25", 3));

        // when / then: delete endpoints should return 204 NO_CONTENT.
        assertEquals(HttpStatus.NO_CONTENT, userController.deleteUser(deleteUser.getId()).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, productController.deleteProduct(deleteProduct.getId()).getStatusCode());
    }

    @Test
    void globalExceptionHandlerCreatesSimpleErrorResponses() {
        /*
         * given: the exception handler is created.
         *
         * The exception handler converts Java exceptions into HTTP responses.
         * Example:
         * ResourceNotFoundException becomes HTTP 404 NOT_FOUND.
         */
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        // when: the handler receives application exceptions.
        ResponseEntity<Map<String, Object>> notFoundResponse = handler.handleResourceNotFoundException(
                new ResourceNotFoundException("Product", "id", 123L)
        );
        ResponseEntity<Map<String, Object>> apiResponse = handler.handleAPIException(
                new APIException("Something went wrong")
        );

        // then: the handler should return simple HTTP error responses.
        assertEquals(HttpStatus.NOT_FOUND, notFoundResponse.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, apiResponse.getStatusCode());
        assertFalse((Boolean) notFoundResponse.getBody().get("status"));
        assertEquals("Something went wrong", apiResponse.getBody().get("message"));
    }

    private UserResponse createTestUser(String name) {
        // Helper method used by many tests to avoid repeating the same user setup code.
        UserCreateRequest request = createUserRequest(name);
        return userService.createUser(request);
    }

    private UserCreateRequest createUserRequest(String name) {
        /*
         * Tests create fake users in the database.
         *
         * Email addresses must be unique, so this method adds a unique value
         * to the email address.
         *
         * Example:
         * Controller User becomes something like:
         * controller-user-1789123456789@example.com
         *
         * This prevents tests from failing when the database already has old test users.
         */
        String email = name.toLowerCase().replace(" ", "-") + "-" + uniqueTestValue() + "@example.com";
        return new UserCreateRequest(name, email, "secret123");
    }

    private Product createProduct(String name, String price, Integer stock) {
        // Helper method used by many tests to create products with simple test values.
        return new Product(
                null,
                name + " " + uniqueTestValue(),
                "Test product description",
                new BigDecimal(price),
                stock,
                "images/products/test.jpg"
        );
    }

    private String uniqueTestValue() {
        /*
         * This gives each test user/product a different value.
         *
         * System.currentTimeMillis() returns the current time as a number.
         * We use it only to avoid duplicate names/emails in the test database.
         */
        return String.valueOf(System.currentTimeMillis());
    }

    private boolean userListContainsId(List<UserResponse> users, Long userId) {
        // Beginner-friendly loop: look through the list until we find the user ID.
        for (UserResponse user : users) {
            if (user.getId().equals(userId)) {
                return true;
            }
        }

        return false;
    }

    private boolean productListContainsId(List<ProductDTO> products, Long productId) {
        // Beginner-friendly loop: look through the list until we find the product ID.
        for (ProductDTO product : products) {
            if (product.getId().equals(productId)) {
                return true;
            }
        }

        return false;
    }

    private boolean orderListContainsId(List<OrderDTO> orders, Long orderId) {
        // Beginner-friendly loop: look through the list until we find the order ID.
        for (OrderDTO order : orders) {
            if (order.getId().equals(orderId)) {
                return true;
            }
        }

        return false;
    }
}
