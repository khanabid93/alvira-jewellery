package com.alvira.jewellerystore;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class CustomerAuthController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/signup")
    public String showSignup() {
        return "signup";
    }

    @PostMapping("/signup")
    public String doSignup(@RequestParam String fullName,
                           @RequestParam String email,
                           @RequestParam String phoneNumber,
                           @RequestParam String password,
                           Model model) {

        if (customerRepository.findByEmail(email) != null) {
            model.addAttribute("error", "An account with this email already exists.");
            return "signup";
        }

        Customer customer = new Customer();
        customer.setFullName(fullName);
        customer.setEmail(email);
        customer.setPhoneNumber(phoneNumber);
        customer.setPassword(passwordEncoder.encode(password));
        customerRepository.save(customer);

        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLogin() {
        return "customer-login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String email,
                          @RequestParam String password,
                          HttpSession session,
                          Model model) {

        Customer customer = customerRepository.findByEmail(email);

        if (customer == null || !passwordEncoder.matches(password, customer.getPassword())) {
            model.addAttribute("error", "Invalid email or password.");
            return "customer-login";
        }

        session.setAttribute("loggedInCustomer", customer);
        return "redirect:/";
    }

    @GetMapping("/customer-logout")
    public String customerLogout(HttpSession session) {
        session.removeAttribute("loggedInCustomer");
        return "redirect:/";
    }

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ReturnRequestRepository returnRequestRepository;
    @GetMapping("/orders/rate-redirect/{id}")
    public String rateRedirect(@PathVariable Long id, HttpSession session) {
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer == null) {
            return "redirect:/login";
        }
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null || !customer.getEmail().equals(order.getCustomerEmail()) || order.getOrderSummary() == null) {
            return "redirect:/account";
        }
        String[] items = order.getOrderSummary().split(",");
        String firstCleanName = null;
        for (String itemName : items) {
            String cleanName = itemName.trim().replaceAll(" x\\d+$", "");
            if (!cleanName.isEmpty()) {
                firstCleanName = cleanName;
                break;
            }
        }
        if (firstCleanName == null) {
            return "redirect:/account";
        }
        for (Product p : productRepository.findAll()) {
            if (p.getName().equals(firstCleanName)) {
                return "redirect:/product/" + p.getId() + "#reviewForm";
            }
        }
        return "redirect:/account";
    }
    @GetMapping("/orders/invoice/{id}")
    public String viewInvoice(@PathVariable Long id, HttpSession session, org.springframework.ui.Model model) {
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer == null) {
            return "redirect:/login";
        }
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null || !customer.getEmail().equals(order.getCustomerEmail())) {
            return "redirect:/account";
        }
        model.addAttribute("order", order);
        return "invoice";
    }

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/account")
    public String showAccount(HttpSession session, Model model) {
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer == null) {
            return "redirect:/login";
        }
        model.addAttribute("customer", customer);
        java.util.List<Order> orders = orderRepository.findByCustomerEmailOrderByOrderDateDesc(customer.getEmail());
        model.addAttribute("orders", orders);

        java.util.Map<Long, ReturnRequest> returnsByOrderId = new java.util.HashMap<>();
        for (ReturnRequest r : returnRequestRepository.findByCustomerEmail(customer.getEmail())) {
            returnsByOrderId.put(r.getOrderId(), r);
        }
        model.addAttribute("returnsByOrderId", returnsByOrderId);
        model.addAttribute("now", java.time.LocalDateTime.now());

        // Build a product-name-to-image lookup so order history can show thumbnails
        java.util.Map<String, String> productImagesByName = new java.util.HashMap<>();
        java.util.Map<String, Long> productIdsByName = new java.util.HashMap<>();
        for (Product p : productRepository.findAll()) {
            productImagesByName.put(p.getName(), p.getImageUrl());
            productIdsByName.put(p.getName(), p.getId());
        }
        model.addAttribute("productImagesByName", productImagesByName);
        model.addAttribute("productIdsByName", productIdsByName);

        return "account";
    }
}