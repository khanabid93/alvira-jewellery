package com.alvira.jewellerystore;

import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private WishlistItemRepository wishlistItemRepository;

    @Autowired
    private QuantityOfferRepository quantityOfferRepository;

    @GetMapping("/")
    public String home(Model model, jakarta.servlet.http.HttpSession session) {
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
        List<Product> featured = productRepository.findByFeaturedTrue();
        model.addAttribute("featuredProducts", featured);

        List<QuantityOffer> activeOffers = quantityOfferRepository.findByActiveTrue();
        java.util.Map<Long, String> offerBadges = new java.util.HashMap<>();
        for (Product p : featured) {
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

        return "home";
    }
    @Autowired
    private NewsletterSubscriberRepository newsletterRepository;

    @PostMapping("/newsletter-signup")
    public String newsletterSignup(@RequestParam String email, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        if (newsletterRepository.findByEmail(email) != null) {
            redirectAttributes.addFlashAttribute("newsletterMessage", "You're already subscribed!");
        } else {
            NewsletterSubscriber subscriber = new NewsletterSubscriber();
            subscriber.setEmail(email);
            newsletterRepository.save(subscriber);
            redirectAttributes.addFlashAttribute("newsletterMessage", "Thank you for subscribing!");
        }
        return "redirect:/";
    }
    @GetMapping("/products")
    @ResponseBody
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @GetMapping("/add-sample-product")
    @ResponseBody
    public String addSampleProducts() {
        Product p1 = new Product();
        p1.setName("Gold Plated Necklace");
        p1.setDescription("Elegant gold plated necklace with stone work");
        p1.setPrice(1299.0);
        p1.setImageUrl("https://images.pexels.com/photos/29013500/pexels-photo-29013500.jpeg?cs=srgb&w=400&h=400&fit=crop");
        productRepository.save(p1);

        Product p2 = new Product();
        p2.setName("Pearl Drop Earrings");
        p2.setDescription("Classic pearl drop earrings for everyday elegance");
        p2.setPrice(499.0);
        p2.setImageUrl("https://images.pexels.com/photos/13155692/pexels-photo-13155692.jpeg?cs=srgb&w=400&h=400&fit=crop");
        productRepository.save(p2);

        Product p3 = new Product();
        p3.setName("Oxidized Silver Jhumkas");
        p3.setDescription("Traditional oxidized silver jhumka earrings");
        p3.setPrice(699.0);
        p3.setImageUrl("https://images.pexels.com/photos/10164658/pexels-photo-10164658.jpeg?cs=srgb&w=400&h=400&fit=crop");
        productRepository.save(p3);

        Product p4 = new Product();
        p4.setName("Kundan Choker Set");
        p4.setDescription("Bridal kundan choker necklace set with earrings");
        p4.setPrice(2499.0);
        p4.setImageUrl("https://images.pexels.com/photos/29043373/pexels-photo-29043373.jpeg?cs=srgb&w=400&h=400&fit=crop");
        productRepository.save(p4);

        return "Added 4 sample products!";
    }
}