package com.example.shoppingcart;

import com.example.shoppingcart.model.Product;
import com.example.shoppingcart.model.User;
import com.example.shoppingcart.repository.ProductRepository;
import com.example.shoppingcart.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class ShoppingCartApplication {

	private static final Logger logger = LoggerFactory.getLogger(ShoppingCartApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(ShoppingCartApplication.class, args);
		logger.info("ShoppingCartApplication started successfully.");
	}

	@Bean
	public CommandLineRunner demo(UserRepository userRepository, ProductRepository productRepository, PasswordEncoder passwordEncoder) {
		return (args) -> {
			logger.info("Initializing demo data...");
			// Create admin user if not exists
			if (userRepository.findByUsername("admin").isEmpty()) {
				User admin = new User();
				admin.setUsername("admin");
				admin.setPassword(passwordEncoder.encode("admin123"));
				admin.setRole("ROLE_ADMIN");
				userRepository.save(admin);
				System.out.println("Admin user created: admin/admin123");
				logger.info("Admin user created.");
			}

			// Create normal user if not exists
			if (userRepository.findByUsername("user").isEmpty()) {
				User user = new User();
				user.setUsername("user");
				user.setPassword(passwordEncoder.encode("user123"));
				user.setRole("ROLE_USER");
				userRepository.save(user);
				System.out.println("Normal user created: user/user123");
				logger.info("Normal user created.");
			}

			// Create sample products if empty
			if (productRepository.count() == 0) {
				productRepository.save(new Product("iPhone 15 Pro", "Titanium design, A17 Pro chip, 48MP Main camera.", 999.00, "https://m.media-amazon.com/images/I/71657TiFeHL._SX679_.jpg"));
				productRepository.save(new Product("Samsung Galaxy S24 Ultra", "Galaxy AI is here. Epic design, epic performance.", 1299.00, "https://m.media-amazon.com/images/I/418mFfRZu-L._SY300_SX300_QL70_FMwebp_.jpg"));
				productRepository.save(new Product("Sony WH-1000XM5", "Wireless Noise Cancelling Headphones with Auto NC Optimizer.", 348.00, "https://m.media-amazon.com/images/I/51SKmu2G9FL._AC_UF894,1000_QL80_.jpg"));
				productRepository.save(new Product("MacBook Air 15-inch", "Supercharged by M2. Impossibly thin and incredibly fast.", 1299.00, "https://store.storeimages.cdn-apple.com/4668/as-images.apple.com/is/mba15-midnight-select-202306?wid=904&hei=840&fmt=jpeg&qlt=90&.v=1684518479433"));
				productRepository.save(new Product("Nintendo Switch OLED", "7-inch OLED screen, wide adjustable stand, wired LAN port.", 349.99, "https://assets.nintendo.com/image/upload/f_auto/q_auto/dpr_1.5/c_scale,w_600/ncom/en_US/switch/site-design-update/hardware/switch/nintendo-switch-oled-model-white-set/gallery/image01"));
				
				// Additional 5 products
				productRepository.save(new Product("Dell XPS 13", "13.4-inch FHD+ display, Intel Core i7, 16GB RAM, 512GB SSD.", 1099.00, "https://m.media-amazon.com/images/I/712CAwRf6xL._SX679_.jpg"));
				productRepository.save(new Product("iPad Air", "Liquid Retina display, M1 chip, 5G capable.", 599.00, "https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/ipad-air-select-wifi-blue-202203?wid=940&hei=1112&fmt=png-alpha&.v=1645065732688"));
				productRepository.save(new Product("Bose QuietComfort 45", "Iconic quiet, comfort, and sound.", 329.00, "https://m.media-amazon.com/images/I/31+fg95OcqL._SY300_SX300_QL70_FMwebp_.jpg"));
				productRepository.save(new Product("Canon EOS R6", "Full-frame mirrorless camera with 20MP CMOS sensor.", 2499.00, "https://static.bhphoto.com/images/images500x500/canon_eos_r6_mirrorless_digital_1594281472_1547009.jpg"));
				productRepository.save(new Product("Logitech MX Master 3S", "Performance Wireless Mouse with Ultra-fast Scrolling.", 99.99, "https://resource.logitech.com/w_692,c_lpad,ar_4:3,q_auto,f_auto,dpr_1.0/d_transparent.gif/content/dam/logitech/en/products/mice/mx-master-3s/gallery/mx-master-3s-mouse-top-view-graphite.png?v=1"));
				
				System.out.println("Sample products created");
				logger.info("Sample products created.");
			}
			logger.info("Demo data initialization completed.");
		};
	}

}
