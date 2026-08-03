package org.example.backend;

import org.example.backend.dto.ProductDTO;
import org.example.backend.exceptions.ResourceNotFoundException;
import org.example.backend.model.Product;
import org.example.backend.repository.ProductRepository;
import org.example.backend.service.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ProductServiceImplUnitTests {

    @Mock
    private ProductRepository productRepository;

    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl(productRepository);
    }

    @Test
    void createProductSetsIdToNullAndSavesProduct() {

        // given: a product request comes in with an ID, but new products should not choose their own ID.

        Product productRequest = createProduct(99L, "Keyboard", "49.99", 10);
        Product savedProduct = createProduct(1L, "Keyboard", "49.99", 10);

        when(productRepository.save(productRequest)).thenReturn(savedProduct);

        // when: the service creates the product.

        ProductDTO response = productService.createProduct(productRequest);

        // then: the service clears the request ID and returns the saved product data.

        assertNull(productRequest.getId());
        assertEquals(1L, response.getId());
        assertEquals("Keyboard", response.getName());
        assertEquals(new BigDecimal("49.99"), response.getPrice());

        verify(productRepository).save(productRequest);
    }

    @Test
    void updateProductCopiesNewValuesToExistingProduct() {

        // given: an existing product is already in the database.

        Long productId = 1L;
        Product existingProduct = createProduct(productId, "Old Mouse", "20.00", 5);
        Product updateRequest = createProduct(null, "New Mouse", "25.00", 8);

        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(existingProduct)).thenReturn(existingProduct);

        // when: the service updates that product.

        ProductDTO response = productService.updateProduct(updateRequest, productId);

        // then: the existing product now has the new values.

        assertEquals(productId, response.getId());
        assertEquals("New Mouse", response.getName());
        assertEquals("Test product description", response.getDescription());
        assertEquals(new BigDecimal("25.00"), response.getPrice());
        assertEquals(8, response.getStock());

        verify(productRepository).findById(productId);
        verify(productRepository).save(existingProduct);
    }

    @Test
    void getProductByIdThrowsExceptionWhenProductDoesNotExist() {

        // given: the fake repository cannot find this product ID.

        Long missingProductId = 100L;

        when(productRepository.findById(missingProductId)).thenReturn(Optional.empty());

        // when / then: asking for the missing product should throw ResourceNotFoundException.

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(missingProductId));

        verify(productRepository).findById(missingProductId);
    }

    private Product createProduct(Long id, String name, String price, Integer stock) {
        return new Product(
                id,
                name,
                "Test product description",
                new BigDecimal(price),
                stock,
                "images/products/test.jpg"
        );
    }
}
