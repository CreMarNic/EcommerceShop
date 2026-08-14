package org.example.backend.service;

import org.example.backend.exceptions.ResourceNotFoundException;
import org.example.backend.dto.ProductDTO;
import org.example.backend.model.Product;
import org.example.backend.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<ProductDTO> getAllProducts() {
        List<Product> products = productRepository.findAll();
        List<ProductDTO> productDTOs = new ArrayList<>();

        for (Product product : products) {
            ProductDTO productDTO = toDTO(product);
            productDTOs.add(productDTO);
        }

        return productDTOs;
    }

    @Override
    public ProductDTO getProductById(Long productId) {
        return toDTO(findProductById(productId));
    }

    @Override
    public ProductDTO createProduct(Product product) {
        product.setId(null);
        return toDTO(productRepository.save(product));
    }

    @Override
    public ProductDTO updateProduct(Product product, Long productId) {
        Product existingProduct = findProductById(productId);

        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setStock(product.getStock());
        existingProduct.setImageUrl(product.getImageUrl());

        return toDTO(productRepository.save(existingProduct));
    }

    @Override
    public void deleteProduct(Long productId) {
        Product product = findProductById(productId);
        productRepository.delete(product);
    }

    private Product findProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
    }

    private ProductDTO toDTO(Product product) {
        return new ProductDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getImageUrl()
        );
    }

}

