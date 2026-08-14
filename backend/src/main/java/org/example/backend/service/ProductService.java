package org.example.backend.service;

import org.example.backend.dto.ProductDTO;
import org.example.backend.model.Product;

import java.util.List;

public interface ProductService {

    List<ProductDTO> getAllProducts();

    ProductDTO getProductById(Long productId);

    ProductDTO createProduct(Product product);

    ProductDTO updateProduct(Product product, Long productId);

    void deleteProduct(Long productId);
}

