package com.alvira.jewellerystore;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class StoreController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private WishlistItemRepository wishlistItemRepository;

    @Autowired
    private QuantityOfferRepository quantityOfferRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @GetMapping("/store")
    public String showStore(@RequestParam(required = false) String category,
                            @RequestParam(required = false) String search,
                            @RequestParam(required = false) String sort,
                            Model model, HttpSession session) {
        List<Product> products;
        if (search != null && !search.trim().isEmpty()) {
            products = productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search.trim(), search.trim());
        } else if (category != null && !category.isEmpty()) {
            products = productRepository.findByCategory(category);
        } else {
            products = productRepository.findAll();
        }

        if ("price_low".equals(sort)) {
            products = products.stream()
                    .sorted((a, b) -> Double.compare(a.getPrice() != null ? a.getPrice() : 0, b.getPrice() != null ? b.getPrice() : 0))
                    .toList();
        } else if ("price_high".equals(sort)) {
            products = products.stream()
                    .sorted((a, b) -> Double.compare(b.getPrice() != null ? b.getPrice() : 0, a.getPrice() != null ? a.getPrice() : 0))
                    .toList();
        } else if ("popularity".equals(sort)) {
            products = products.stream()
                    .sorted((a, b) -> reviewRepository.findByProductId(b.getId()).size() - reviewRepository.findByProductId(a.getId()).size())
                    .toList();
        }

        model.addAttribute("products", products);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("searchQuery", search);
        model.addAttribute("selectedSort", sort);

        Cart cart = (Cart) session.getAttribute("cart");
        int cartCount = (cart != null) ? cart.getItemCount() : 0;
        model.addAttribute("cartCount", cartCount);

        java.util.Map<Long, Integer> cartQuantities = new java.util.HashMap<>();
        if (cart != null) {
            for (CartItem item : cart.getItems()) {
                cartQuantities.put(item.getProduct().getId(), item.getQuantity());
            }
        }
        model.addAttribute("cartQuantities", cartQuantities);

        List<QuantityOffer> activeOffers = quantityOfferRepository.findByActiveTrue();
        java.util.Map<Long, String> offerBadges = new java.util.HashMap<>();
        for (Product p : products) {
            for (QuantityOffer offer : activeOffers) {
                if (offer.appliesToProduct(p.getId())) {
                    offerBadges.put(p.getId(), offer.getBadgeText());
                    break;
                }
            }
        }
        model.addAttribute("offerBadges", offerBadges);

        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        java.util.Set<Long> wishlistedIds = new java.util.HashSet<>();
        if (customer != null) {
            for (WishlistItem item : wishlistItemRepository.findByCustomerEmail(customer.getEmail())) {
                wishlistedIds.add(item.getProductId());
            }
        }
        model.addAttribute("wishlistedIds", wishlistedIds);

        return "products";
    }}