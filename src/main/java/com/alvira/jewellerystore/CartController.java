package com.alvira.jewellerystore;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class CartController {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CouponRepository couponRepository;
    @Autowired
    private QuantityOfferRepository quantityOfferRepository;

    @PostMapping("/apply-coupon")
    public String applyCoupon(@RequestParam String couponCode, HttpSession session, Model model) {
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart == null) {
            cart = new Cart();
        }
        cart.recalculateOfferDiscount(quantityOfferRepository.findByActiveTrue());

        if (cart.getQuantityOfferDiscount() > 0) {
            model.addAttribute("couponError", "A quantity offer is already applied to your cart. Coupons can't be combined with quantity offers.");
        } else {
            Coupon coupon = couponRepository.findByCode(couponCode.trim().toUpperCase());

            if (coupon == null || !coupon.isActive()) {
                model.addAttribute("couponError", "Invalid or inactive coupon code.");
            } else if (coupon.getExpiryDate() != null && coupon.getExpiryDate().isBefore(java.time.LocalDate.now())) {
                model.addAttribute("couponError", "This coupon has expired.");
            } else if (cart.getSubtotal() < coupon.getMinOrderValue()) {
                model.addAttribute("couponError", "Minimum order value for this coupon is ₹" + coupon.getMinOrderValue());
            } else {
                double discount;
                if (coupon.getDiscountType().equals("PERCENTAGE")) {
                    discount = cart.getSubtotal() * (coupon.getDiscountValue() / 100);
                } else {
                    discount = coupon.getDiscountValue();
                }
                cart.setAppliedCouponCode(coupon.getCode());
                cart.setDiscountAmount(discount);
            }
        }

        session.setAttribute("cart", cart);
        model.addAttribute("cart", cart);
        model.addAttribute("activeOffers", quantityOfferRepository.findByActiveTrue());
        return "cart";
    }
    @GetMapping("/remove-from-cart/{id}")
    public String removeFromCart(@PathVariable Long id, HttpSession session) {
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart != null) {
            cart.removeProduct(id);
        }
        session.setAttribute("cart", cart);
        return "redirect:/cart";
    }

    @GetMapping("/update-cart-quantity/{id}")
    public String updateCartQuantity(@PathVariable Long id, @RequestParam int quantity,
                                     @RequestParam(required = false) String redirectTo, HttpSession session) {
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart != null) {
            cart.setQuantity(id, quantity);
        }
        session.setAttribute("cart", cart);
        return "redirect:" + (redirectTo != null ? redirectTo : "/cart");
    }
    @GetMapping("/clear-cart")
    public String clearCart(HttpSession session) {
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart != null) {
            cart.clearItems();
        }
        session.setAttribute("cart", cart);
        return "redirect:/cart";
    }

    @PostMapping("/apply-coupon-checkout")
    @ResponseBody
    public java.util.Map<String, Object> applyCouponCheckout(@RequestParam String couponCode, HttpSession session) {
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart == null) {
            cart = new Cart();
        }
        cart.recalculateOfferDiscount(quantityOfferRepository.findByActiveTrue());

        java.util.Map<String, Object> result = new java.util.HashMap<>();

        if (cart.getQuantityOfferDiscount() > 0) {
            result.put("error", "A quantity offer is already applied to your cart. Coupons can't be combined with quantity offers.");
        } else {
            Coupon coupon = couponRepository.findByCode(couponCode.trim().toUpperCase());

            if (coupon == null || !coupon.isActive()) {
                result.put("error", "Invalid or inactive coupon code.");
            } else if (coupon.getExpiryDate() != null && coupon.getExpiryDate().isBefore(java.time.LocalDate.now())) {
                result.put("error", "This coupon has expired.");
            } else if (cart.getSubtotal() < coupon.getMinOrderValue()) {
                result.put("error", "Minimum order value for this coupon is ₹" + coupon.getMinOrderValue());
            } else {
                double discount;
                if (coupon.getDiscountType().equals("PERCENTAGE")) {
                    discount = cart.getSubtotal() * (coupon.getDiscountValue() / 100);
                } else {
                    discount = coupon.getDiscountValue();
                }
                cart.setAppliedCouponCode(coupon.getCode());
                cart.setDiscountAmount(discount);
            }
        }

        session.setAttribute("cart", cart);

        result.put("subtotal", cart.getSubtotal());
        result.put("total", cart.getTotal());
        result.put("appliedCouponCode", cart.getAppliedCouponCode());
        result.put("discountAmount", cart.getDiscountAmount());
        result.put("quantityOfferDiscount", cart.getQuantityOfferDiscount());
        return result;
    }

    @GetMapping("/remove-coupon-checkout")
    @ResponseBody
    public java.util.Map<String, Object> removeCouponCheckout(HttpSession session) {
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart != null) {
            cart.removeCoupon();
        }
        session.setAttribute("cart", cart);

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("subtotal", cart != null ? cart.getSubtotal() : 0);
        result.put("total", cart != null ? cart.getTotal() : 0);
        result.put("appliedCouponCode", cart != null ? cart.getAppliedCouponCode() : null);
        result.put("discountAmount", cart != null ? cart.getDiscountAmount() : 0);
        result.put("quantityOfferDiscount", cart != null ? cart.getQuantityOfferDiscount() : 0);
        return result;
    }

    @GetMapping("/remove-coupon")
    public String removeCoupon(HttpSession session) {
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart != null) {
            cart.removeCoupon();
        }
        session.setAttribute("cart", cart);
        return "redirect:/cart";
    }
    @Autowired
    private OrderRepository orderRepository;

    @GetMapping("/buy-now/{id}")
    public String buyNow(@PathVariable Long id, HttpSession session) {
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart == null) {
            cart = new Cart();
        }

        Product product = productRepository.findById(id).orElse(null);
        if (product != null) {
            cart.addProduct(product);
        }

        session.setAttribute("cart", cart);
        return "redirect:/checkout";
    }
    @GetMapping("/add-to-cart/{id}")
    public String addToCart(@PathVariable Long id, @RequestParam(required = false) String redirectTo, HttpSession session) {
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart == null) {
            cart = new Cart();
        }

        Product product = productRepository.findById(id).orElse(null);
        if (product != null) {
            cart.addProduct(product);
        }

        session.setAttribute("cart", cart);

        String base = redirectTo != null ? redirectTo : "/store";
        String separator = base.contains("?") ? "&" : "?";
        String toastParam = separator + "toast=Added+to+cart!";

        int hashIndex = base.indexOf('#');
        if (hashIndex != -1) {
            String beforeHash = base.substring(0, hashIndex);
            String afterHash = base.substring(hashIndex);
            return "redirect:" + beforeHash + toastParam + afterHash;
        }
        return "redirect:" + base + toastParam;
    }

    @PostMapping("/place-order")
    public String placeOrder(
            @RequestParam String customerName,
            @RequestParam String phoneNumber,
            @RequestParam String flatHouseNo,
            @RequestParam String areaSector,
            @RequestParam(required = false) String landmark,
            @RequestParam String city,
            @RequestParam String state,
            @RequestParam String pincode,
            HttpSession session,
            Model model) {

        Cart cart = (Cart) session.getAttribute("cart");
        if (cart == null || cart.getItems().isEmpty()) {
            return "redirect:/store";
        }

        Customer loggedInCustomerCheck = (Customer) session.getAttribute("loggedInCustomer");
        if (loggedInCustomerCheck != null && loggedInCustomerCheck.isBlocked()) {
            return "redirect:/checkout?blocked=true";
        }

        cart.recalculateOfferDiscount(quantityOfferRepository.findByActiveTrue());

        // Combine the address parts into one readable line
        StringBuilder fullAddress = new StringBuilder();
        fullAddress.append(flatHouseNo).append(", ").append(areaSector);
        if (landmark != null && !landmark.trim().isEmpty()) {
            fullAddress.append(", Near ").append(landmark);
        }

        // Build a simple text summary of the order
        StringBuilder summary = new StringBuilder();
        for (CartItem item : cart.getItems()) {
            summary.append(item.getProduct().getName())
                    .append(" x")
                    .append(item.getQuantity())
                    .append(", ");
        }

        Order order = new Order();
        order.setCustomerName(customerName);
        order.setPhoneNumber(phoneNumber);
        order.setAddress(fullAddress.toString());
        order.setCity(city);
        order.setState(state);
        order.setPincode(pincode);
        order.setTotalAmount(cart.getTotal());
        order.setOrderSummary(summary.toString());

        Customer loggedInCustomer = (Customer) session.getAttribute("loggedInCustomer");
        if (loggedInCustomer != null) {
            order.setCustomerEmail(loggedInCustomer.getEmail());
        }
        AbandonedCart abandoned = abandonedCartRepository.findByPhoneNumber(phoneNumber);
        if (abandoned != null) {
            abandoned.setRecovered(true);
            abandonedCartRepository.save(abandoned);
        }
        orderRepository.save(order);

        // Clear the cart after placing the order
        session.removeAttribute("cart");

        model.addAttribute("order", order);
        return "order-confirmation";
    }

    @GetMapping("/cart")
    public String viewCart(Model model, HttpSession session) {
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart == null) {
            cart = new Cart();
        }
        java.util.List<QuantityOffer> activeOffers = quantityOfferRepository.findByActiveTrue();
        cart.recalculateOfferDiscount(activeOffers);
        session.setAttribute("cart", cart);
        model.addAttribute("cart", cart);
        model.addAttribute("activeOffers", activeOffers);
        return "cart";
    }
    @Autowired
    private AbandonedCartRepository abandonedCartRepository;

    @PostMapping("/track-abandoned-cart")
    @ResponseBody
    public String trackAbandonedCart(@RequestParam String phoneNumber, @RequestParam(required = false) String customerName, HttpSession session) {
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart == null || cart.getItems().isEmpty() || phoneNumber == null || phoneNumber.length() != 10) {
            return "ignored";
        }

        StringBuilder summary = new StringBuilder();
        for (CartItem item : cart.getItems()) {
            summary.append(item.getProduct().getName()).append(" x").append(item.getQuantity()).append(", ");
        }

        AbandonedCart existing = abandonedCartRepository.findByPhoneNumber(phoneNumber);
        if (existing == null) {
            existing = new AbandonedCart();
            existing.setPhoneNumber(phoneNumber);
        }
        existing.setCustomerName(customerName);
        existing.setCartSummary(summary.toString());
        existing.setCartTotal(cart.getTotal());
        existing.setLastActivityAt(java.time.LocalDateTime.now());
        existing.setRecovered(false);
        existing.setReminderSent(false);
        abandonedCartRepository.save(existing);

        return "tracked";
    }
    @GetMapping("/checkout")
    public String showCheckout(Model model, HttpSession session) {
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart == null) {
            cart = new Cart();
        }
        java.util.List<QuantityOffer> activeOffers = quantityOfferRepository.findByActiveTrue();
        cart.recalculateOfferDiscount(activeOffers);
        session.setAttribute("cart", cart);
        model.addAttribute("cart", cart);
        model.addAttribute("activeOffers", activeOffers);
        return "checkout";
    }
}