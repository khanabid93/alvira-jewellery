package com.alvira.jewellerystore;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@Controller
public class WishlistController {

    @Autowired
    private WishlistItemRepository wishlistItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/wishlist/toggle/{productId}")
    public String toggleWishlist(@PathVariable Long productId,
                                 @RequestParam(required = false) String redirectTo,
                                 HttpSession session) {
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer == null) {
            return "redirect:/login";
        }

        WishlistItem existing = wishlistItemRepository.findByCustomerEmailAndProductId(customer.getEmail(), productId);
        String message;
        if (existing != null) {
            wishlistItemRepository.deleteByCustomerEmailAndProductId(customer.getEmail(), productId);
            message = "Removed+from+wishlist";
        } else {
            WishlistItem item = new WishlistItem();
            item.setCustomerEmail(customer.getEmail());
            item.setProductId(productId);
            wishlistItemRepository.save(item);
            message = "Added+to+wishlist!";
        }

        String base = redirectTo != null ? redirectTo : "/store";
        String separator = base.contains("?") ? "&" : "?";
        String toastParam = separator + "toast=" + message;

        int hashIndex = base.indexOf('#');
        if (hashIndex != -1) {
            String beforeHash = base.substring(0, hashIndex);
            String afterHash = base.substring(hashIndex);
            return "redirect:" + beforeHash + toastParam + afterHash;
        }
        return "redirect:" + base + toastParam;
    }

    @GetMapping("/wishlist")
    public String viewWishlist(Model model, HttpSession session) {
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer == null) {
            return "redirect:/login";
        }

        List<WishlistItem> wishlistItems = wishlistItemRepository.findByCustomerEmail(customer.getEmail());
        List<Product> products = wishlistItems.stream()
                .map(item -> productRepository.findById(item.getProductId()).orElse(null))
                .filter(p -> p != null)
                .toList();

        model.addAttribute("products", products);
        return "wishlist";
    }
}