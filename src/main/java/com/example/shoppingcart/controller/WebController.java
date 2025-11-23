package com.example.shoppingcart.controller;

import com.example.shoppingcart.model.Order;
import com.example.shoppingcart.model.Product;
import com.example.shoppingcart.model.User;
import com.example.shoppingcart.repository.UserRepository;
import com.example.shoppingcart.service.CartService;
import com.example.shoppingcart.service.OrderService;
import com.example.shoppingcart.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import com.example.shoppingcart.model.CartItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class WebController {

    private static final Logger logger = LoggerFactory.getLogger(WebController.class);

    @Autowired
    private ProductService productService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @ModelAttribute("cartCount")
    public int getCartCount() {
        return cartService.getCartItemCount();
    }

    @GetMapping("/login")
    public String login() {
        logger.info("Accessing login page");
        return "login";
    }

    @GetMapping("/")
    public String viewHomePage(Model model) {
        logger.info("Accessing home page");
        model.addAttribute("listProducts", productService.getAllProducts());
        return "index";
    }

    @GetMapping("/showNewProductForm")
    public String showNewProductForm(Model model) {
        logger.info("Accessing new product form");
        Product product = new Product();
        model.addAttribute("product", product);
        return "new_product";
    }

    @PostMapping("/saveProduct")
    public String saveProduct(@ModelAttribute("product") Product product) {
        logger.info("Saving product: {}", product.getName());
        productService.saveProduct(product);
        return "redirect:/";
    }

    @GetMapping("/showFormForUpdate/{id}")
    public String showFormForUpdate(@PathVariable(value = "id") Long id, Model model) {
        logger.info("Accessing update form for product id: {}", id);
        Product product = productService.getProductById(id).orElse(null);
        model.addAttribute("product", product);
        return "update_product";
    }

    @GetMapping("/deleteProduct/{id}")
    public String deleteProduct(@PathVariable(value = "id") Long id) {
        logger.info("Deleting product id: {}", id);
        productService.deleteProduct(id);
        return "redirect:/";
    }

    @GetMapping("/product/{id}")
    public String viewProductDetails(@PathVariable(value = "id") Long id, Model model) {
        logger.info("Viewing product details for id: {}", id);
        Product product = productService.getProductById(id).orElse(null);
        if (product == null) {
            logger.warn("Product not found for id: {}", id);
            return "redirect:/";
        }
        model.addAttribute("product", product);
        return "product_details";
    }

    @GetMapping("/addToCart/{id}")
    public String addToCart(@PathVariable(value = "id") Long id, RedirectAttributes redirectAttributes) {
        logger.info("Adding product id: {} to cart", id);
        Product product = productService.getProductById(id).orElse(null);
        if (product != null) {
            cartService.addToCart(product);
            redirectAttributes.addFlashAttribute("message", "1 item added to the cart");
        } else {
            logger.warn("Product not found for id: {}", id);
            redirectAttributes.addFlashAttribute("error", "Product not found");
        }
        return "redirect:/";
    }

    @PostMapping("/updateCartItemQuantity")
    public String updateCartItemQuantity(@RequestParam("productId") Long productId, @RequestParam("quantity") int quantity) {
        cartService.updateItemQuantity(productId, quantity);
        return "redirect:/viewCart";
    }

    @GetMapping("/buyNow/{id}")
    public String buyNow(@PathVariable(value = "id") Long id) {
        logger.info("Buying now product id: {}", id);
        Product product = productService.getProductById(id).orElse(null);
        if (product != null) {
            cartService.addToCart(product);
        } else {
            logger.warn("Product not found for id: {}", id);
        }
        return "redirect:/checkout";
    }

    @GetMapping("/viewCart")
    public String viewCart(Model model) {
        logger.info("Viewing cart");
        model.addAttribute("cartItems", cartService.getCartItems());
        model.addAttribute("total", cartService.getCartTotal());
        return "view_cart";
    }

    @GetMapping("/removeFromCart/{id}")
    public String removeFromCart(@PathVariable(value = "id") Long id) {
        logger.info("Removing product id: {} from cart", id);
        cartService.removeFromCart(id);
        return "redirect:/viewCart";
    }

    @GetMapping("/checkout")
    public String checkout(Model model) {
        logger.info("Accessing checkout page");
        model.addAttribute("cartItems", cartService.getCartItems());
        model.addAttribute("total", cartService.getCartTotal());
        return "checkout";
    }

    @GetMapping("/payment")
    public String payment(Model model) {
        logger.info("Accessing payment page");
        model.addAttribute("total", cartService.getCartTotal());
        return "payment";
    }

    @PostMapping("/processPayment")
    public String processPayment(Model model) {
        logger.info("Processing payment");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        
        if (user != null) {
            List<CartItem> cartItems = cartService.getCartItems();
            if (!cartItems.isEmpty()) {
                logger.info("Creating order for user: {}", username);
                orderService.createOrder(user, cartItems);
                cartService.clearCart();
            } else {
                logger.warn("Cart is empty for user: {}", username);
            }
        } else {
            logger.error("User not found during payment processing: {}", username);
        }
        return "order_confirmation";
    }

    @GetMapping("/myOrders")
    public String myOrders(Model model) {
        logger.info("Accessing my orders");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null) {
            List<Order> orders = orderService.getOrdersByUser(user);
            model.addAttribute("orders", orders);
        }
        return "my_orders";
    }

    @GetMapping("/order/{id}")
    public String orderDetails(@PathVariable(value = "id") Long id, Model model) {
        logger.info("Viewing order details for order id: {}", id);
        Order order = orderService.getOrderById(id).orElse(null);
        model.addAttribute("order", order);
        return "order_details";
    }

    @GetMapping("/admin/orders")
    public String adminOrders(Model model) {
        logger.info("Accessing admin orders page");
        List<Order> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);
        return "admin_orders";
    }

    @PostMapping("/admin/updateOrderStatus")
    public String updateOrderStatus(@RequestParam("orderId") Long orderId, @RequestParam("status") String status) {
        logger.info("Updating order status for order id: {} to {}", orderId, status);
        orderService.updateOrderStatus(orderId, status);
        return "redirect:/admin/orders";
    }
}
