package com.alvira.jewellerystore;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ProductController {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CloudinaryService cloudinaryService;
    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private QuantityOfferRepository quantityOfferRepository;

    @GetMapping("/product/{id}")
    public String viewProduct(@PathVariable Long id, Model model, HttpSession session) {
        Product product = productRepository.findById(id).orElse(null);
        List<Review> reviews = reviewRepository.findByProductId(id);

        List<Product> relatedProducts = List.of();
        if (product != null && product.getCategory() != null) {
            relatedProducts = productRepository.findByCategory(product.getCategory()).stream()
                    .filter(p -> !p.getId().equals(id))
                    .limit(4)
                    .toList();
        }

        double avgRating = 0;
        int[] starCounts = new int[6]; // index 1-5 used
        if (!reviews.isEmpty()) {
            int sum = 0;
            for (Review r : reviews) {
                sum += r.getRating();
                starCounts[r.getRating()]++;
            }
            avgRating = (double) sum / reviews.size();
        }

        model.addAttribute("product", product);
        model.addAttribute("relatedProducts", relatedProducts);
        model.addAttribute("reviews", reviews);
        model.addAttribute("avgRating", Math.round(avgRating * 10.0) / 10.0);
        model.addAttribute("reviewCount", reviews.size());
        model.addAttribute("starCounts", starCounts);

        Cart cart = (Cart) session.getAttribute("cart");
        model.addAttribute("cartCount", cart != null ? cart.getItemCount() : 0);

        int quantityInCart = 0;
        if (cart != null) {
            for (CartItem item : cart.getItems()) {
                if (item.getProduct().getId().equals(id)) {
                    quantityInCart = item.getQuantity();
                    break;
                }
            }
        }
        model.addAttribute("quantityInCart", quantityInCart);

        List<QuantityOffer> activeOffers = quantityOfferRepository.findByActiveTrue();
        String offerBadge = null;
        for (QuantityOffer offer : activeOffers) {
            if (offer.appliesToProduct(id)) {
                offerBadge = offer.getBadgeText();
                break;
            }
        }
        model.addAttribute("offerBadge", offerBadge);

        boolean canReview = false;
        Customer loggedInCustomer = (Customer) session.getAttribute("loggedInCustomer");
        if (loggedInCustomer != null && product != null) {
            canReview = orderRepository.findByCustomerEmailOrderByOrderDateDesc(loggedInCustomer.getEmail()).stream()
                    .anyMatch(o -> "DELIVERED".equals(o.getStatus())
                            && o.getOrderSummary() != null
                            && o.getOrderSummary().contains(product.getName()));
        }
        model.addAttribute("canReview", canReview);
        model.addAttribute("isLoggedIn", loggedInCustomer != null);

        return "product-detail";
    }

    @Autowired
    private OrderRepository orderRepository;

    @PostMapping("/product/{id}/review")
    public String addReview(@PathVariable Long id,
                            @RequestParam String customerName,
                            @RequestParam int rating,
                            @RequestParam String comment,
                            @RequestParam(required = false) org.springframework.web.multipart.MultipartFile photo,
                            jakarta.servlet.http.HttpSession session) throws java.io.IOException {

        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer == null) {
            return "redirect:/login";
        }

        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            return "redirect:/product/" + id;
        }

        boolean hasPurchased = orderRepository.findByCustomerEmailOrderByOrderDateDesc(customer.getEmail()).stream()
                .anyMatch(o -> "DELIVERED".equals(o.getStatus())
                        && o.getOrderSummary() != null
                        && o.getOrderSummary().contains(product.getName()));

        if (!hasPurchased) {
            return "redirect:/product/" + id + "?reviewError=notpurchased";
        }

        Review review = new Review();
        review.setProductId(id);
        review.setCustomerName(customerName);
        review.setRating(rating);
        review.setComment(comment);

        if (photo != null && !photo.isEmpty()) {
            review.setPhotoPath(cloudinaryService.uploadFile(photo));
        }

        reviewRepository.save(review);
        return "redirect:/product/" + id;
    }
}