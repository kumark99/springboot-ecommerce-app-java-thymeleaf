package com.example.shoppingcart.service;

import com.example.shoppingcart.exception.ServiceException;
import com.example.shoppingcart.model.CartItem;
import com.example.shoppingcart.model.Order;
import com.example.shoppingcart.model.OrderItem;
import com.example.shoppingcart.model.User;
import com.example.shoppingcart.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Transactional
    public Order createOrder(User user, List<CartItem> cartItems) {
        try {
            logger.info("Creating order for user: {}", user.getUsername());
            Order order = new Order();
            order.setUser(user);
            order.setOrderDate(LocalDateTime.now());
            order.setStatus("PENDING");
            
            double total = 0;
            for (CartItem cartItem : cartItems) {
                OrderItem orderItem = new OrderItem();
                orderItem.setProduct(cartItem.getProduct());
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setPrice(cartItem.getProduct().getPrice());
                order.addItem(orderItem);
                total += cartItem.getTotalPrice();
            }
            order.setTotalAmount(total);
            
            Order savedOrder = orderRepository.save(order);
            logger.info("Order created successfully with id: {}", savedOrder.getId());
            return savedOrder;
        } catch (Exception e) {
            logger.error("Error creating order for user: {}", user.getUsername(), e);
            throw new ServiceException("Error creating order", e);
        }
    }

    public List<Order> getOrdersByUser(User user) {
        try {
            logger.debug("Fetching orders for user: {}", user.getUsername());
            return orderRepository.findByUserOrderByOrderDateDesc(user);
        } catch (Exception e) {
            logger.error("Error fetching orders for user: {}", user.getUsername(), e);
            throw new ServiceException("Error fetching orders for user", e);
        }
    }

    public List<Order> getAllOrders() {
        try {
            logger.debug("Fetching all orders");
            return orderRepository.findAllByOrderByOrderDateDesc();
        } catch (Exception e) {
            logger.error("Error fetching all orders", e);
            throw new ServiceException("Error fetching all orders", e);
        }
    }

    public Optional<Order> getOrderById(Long id) {
        try {
            logger.debug("Fetching order by id: {}", id);
            return orderRepository.findById(id);
        } catch (Exception e) {
            logger.error("Error fetching order by id: {}", id, e);
            throw new ServiceException("Error fetching order by id: " + id, e);
        }
    }

    public void updateOrderStatus(Long orderId, String status) {
        try {
            logger.info("Updating order status for order id: {} to {}", orderId, status);
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                order.setStatus(status);
                orderRepository.save(order);
                logger.info("Order status updated successfully");
            } else {
                logger.warn("Order not found for id: {}", orderId);
                throw new ServiceException("Order not found for id: " + orderId);
            }
        } catch (Exception e) {
            logger.error("Error updating order status for id: {}", orderId, e);
            throw new ServiceException("Error updating order status", e);
        }
    }
}
