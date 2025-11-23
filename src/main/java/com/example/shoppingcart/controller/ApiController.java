package com.example.shoppingcart.controller;

import com.example.shoppingcart.model.Product;
import com.example.shoppingcart.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ApiController {

    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

    @Autowired
    private ProductService productService;

    @GetMapping
    public List<Product> getAllProducts() {
        logger.info("API: Fetching all products");
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        logger.info("API: Fetching product by id: {}", id);
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    logger.warn("API: Product not found for id: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        logger.info("API: Creating new product: {}", product.getName());
        return productService.saveProduct(product);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        logger.info("API: Updating product id: {}", id);
        return productService.getProductById(id)
                .map(product -> {
                    product.setName(productDetails.getName());
                    product.setDescription(productDetails.getDescription());
                    product.setPrice(productDetails.getPrice());
                    product.setImageUrl(productDetails.getImageUrl());
                    Product updatedProduct = productService.saveProduct(product);
                    return ResponseEntity.ok(updatedProduct);
                })
                .orElseGet(() -> {
                    logger.warn("API: Product not found for update, id: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        logger.info("API: Deleting product id: {}", id);
        if (productService.getProductById(id).isPresent()) {
            productService.deleteProduct(id);
            return ResponseEntity.ok().build();
        }
        logger.warn("API: Product not found for deletion, id: {}", id);
        return ResponseEntity.notFound().build();
    }
}
