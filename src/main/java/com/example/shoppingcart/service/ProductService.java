package com.example.shoppingcart.service;

import com.example.shoppingcart.exception.ServiceException;
import com.example.shoppingcart.model.Product;
import com.example.shoppingcart.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        try {
            logger.debug("Fetching all products");
            return productRepository.findAll();
        } catch (Exception e) {
            logger.error("Error fetching all products", e);
            throw new ServiceException("Error fetching all products", e);
        }
    }

    public Optional<Product> getProductById(Long id) {
        try {
            logger.debug("Fetching product by id: {}", id);
            return productRepository.findById(id);
        } catch (Exception e) {
            logger.error("Error fetching product by id: {}", id, e);
            throw new ServiceException("Error fetching product by id: " + id, e);
        }
    }

    public Product saveProduct(Product product) {
        try {
            logger.info("Saving product: {}", product.getName());
            return productRepository.save(product);
        } catch (Exception e) {
            logger.error("Error saving product: {}", product.getName(), e);
            throw new ServiceException("Error saving product: " + product.getName(), e);
        }
    }

    public void deleteProduct(Long id) {
        try {
            logger.info("Deleting product id: {}", id);
            productRepository.deleteById(id);
        } catch (Exception e) {
            logger.error("Error deleting product id: {}", id, e);
            throw new ServiceException("Error deleting product id: " + id, e);
        }
    }
}
