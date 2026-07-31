package org.example.backend.config;

import org.example.backend.model.Product;
import org.example.backend.model.User;
import org.example.backend.repository.ProductRepository;
import org.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedAdminUser(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.username:admin@example.com}") String adminUsername,
            @Value("${app.admin.password:admin123}") String adminPassword
    ) {
        return args -> {
            if (userRepository.findByEmail(adminUsername) != null) {
                return;
            }

            userRepository.save(new User(
                    null,
                    "Admin",
                    adminUsername,
                    passwordEncoder.encode(adminPassword)
            ));
        };
    }

    @Bean
    CommandLineRunner seedProducts(ProductRepository productRepository) {
        return args -> {
            seedProduct(productRepository, "Black and Gray Athletic Cotton Socks - 6 Pairs", "Sports and apparel product from the EcommerceShop catalog.", 1090, 50, "images/products/athletic-cotton-socks-6-pairs.jpg");
            seedProduct(productRepository, "Intermediate Size Basketball", "Sports product from the EcommerceShop catalog.", 2095, 50, "images/products/intermediate-composite-basketball.jpg");
            seedProduct(productRepository, "Adults Plain Cotton T-Shirt - 2 Pack", "Apparel product from the EcommerceShop catalog.", 799, 50, "images/products/adults-plain-cotton-tshirt-2-pack-teal.jpg");
            seedProduct(productRepository, "2 Slot Toaster - White", "Kitchen appliance from the EcommerceShop catalog.", 1899, 50, "images/products/2-slot-toaster-white.jpg");
            seedProduct(productRepository, "2 Piece White Dinner Plate Set", "Kitchen and dining product from the EcommerceShop catalog.", 2067, 50, "images/products/elegant-white-dinner-plate-set.jpg");
            seedProduct(productRepository, "3 Piece Non-Stick, Black Cooking Pot Set", "Kitchen cookware product from the EcommerceShop catalog.", 3499, 50, "images/products/3-piece-cooking-set.jpg");
            seedProduct(productRepository, "Cotton Oversized Sweater - Gray", "Apparel product from the EcommerceShop catalog.", 2400, 50, "images/products/women-plain-cotton-oversized-sweater-gray.jpg");
            seedProduct(productRepository, "2 Piece Luxury Towel Set - White", "Bathroom towel set from the EcommerceShop catalog.", 3599, 50, "images/products/luxury-towel-set.jpg");
            seedProduct(productRepository, "Ultra Soft Tissue 2-Ply - 8 Boxes", "Household product from the EcommerceShop catalog.", 2374, 50, "images/products/facial-tissue-2-ply-8-boxes.jpg");
            seedProduct(productRepository, "Women's Striped Beach Dress", "Women's apparel product from the EcommerceShop catalog.", 2970, 50, "images/products/women-striped-beach-dress.jpg");
            seedProduct(productRepository, "Women's Sandal Heels - Pink", "Women's footwear product from the EcommerceShop catalog.", 5300, 50, "images/products/women-sandal-heels-white-pink.jpg");
            seedProduct(productRepository, "Round Sunglasses", "Accessories product from the EcommerceShop catalog.", 3560, 50, "images/products/round-sunglasses-gold.jpg");
            seedProduct(productRepository, "Blackout Curtains Set - Beige", "Home product from the EcommerceShop catalog.", 4599, 50, "images/products/blackout-curtain-set-beige.jpg");
            seedProduct(productRepository, "Women's Summer Jean Shorts", "Women's apparel product from the EcommerceShop catalog.", 1699, 50, "images/products/women-summer-jean-shorts.jpg");
            seedProduct(productRepository, "Electric Hot Water Kettle - White", "Kitchen appliance from the EcommerceShop catalog.", 5074, 50, "images/products/electric-steel-hot-water-kettle-white.jpg");
            seedProduct(productRepository, "Waterproof Knit Athletic Sneakers - Gray", "Footwear product from the EcommerceShop catalog.", 5390, 50, "images/products/knit-athletic-sneakers-gray.jpg");
            seedProduct(productRepository, "Straw Wide Brim Sun Hat", "Summer apparel product from the EcommerceShop catalog.", 2200, 50, "images/products/straw-sunhat.jpg");
            seedProduct(productRepository, "Men's Athletic Sneaker - White", "Men's footwear product from the EcommerceShop catalog.", 4590, 50, "images/products/men-athletic-shoes-white.jpg");
            seedProduct(productRepository, "Men's Wool Sweater - Black", "Men's apparel product from the EcommerceShop catalog.", 3374, 50, "images/products/men-stretch-wool-sweater-black.jpg");
            seedProduct(productRepository, "Bathroom Bath Mat 16 x 32 Inch - Grey", "Bathroom and home product from the EcommerceShop catalog.", 1850, 50, "images/products/bathroom-mat.jpg");
            seedProduct(productRepository, "Women's Ballet Flat - White", "Women's footwear product from the EcommerceShop catalog.", 2640, 50, "images/products/women-knit-ballet-flat-white.jpg");
            seedProduct(productRepository, "Men's Golf Polo Shirt - Gray", "Men's apparel product from the EcommerceShop catalog.", 1599, 50, "images/products/men-golf-polo-t-shirt-gray.jpg");
            seedProduct(productRepository, "Laundry Detergent Tabs, 50 Loads", "Cleaning product from the EcommerceShop catalog.", 2899, 50, "images/products/laundry-detergent-tabs.jpg");
            seedProduct(productRepository, "Sterling Silver Leaf Branch Earrings", "Jewelry and accessories product from the EcommerceShop catalog.", 6799, 50, "images/products/sky-leaf-branch-earrings.jpg");
            seedProduct(productRepository, "Duvet Cover Set, Diamond Pattern", "Bedroom and home product from the EcommerceShop catalog.", 4399, 50, "images/products/duvet-cover-set-gray-queen.jpg");
            seedProduct(productRepository, "Women's Knit Winter Beanie - Blue", "Women's winter apparel product from the EcommerceShop catalog.", 1950, 50, "images/products/women-knit-beanie-pom-pom-blue.jpg");
            seedProduct(productRepository, "Men's Chino Pants - Beige", "Men's apparel product from the EcommerceShop catalog.", 2290, 50, "images/products/men-chino-pants-beige.jpg");
            seedProduct(productRepository, "Men's Navigator Sunglasses", "Men's accessories product from the EcommerceShop catalog.", 3690, 50, "images/products/men-navigator-sunglasses-black.jpg");
            seedProduct(productRepository, "Men's Brown Flat Sneakers", "Men's footwear product from the EcommerceShop catalog.", 2499, 50, "images/products/men-brown-flat-sneakers.jpg");
            seedProduct(productRepository, "Non-Stick Cook Set With Lids - 4 Pieces", "Kitchen cookware product from the EcommerceShop catalog.", 6797, 50, "images/products/non-stick-cooking-set-4-pieces.jpg");
            seedProduct(productRepository, "Vanity Mirror with LED Lights - Pink", "Bathroom and home product from the EcommerceShop catalog.", 2549, 50, "images/products/vanity-mirror-pink.jpg");
            seedProduct(productRepository, "Women's Relaxed Lounge Pants - Pink", "Women's apparel product from the EcommerceShop catalog.", 3400, 50, "images/products/women-relaxed-lounge-pants-pink.jpg");
            seedProduct(productRepository, "Crystal Zirconia Stud Earrings - Pink", "Women's accessories product from the EcommerceShop catalog.", 3467, 50, "images/products/crystal-zirconia-stud-earrings-pink.jpg");
            seedProduct(productRepository, "Glass Screw Lid Containers - 3 Pieces", "Kitchen food container product from the EcommerceShop catalog.", 2899, 50, "images/products/glass-screw-lid-food-containers.jpg");
            seedProduct(productRepository, "Black and Silver Espresso Maker", "Kitchen appliance from the EcommerceShop catalog.", 8250, 50, "images/products/black-and-silver-espresso-maker.jpg");
            seedProduct(productRepository, "Blackout Curtains Set 42 x 84-Inch - Teal", "Bedroom and home product from the EcommerceShop catalog.", 3099, 50, "images/products/blackout-curtains-set-teal.jpg");
            seedProduct(productRepository, "Bath Towels 2 Pack - Gray, Rosewood", "Bathroom towel set from the EcommerceShop catalog.", 2990, 50, "images/products/bath-towel-set-gray-rosewood.jpg");
            seedProduct(productRepository, "Athletic Skateboard Shoes - Gray", "Footwear product from the EcommerceShop catalog.", 3390, 50, "images/products/athletic-skateboard-shoes-gray.jpg");
            seedProduct(productRepository, "Countertop Push Blender - Black", "Kitchen appliance from the EcommerceShop catalog.", 10747, 50, "images/products/countertop-push-blender-black.jpg");
            seedProduct(productRepository, "Men's Fleece Hoodie - Light Teal", "Men's apparel product from the EcommerceShop catalog.", 3800, 50, "images/products/men-cozy-fleece-hoodie-light-teal.jpg");
            seedProduct(productRepository, "Artistic Bowl and Plate Set - 6 Pieces", "Kitchen product from the EcommerceShop catalog.", 3899, 50, "images/products/artistic-bowl-set-6-piece.jpg");
            seedProduct(productRepository, "2-Ply Kitchen Paper Towels - 8 Pack", "Kitchen household product from the EcommerceShop catalog.", 1899, 50, "images/products/kitchen-paper-towels-8-pack.jpg");
        };
    }

    private void seedProduct(
            ProductRepository productRepository,
            String name,
            String description,
            int priceCents,
            int stock,
            String imageUrl
    ) {
        if (productRepository.existsByName(name)) {
            return;
        }

        productRepository.save(new Product(
                null,
                name,
                description,
                BigDecimal.valueOf(priceCents, 2),
                stock,
                imageUrl
        ));
    }
}

