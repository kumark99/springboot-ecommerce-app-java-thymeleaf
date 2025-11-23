package com.example.shoppingcart.service;

import com.example.shoppingcart.exception.ServiceException;
import com.example.shoppingcart.model.CartItem;
import com.example.shoppingcart.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@SessionScope
public class CartService {
    private static final Logger logger = LoggerFactory.getLogger(CartService.class);
    private List<CartItem> cartItems = new ArrayList<>();

    public void addToCart(Product product) {
        try {
            logger.info("Adding product to cart: {}", product.getName());
            Optional<CartItem> existingItem = cartItems.stream()
                    .filter(item -> item.getProduct().getId().equals(product.getId()))
                    .findFirst();

            if (existingItem.isPresent()) {
                logger.debug("Product already in cart, incrementing quantity");
                existingItem.get().setQuantity(existingItem.get().getQuantity() + 1);
            } else {
                logger.debug("Product not in cart, adding new item");
                cartItems.add(new CartItem(product, 1));
            }
        } catch (Exception e) {
            logger.error("Error adding product to cart: {}", product.getName(), e);
            throw new ServiceException("Error adding product to cart", e);
        }
    }

    public void removeFromCart(Long productId) {
        try {
            logger.info("Removing product from cart, id: {}", productId);
            cartItems.removeIf(item -> item.getProduct().getId().equals(productId));
        } catch (Exception e) {
            logger.error("Error removing product from cart, id: {}", productId, e);
            throw new ServiceException("Error removing product from cart", e);
        }
    }

    public List<CartItem> getCartItems() {
        try {
            return cartItems;
        } catch (Exception e) {
            logger.error("Error fetching cart items", e);
            throw new ServiceException("Error fetching cart items", e);
        }
    }

    public void clearCart() {
        try {
            logger.info("Clearing cart");
            cartItems.clear();
        } catch (Exception e) {
            logger.error("Error clearing cart", e);
            throw new ServiceException("Error clearing cart", e);
        }
    }

    public double getCartTotal() {
        try {
            return cartItems.stream().mapToDouble(CartItem::getTotalPrice).sum();
        } catch (Exception e) {
            logger.error("Error calculating cart total", e);
            throw new ServiceException("Error calculating cart total", e);
        }
    }

    public int getCartItemCount() {
        try {
            return cartItems.stream().mapToInt(CartItem::getQuantity).sum();
        } catch (Exception e) {
            logger.error("Error calculating cart item count", e);
            return 0;
        }
    }

    public void updateItemQuantity(Long productId, int quantity) {
        try {
            logger.info("Updating quantity for product id: {} to {}", productId, quantity);
            Optional<CartItem> item = cartItems.stream()
                    .filter(i -> i.getProduct().getId().equals(productId))
                    .findFirst();
            
            if (item.isPresent()) {
                if (quantity <= 0) {
                    removeFromCart(productId);
                } else {
                    item.get().setQuantity(quantity);
                }
            }
        } catch (Exception e) {
            logger.error("Error updating cart item quantity", e);
            throw new ServiceException("Error updating cart item quantity", e);
        }
    }
}
