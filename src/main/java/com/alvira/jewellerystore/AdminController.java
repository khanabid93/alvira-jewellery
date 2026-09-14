package com.alvira.jewellerystore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AdminController {
    @GetMapping("/admin")
    public String dashboard(org.springframework.security.core.Authentication auth, Model model) {
        java.util.List<Order> allOrders = orderRepository.findAll();
        java.util.List<Product> allProducts = productRepository.findAll();

        double totalRevenue = 0;
        for (Order o : allOrders) {
            if (o.getTotalAmount() != null) {
                totalRevenue += o.getTotalAmount();
            }
        }

        long lowStockCount = allProducts.stream()
                .filter(p -> p.getStockQuantity() != null && p.getStockQuantity() <= 5)
                .count();

        java.util.List<Order> recentOrders = allOrders.stream()
                .sorted((a, b) -> b.getOrderDate().compareTo(a.getOrderDate()))
                .limit(5)
                .toList();

        model.addAttribute("totalOrders", allOrders.size());
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalProducts", allProducts.size());
        model.addAttribute("lowStockCount", lowStockCount);
        model.addAttribute("recentOrders", recentOrders);
        model.addAttribute("currentAdminUser", getCurrentAdminUser(auth));
        model.addAttribute("canViewRevenue", hasPermission(auth, "revenue"));

        return "admin-dashboard";
    }
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @GetMapping("/admin/products")
    public String listProducts(org.springframework.security.core.Authentication auth, Model model) {
        if (!hasPermission(auth, "products")) return "redirect:/admin";
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("currentAdminUser", getCurrentAdminUser(auth));
        return "admin-products";
    }

    @GetMapping("/admin/products/new")
    public String showAddProductForm(org.springframework.security.core.Authentication auth, Model model) {
        if (!hasPermission(auth, "products")) return "redirect:/admin";
        model.addAttribute("product", new Product());
        model.addAttribute("currentAdminUser", getCurrentAdminUser(auth));
        return "admin-product-form";
    }

    @GetMapping("/admin/products/edit/{id}")
    public String showEditProductForm(@PathVariable Long id, org.springframework.security.core.Authentication auth, Model model) {
        if (!hasPermission(auth, "products")) return "redirect:/admin";
        Product product = productRepository.findById(id).orElse(null);
        model.addAttribute("product", product);
        model.addAttribute("currentAdminUser", getCurrentAdminUser(auth));
        return "admin-product-form";
    }

    @Autowired
    private CloudinaryService cloudinaryService;

    @PostMapping("/admin/products/save")
    public String saveProduct(@ModelAttribute Product product,
                              @RequestParam(required = false) org.springframework.web.multipart.MultipartFile imageFile,
                              @RequestParam(required = false) org.springframework.web.multipart.MultipartFile videoFile,
                              @RequestParam(required = false) org.springframework.web.multipart.MultipartFile[] additionalImageFiles,
                              org.springframework.security.core.Authentication auth) throws java.io.IOException {

        if (!hasPermission(auth, "products")) return "redirect:/admin";
        boolean isNewProduct = product.getId() == null;

        if (imageFile != null && !imageFile.isEmpty()) {
            product.setImageUrl(cloudinaryService.uploadFile(imageFile));
        }

        if (videoFile != null && !videoFile.isEmpty()) {
            product.setVideoUrl(cloudinaryService.uploadFile(videoFile));
        }

        if (additionalImageFiles != null && additionalImageFiles.length > 0) {
            StringBuilder paths = new StringBuilder();
            for (org.springframework.web.multipart.MultipartFile file : additionalImageFiles) {
                if (!file.isEmpty()) {
                    String url = cloudinaryService.uploadFile(file);
                    if (paths.length() > 0) {
                        paths.append(",");
                    }
                    paths.append(url);
                }
            }
            if (paths.length() > 0) {
                product.setAdditionalImages(paths.toString());
            }
        }

        productRepository.save(product);
        logActivity(auth, isNewProduct ? "Created Product" : "Updated Product", product.getName());
        return "redirect:/admin/products";
    }

    @GetMapping("/admin/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, org.springframework.security.core.Authentication auth) {
        if (!hasPermission(auth, "products")) return "redirect:/admin";
        Product product = productRepository.findById(id).orElse(null);
        String name = product != null ? product.getName() : ("ID " + id);
        productRepository.deleteById(id);
        logActivity(auth, "Deleted Product", name);
        return "redirect:/admin/products";
    }
    @GetMapping("/admin/orders")
    public String listOrders(@RequestParam(required = false) String search,
                             @RequestParam(required = false) String paymentMode,
                             @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd") java.time.LocalDate orderDate,
                             org.springframework.security.core.Authentication auth,
                             Model model) {

        if (!hasPermission(auth, "orders")) return "redirect:/admin";

        java.util.List<Order> orders = orderRepository.findAll();

        if (search != null && !search.trim().isEmpty()) {
            String q = search.trim().toLowerCase();
            orders = orders.stream()
                    .filter(o -> (o.getCustomerName() != null && o.getCustomerName().toLowerCase().contains(q))
                            || (o.getPhoneNumber() != null && o.getPhoneNumber().toLowerCase().contains(q))
                            || (o.getOrderSummary() != null && o.getOrderSummary().toLowerCase().contains(q))
                            || String.valueOf(o.getId()).equals(q))
                    .toList();
        }

        if (paymentMode != null && !paymentMode.isEmpty()) {
            orders = orders.stream()
                    .filter(o -> paymentMode.equals(o.getPaymentMode()))
                    .toList();
        }

        if (orderDate != null) {
            orders = orders.stream()
                    .filter(o -> o.getOrderDate() != null && o.getOrderDate().toLocalDate().equals(orderDate))
                    .toList();
        }

        orders = orders.stream()
                .sorted((a, b) -> b.getOrderDate().compareTo(a.getOrderDate()))
                .toList();

        model.addAttribute("orders", orders);
        model.addAttribute("searchQuery", search);
        model.addAttribute("selectedPaymentMode", paymentMode);
        model.addAttribute("selectedDate", orderDate);
        model.addAttribute("currentAdminUser", getCurrentAdminUser(auth));

        return "admin-orders";
    }

    @GetMapping("/admin/orders/edit/{id}")
    public String showEditOrderForm(@PathVariable Long id, org.springframework.security.core.Authentication auth, Model model) {
        if (!hasPermission(auth, "orders")) return "redirect:/admin";
        Order order = orderRepository.findById(id).orElse(null);
        model.addAttribute("order", order);
        model.addAttribute("currentAdminUser", getCurrentAdminUser(auth));
        return "admin-order-edit";
    }

    @PostMapping("/admin/orders/update/{id}")
    public String updateOrder(@PathVariable Long id,
                              @RequestParam String customerName,
                              @RequestParam String phoneNumber,
                              @RequestParam String address,
                              @RequestParam String city,
                              @RequestParam(required = false) String state,
                              @RequestParam String pincode,
                              @RequestParam(required = false) String paymentMode,
                              @RequestParam(required = false) String courierName,
                              @RequestParam(required = false) String awbCode,
                              @RequestParam(required = false) String shipmentStatus,
                              @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd") java.time.LocalDate estimatedDeliveryDate,
                              org.springframework.security.core.Authentication auth) {
        if (!hasPermission(auth, "orders")) return "redirect:/admin";
        Order order = orderRepository.findById(id).orElse(null);
        if (order != null) {
            order.setCustomerName(customerName);
            order.setPhoneNumber(phoneNumber);
            order.setAddress(address);
            order.setCity(city);
            order.setState(state);
            order.setPincode(pincode);
            order.setPaymentMode(paymentMode);
            order.setCourierName(courierName);
            order.setAwbCode(awbCode);
            order.setShipmentStatus(shipmentStatus);
            order.setEstimatedDeliveryDate(estimatedDeliveryDate);

            if (shipmentStatus != null) {
                if (shipmentStatus.equals("Pickup Scheduled") || shipmentStatus.equals("In Transit") || shipmentStatus.equals("Out for Delivery")) {
                    order.setStatus("SHIPPED");
                } else if (shipmentStatus.equals("Delivered")) {
                    order.setStatus("DELIVERED");
                    if (order.getDeliveredDate() == null) {
                        order.setDeliveredDate(java.time.LocalDateTime.now());
                    }
                }
            }

            orderRepository.save(order);
        }
        return "redirect:/admin/orders";
    }
    @GetMapping("/admin/orders/update-status/{id}")
    public String updateOrderStatus(@PathVariable Long id, @RequestParam String status, org.springframework.security.core.Authentication auth) {
        if (!hasPermission(auth, "orders")) return "redirect:/admin";
        Order order = orderRepository.findById(id).orElse(null);
        if (order != null) {
            order.setStatus(status);
            if ("DELIVERED".equals(status) && order.getDeliveredDate() == null) {
                order.setDeliveredDate(java.time.LocalDateTime.now());
            }
            orderRepository.save(order);
            logActivity(auth, "Updated Order Status", "Order #" + order.getId() + " → " + status);
        }
        return "redirect:/admin/orders";
    }
    @Autowired
    private CouponRepository couponRepository;

    @GetMapping("/admin/coupons")
    public String listCoupons(org.springframework.security.core.Authentication auth, Model model) {
        if (!hasPermission(auth, "coupons")) return "redirect:/admin";
        model.addAttribute("coupons", couponRepository.findAll());
        model.addAttribute("coupon", new Coupon());
        model.addAttribute("currentAdminUser", getCurrentAdminUser(auth));
        return "admin-coupons";
    }

    @PostMapping("/admin/coupons/save")
    public String saveCoupon(@ModelAttribute Coupon coupon, org.springframework.security.core.Authentication auth) {
        if (!hasPermission(auth, "coupons")) return "redirect:/admin";
        couponRepository.save(coupon);
        logActivity(auth, "Created/Updated Coupon", coupon.getCode());
        return "redirect:/admin/coupons";
    }

    @GetMapping("/admin/coupons/delete/{id}")
    public String deleteCoupon(@PathVariable Long id, org.springframework.security.core.Authentication auth) {
        if (!hasPermission(auth, "coupons")) return "redirect:/admin";
        Coupon coupon = couponRepository.findById(id).orElse(null);
        String code = coupon != null ? coupon.getCode() : ("ID " + id);
        couponRepository.deleteById(id);
        logActivity(auth, "Deleted Coupon", code);
        return "redirect:/admin/coupons";
    }

    @Autowired
    private QuantityOfferRepository quantityOfferRepository;

    @GetMapping("/admin/offers")
    public String listOffers(org.springframework.security.core.Authentication auth, Model model) {
        if (!hasPermission(auth, "offers")) return "redirect:/admin";
        model.addAttribute("offers", quantityOfferRepository.findAll());
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("offer", new QuantityOffer());
        model.addAttribute("currentAdminUser", getCurrentAdminUser(auth));
        return "admin-offers";
    }

    @PostMapping("/admin/offers/save")
    public String saveOffer(@ModelAttribute QuantityOffer offer, org.springframework.security.core.Authentication auth) {
        if (!hasPermission(auth, "offers")) return "redirect:/admin";
        quantityOfferRepository.save(offer);
        return "redirect:/admin/offers";
    }

    @GetMapping("/admin/offers/delete/{id}")
    public String deleteOffer(@PathVariable Long id, org.springframework.security.core.Authentication auth) {
        if (!hasPermission(auth, "offers")) return "redirect:/admin";
        quantityOfferRepository.deleteById(id);
        return "redirect:/admin/offers";
    }

    @GetMapping("/admin/offers/toggle/{id}")
    public String toggleOffer(@PathVariable Long id, org.springframework.security.core.Authentication auth) {
        if (!hasPermission(auth, "offers")) return "redirect:/admin";
        QuantityOffer offer = quantityOfferRepository.findById(id).orElse(null);
        if (offer != null) {
            offer.setActive(!offer.isActive());
            quantityOfferRepository.save(offer);
        }
        return "redirect:/admin/offers";
    }

    @Autowired
    private ReturnRequestRepository returnRequestRepository;

    @GetMapping("/admin/returns")
    public String listReturns(@RequestParam(required = false) String status,
                              @RequestParam(required = false) String reason,
                              @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd") java.time.LocalDate fromDate,
                              @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd") java.time.LocalDate toDate,
                              org.springframework.security.core.Authentication auth,
                              Model model) {
        if (!hasPermission(auth, "orders")) return "redirect:/admin";
        java.util.List<ReturnRequest> returns = returnRequestRepository.findAll();

        if (status != null && !status.isEmpty()) {
            returns = returns.stream().filter(r -> status.equals(r.getStatus())).toList();
        }
        if (reason != null && !reason.isEmpty()) {
            returns = returns.stream().filter(r -> reason.equals(r.getReason())).toList();
        }
        if (fromDate != null) {
            returns = returns.stream().filter(r -> !r.getRequestedAt().toLocalDate().isBefore(fromDate)).toList();
        }
        if (toDate != null) {
            returns = returns.stream().filter(r -> !r.getRequestedAt().toLocalDate().isAfter(toDate)).toList();
        }

        returns = returns.stream()
                .sorted((a, b) -> b.getRequestedAt().compareTo(a.getRequestedAt()))
                .toList();

        java.util.Map<Long, Order> ordersById = new java.util.HashMap<>();
        for (ReturnRequest r : returns) {
            Order order = orderRepository.findById(r.getOrderId()).orElse(null);
            if (order != null) {
                ordersById.put(r.getOrderId(), order);
            }
        }

        model.addAttribute("returns", returns);
        model.addAttribute("ordersById", ordersById);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedReason", reason);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("currentAdminUser", getCurrentAdminUser(auth));
        return "admin-returns";
    }

    @PostMapping("/admin/returns/update/{id}")
    public String updateReturnStatus(@PathVariable Long id,
                                     @RequestParam String status,
                                     @RequestParam(required = false) String adminNotes,
                                     @RequestParam(required = false) String rejectionReason,
                                     org.springframework.security.core.Authentication auth) {
        if (!hasPermission(auth, "orders")) return "redirect:/admin";
        ReturnRequest request = returnRequestRepository.findById(id).orElse(null);
        if (request != null) {
            request.setStatus(status);
            request.setAdminNotes(adminNotes);
            request.setRejectionReason("REJECTED".equals(status) ? rejectionReason : null);
            returnRequestRepository.save(request);
        }
        return "redirect:/admin/returns";
    }
    @Autowired
    private CustomerRepository customerRepository;

    @GetMapping("/admin/reports")
    public String showReports(@RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd") java.time.LocalDate fromDate,
                              @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd") java.time.LocalDate toDate,
                              @RequestParam(required = false) String paymentMode,
                              @RequestParam(required = false) String productName,
                              org.springframework.security.core.Authentication auth,
                              Model model) {
        if (!hasPermission(auth, "reports")) return "redirect:/admin";
        java.util.List<Order> allOrders = orderRepository.findAll();
        java.util.List<Product> allProducts = productRepository.findAll();

        if (fromDate != null) {
            allOrders = allOrders.stream()
                    .filter(o -> o.getOrderDate() != null && !o.getOrderDate().toLocalDate().isBefore(fromDate))
                    .toList();
        }
        if (toDate != null) {
            allOrders = allOrders.stream()
                    .filter(o -> o.getOrderDate() != null && !o.getOrderDate().toLocalDate().isAfter(toDate))
                    .toList();
        }
        if (paymentMode != null && !paymentMode.isEmpty()) {
            allOrders = allOrders.stream()
                    .filter(o -> paymentMode.equals(o.getPaymentMode()))
                    .toList();
        }
        if (productName != null && !productName.trim().isEmpty()) {
            String q = productName.trim().toLowerCase();
            allOrders = allOrders.stream()
                    .filter(o -> o.getOrderSummary() != null && o.getOrderSummary().toLowerCase().contains(q))
                    .toList();
        }

        // Revenue by day (last 14 days)
        java.util.Map<String, Double> revenueByDay = new java.util.LinkedHashMap<>();
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd MMM");
        for (int i = 13; i >= 0; i--) {
            java.time.LocalDate day = java.time.LocalDate.now().minusDays(i);
            revenueByDay.put(day.format(fmt), 0.0);
        }
        for (Order o : allOrders) {
            if (o.getOrderDate() != null && o.getTotalAmount() != null) {
                java.time.LocalDate day = o.getOrderDate().toLocalDate();
                String key = day.format(fmt);
                if (revenueByDay.containsKey(key)) {
                    revenueByDay.put(key, revenueByDay.get(key) + o.getTotalAmount());
                }
            }
        }

        // Order status breakdown
        java.util.Map<String, Long> statusBreakdown = new java.util.LinkedHashMap<>();
        statusBreakdown.put("PLACED", allOrders.stream().filter(o -> "PLACED".equals(o.getStatus())).count());
        statusBreakdown.put("SHIPPED", allOrders.stream().filter(o -> "SHIPPED".equals(o.getStatus())).count());
        statusBreakdown.put("DELIVERED", allOrders.stream().filter(o -> "DELIVERED".equals(o.getStatus())).count());

        // Payment mode breakdown
        java.util.Map<String, Long> paymentBreakdown = new java.util.LinkedHashMap<>();
        for (Order o : allOrders) {
            String mode = o.getPaymentMode() != null ? o.getPaymentMode() : "Unknown";
            paymentBreakdown.merge(mode, 1L, Long::sum);
        }

        // Top 5 products by units sold (parsed from orderSummary text)
        java.util.Map<String, Integer> productUnits = new java.util.HashMap<>();
        for (Order o : allOrders) {
            if (o.getOrderSummary() == null) continue;
            for (String part : o.getOrderSummary().split(",")) {
                part = part.trim();
                if (part.isEmpty()) continue;
                int xIndex = part.lastIndexOf(" x");
                if (xIndex > 0) {
                    String name = part.substring(0, xIndex).trim();
                    String qtyStr = part.substring(xIndex + 2).trim();
                    try {
                        int qty = Integer.parseInt(qtyStr);
                        productUnits.merge(name, qty, Integer::sum);
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        java.util.List<java.util.Map.Entry<String, Integer>> topProducts = productUnits.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .limit(5)
                .toList();

        double totalRevenue = allOrders.stream().mapToDouble(o -> o.getTotalAmount() != null ? o.getTotalAmount() : 0).sum();
        double avgOrderValue = allOrders.isEmpty() ? 0 : totalRevenue / allOrders.size();

        double maxDayRevenue = revenueByDay.values().stream().mapToDouble(Double::doubleValue).max().orElse(0);

        model.addAttribute("totalOrders", allOrders.size());
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("avgOrderValue", Math.round(avgOrderValue * 100.0) / 100.0);
        model.addAttribute("totalProducts", allProducts.size());
        model.addAttribute("totalCustomers", customerRepository.count());
        model.addAttribute("revenueByDay", revenueByDay);
        model.addAttribute("maxDayRevenue", maxDayRevenue);
        model.addAttribute("statusBreakdown", statusBreakdown);
        model.addAttribute("paymentBreakdown", paymentBreakdown);
        model.addAttribute("topProducts", topProducts);

        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("selectedPaymentMode", paymentMode);
        model.addAttribute("productName", productName);
        model.addAttribute("currentAdminUser", getCurrentAdminUser(auth));
        model.addAttribute("canViewRevenue", hasPermission(auth, "revenue"));

        return "admin-reports";
    }

    @GetMapping("/admin/export/orders")
    public void exportOrders(jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"orders.csv\"");

        java.io.PrintWriter writer = response.getWriter();
        writer.println("Order ID,Customer Name,Phone,Email,Address,City,State,Pincode,Payment Mode,Items,Total Amount,Status,Order Date");
        for (Order o : orderRepository.findAll()) {
            writer.println(csvRow(
                    String.valueOf(o.getId()),
                    o.getCustomerName(),
                    o.getPhoneNumber(),
                    o.getCustomerEmail(),
                    o.getAddress(),
                    o.getCity(),
                    o.getState(),
                    o.getPincode(),
                    o.getPaymentMode(),
                    o.getOrderSummary(),
                    o.getTotalAmount() != null ? String.valueOf(o.getTotalAmount()) : "",
                    o.getStatus(),
                    o.getOrderDate() != null ? o.getOrderDate().toString() : ""
            ));
        }
        writer.flush();
    }

    @GetMapping("/admin/export/products")
    public void exportProducts(jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"products.csv\"");

        java.io.PrintWriter writer = response.getWriter();
        writer.println("Product ID,Name,Category,Price,Original Price,Stock,Weight,Featured,Created At");
        for (Product p : productRepository.findAll()) {
            writer.println(csvRow(
                    String.valueOf(p.getId()),
                    p.getName(),
                    p.getCategory(),
                    p.getPrice() != null ? String.valueOf(p.getPrice()) : "",
                    p.getOriginalPrice() != null ? String.valueOf(p.getOriginalPrice()) : "",
                    p.getStockQuantity() != null ? String.valueOf(p.getStockQuantity()) : "",
                    p.getWeight() != null ? String.valueOf(p.getWeight()) : "",
                    p.getFeatured() != null && p.getFeatured() ? "Yes" : "No",
                    p.getCreatedAt() != null ? p.getCreatedAt().toString() : ""
            ));
        }
        writer.flush();
    }
    @Autowired
    private LogisticsPartnerRepository logisticsPartnerRepository;

    @Autowired
    private ShippingRuleRepository shippingRuleRepository;

    @GetMapping("/admin/logistics")
    public String showLogisticsManagement(org.springframework.security.core.Authentication auth, Model model) {
        if (!hasPermission(auth, "logistics")) return "redirect:/admin";
        model.addAttribute("partners", logisticsPartnerRepository.findAll());
        model.addAttribute("rules", shippingRuleRepository.findAllByOrderByPriorityAsc());
        model.addAttribute("newPartner", new LogisticsPartner());
        model.addAttribute("newRule", new ShippingRule());

        java.util.Map<Long, String> partnerNames = new java.util.HashMap<>();
        for (LogisticsPartner p : logisticsPartnerRepository.findAll()) {
            partnerNames.put(p.getId(), p.getPartnerName());
        }
        model.addAttribute("partnerNames", partnerNames);
        model.addAttribute("currentAdminUser", getCurrentAdminUser(auth));

        return "admin-logistics";
    }

    @PostMapping("/admin/logistics/partner/save")
    public String savePartner(@ModelAttribute LogisticsPartner partner, org.springframework.security.core.Authentication auth) {
        if (!hasPermission(auth, "logistics")) return "redirect:/admin";
        logisticsPartnerRepository.save(partner);
        return "redirect:/admin/logistics";
    }

    @GetMapping("/admin/logistics/partner/edit/{id}")
    public String editPartner(@PathVariable Long id, org.springframework.security.core.Authentication auth, Model model) {
        if (!hasPermission(auth, "logistics")) return "redirect:/admin";
        model.addAttribute("partners", logisticsPartnerRepository.findAll());
        model.addAttribute("rules", shippingRuleRepository.findAllByOrderByPriorityAsc());
        model.addAttribute("newPartner", logisticsPartnerRepository.findById(id).orElse(new LogisticsPartner()));
        model.addAttribute("newRule", new ShippingRule());

        java.util.Map<Long, String> partnerNames = new java.util.HashMap<>();
        for (LogisticsPartner p : logisticsPartnerRepository.findAll()) {
            partnerNames.put(p.getId(), p.getPartnerName());
        }
        model.addAttribute("partnerNames", partnerNames);
        model.addAttribute("currentAdminUser", getCurrentAdminUser(auth));

        return "admin-logistics";
    }

    @GetMapping("/admin/logistics/partner/delete/{id}")
    public String deletePartner(@PathVariable Long id, org.springframework.security.core.Authentication auth) {
        if (!hasPermission(auth, "logistics")) return "redirect:/admin";
        logisticsPartnerRepository.deleteById(id);
        return "redirect:/admin/logistics";
    }

    @GetMapping("/admin/logistics/partner/toggle/{id}")
    public String togglePartner(@PathVariable Long id, org.springframework.security.core.Authentication auth) {
        if (!hasPermission(auth, "logistics")) return "redirect:/admin";
        LogisticsPartner partner = logisticsPartnerRepository.findById(id).orElse(null);
        if (partner != null) {
            partner.setEnabled(!partner.isEnabled());
            logisticsPartnerRepository.save(partner);
        }
        return "redirect:/admin/logistics";
    }

    @PostMapping("/admin/logistics/rule/save")
    public String saveRule(@ModelAttribute ShippingRule rule, org.springframework.security.core.Authentication auth) {
        if (!hasPermission(auth, "logistics")) return "redirect:/admin";
        shippingRuleRepository.save(rule);
        return "redirect:/admin/logistics";
    }

    @GetMapping("/admin/logistics/rule/delete/{id}")
    public String deleteRule(@PathVariable Long id, org.springframework.security.core.Authentication auth) {
        if (!hasPermission(auth, "logistics")) return "redirect:/admin";
        shippingRuleRepository.deleteById(id);
        return "redirect:/admin/logistics";
    }

    @GetMapping("/admin/logistics/rule/toggle/{id}")
    public String toggleRule(@PathVariable Long id, org.springframework.security.core.Authentication auth) {
        if (!hasPermission(auth, "logistics")) return "redirect:/admin";
        ShippingRule rule = shippingRuleRepository.findById(id).orElse(null);
        if (rule != null) {
            rule.setActive(!rule.isActive());
            shippingRuleRepository.save(rule);
        }
        return "redirect:/admin/logistics";
    }

    @Autowired
    private AbandonedCartRepository abandonedCartRepository;

    @GetMapping("/admin/abandoned-carts")
    public String listAbandonedCarts(org.springframework.security.core.Authentication auth, Model model) {
        if (!hasPermission(auth, "orders")) return "redirect:/admin";
        java.util.List<AbandonedCart> carts = abandonedCartRepository.findAll().stream()
                .filter(c -> !c.isRecovered())
                .sorted((a, b) -> b.getLastActivityAt().compareTo(a.getLastActivityAt()))
                .toList();

        long overOneHour = carts.stream()
                .filter(c -> c.getLastActivityAt().isBefore(java.time.LocalDateTime.now().minusHours(1)))
                .count();

        model.addAttribute("carts", carts);
        model.addAttribute("overOneHourCount", overOneHour);
        model.addAttribute("currentAdminUser", getCurrentAdminUser(auth));
        return "admin-abandoned-carts";
    }

    @GetMapping("/admin/abandoned-carts/mark-contacted/{id}")
    public String markContacted(@PathVariable Long id) {
        AbandonedCart cart = abandonedCartRepository.findById(id).orElse(null);
        if (cart != null) {
            cart.setReminderSent(true);
            abandonedCartRepository.save(cart);
        }
        return "redirect:/admin/abandoned-carts";
    }

    @GetMapping("/admin/customers")
    public String listCustomers(@RequestParam(required = false) String search,
                                @RequestParam(required = false) String status,
                                org.springframework.security.core.Authentication auth,
                                Model model) {
        if (!hasPermission(auth, "customers")) return "redirect:/admin";
        java.util.List<Customer> customers = customerRepository.findAll();

        if (search != null && !search.trim().isEmpty()) {
            String q = search.trim().toLowerCase();
            customers = customers.stream()
                    .filter(c -> (c.getFullName() != null && c.getFullName().toLowerCase().contains(q))
                            || (c.getEmail() != null && c.getEmail().toLowerCase().contains(q))
                            || (c.getPhoneNumber() != null && c.getPhoneNumber().contains(q)))
                    .toList();
        }

        if ("blocked".equals(status)) {
            customers = customers.stream().filter(Customer::isBlocked).toList();
        } else if ("active".equals(status)) {
            customers = customers.stream().filter(c -> !c.isBlocked()).toList();
        }

        java.util.List<Order> allOrders = orderRepository.findAll();
        java.util.Map<String, Integer> orderCountByEmail = new java.util.HashMap<>();
        java.util.Map<String, Double> totalSpentByEmail = new java.util.HashMap<>();
        java.util.Map<String, Integer> deliveredCountByEmail = new java.util.HashMap<>();
        java.util.Map<String, Integer> placedCountByEmail = new java.util.HashMap<>();
        java.util.Map<String, Integer> shippedCountByEmail = new java.util.HashMap<>();
        java.util.Map<String, Integer> returnedCountByEmail = new java.util.HashMap<>();

        java.util.List<ReturnRequest> allReturns = returnRequestRepository.findAll();
        java.util.Map<Long, Boolean> returnedOrderIds = new java.util.HashMap<>();
        for (ReturnRequest r : allReturns) {
            if ("APPROVED".equals(r.getStatus()) || "REFUNDED".equals(r.getStatus())) {
                returnedOrderIds.put(r.getOrderId(), true);
            }
        }

        for (Order o : allOrders) {
            if (o.getCustomerEmail() != null) {
                orderCountByEmail.merge(o.getCustomerEmail(), 1, Integer::sum);
                totalSpentByEmail.merge(o.getCustomerEmail(), o.getTotalAmount() != null ? o.getTotalAmount() : 0, Double::sum);

                if ("DELIVERED".equals(o.getStatus())) {
                    deliveredCountByEmail.merge(o.getCustomerEmail(), 1, Integer::sum);
                } else if ("PLACED".equals(o.getStatus())) {
                    placedCountByEmail.merge(o.getCustomerEmail(), 1, Integer::sum);
                } else if ("SHIPPED".equals(o.getStatus())) {
                    shippedCountByEmail.merge(o.getCustomerEmail(), 1, Integer::sum);
                }

                if (returnedOrderIds.containsKey(o.getId())) {
                    returnedCountByEmail.merge(o.getCustomerEmail(), 1, Integer::sum);
                }
            }
        }
        customers = customers.stream()
                .sorted((a, b) -> {
                    java.time.LocalDateTime aDate = a.getCreatedAt() != null ? a.getCreatedAt() : java.time.LocalDateTime.MIN;
                    java.time.LocalDateTime bDate = b.getCreatedAt() != null ? b.getCreatedAt() : java.time.LocalDateTime.MIN;
                    return bDate.compareTo(aDate);
                })
                .toList();

        model.addAttribute("customers", customers);
        model.addAttribute("orderCountByEmail", orderCountByEmail);
        model.addAttribute("totalSpentByEmail", totalSpentByEmail);
        model.addAttribute("deliveredCountByEmail", deliveredCountByEmail);
        model.addAttribute("returnedCountByEmail", returnedCountByEmail);
        model.addAttribute("searchQuery", search);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("currentAdminUser", getCurrentAdminUser(auth));
        return "admin-customers";
    }

    @GetMapping("/admin/customers/{id}")
    public String viewCustomerDetail(@PathVariable Long id, org.springframework.security.core.Authentication auth, Model model) {
        if (!hasPermission(auth, "customers")) return "redirect:/admin";
        Customer customer = customerRepository.findById(id).orElse(null);
        if (customer == null) {
            return "redirect:/admin/customers";
        }
        java.util.List<Order> customerOrders = orderRepository.findAll().stream()
                .filter(o -> customer.getEmail().equals(o.getCustomerEmail()))
                .sorted((a, b) -> b.getOrderDate().compareTo(a.getOrderDate()))
                .toList();

        model.addAttribute("customer", customer);
        model.addAttribute("orders", customerOrders);
        model.addAttribute("currentAdminUser", getCurrentAdminUser(auth));
        return "admin-customer-detail";
    }

    @PostMapping("/admin/customers/{id}/notes")
    public String saveCustomerNotes(@PathVariable Long id, @RequestParam String adminNotes, org.springframework.security.core.Authentication auth) {
        if (!hasPermission(auth, "customers")) return "redirect:/admin";
        Customer customer = customerRepository.findById(id).orElse(null);
        if (customer != null) {
            customer.setAdminNotes(adminNotes);
            customerRepository.save(customer);
        }
        return "redirect:/admin/customers/" + id;
    }

    @GetMapping("/admin/customers/{id}/toggle-block")
    public String toggleCustomerBlock(@PathVariable Long id, org.springframework.security.core.Authentication auth) {
        if (!hasPermission(auth, "customers")) return "redirect:/admin";
        Customer customer = customerRepository.findById(id).orElse(null);
        if (customer != null) {
            customer.setBlocked(!customer.isBlocked());
            customerRepository.save(customer);
            logActivity(auth, customer.isBlocked() ? "Blocked Customer" : "Unblocked Customer", customer.getFullName() + " (" + customer.getEmail() + ")");
        }
        return "redirect:/admin/customers/" + id;
    }

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    private AdminUser getCurrentAdminUser(org.springframework.security.core.Authentication auth) {
        if (auth == null) return null;
        return adminUserRepository.findByUsername(auth.getName());
    }

    private boolean hasPermission(org.springframework.security.core.Authentication auth, String permission) {
        AdminUser user = getCurrentAdminUser(auth);
        return user != null && user.hasPermission(permission);
    }

    @Autowired
    private ActivityLogRepository activityLogRepository;
    private void logActivity(org.springframework.security.core.Authentication auth, String action, String details) {
        ActivityLog log = new ActivityLog();
        log.setUsername(auth != null ? auth.getName() : "Unknown");
        log.setAction(action);
        log.setDetails(details);
        activityLogRepository.save(log);
    }

    @GetMapping("/admin/activity-log")
    public String viewActivityLog(@RequestParam(required = false) String username,
                                  org.springframework.security.core.Authentication auth,
                                  Model model) {
        AdminUser currentUser = adminUserRepository.findByUsername(auth.getName());
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return "redirect:/admin";
        }

        java.util.List<ActivityLog> logs = activityLogRepository.findAll();

        if (username != null && !username.trim().isEmpty()) {
            logs = logs.stream().filter(l -> username.equals(l.getUsername())).toList();
        }

        logs = logs.stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(200)
                .toList();

        java.util.Set<String> allUsernames = new java.util.TreeSet<>();
        for (AdminUser u : adminUserRepository.findAll()) {
            allUsernames.add(u.getUsername());
        }

        model.addAttribute("logs", logs);
        model.addAttribute("allUsernames", allUsernames);
        model.addAttribute("selectedUsername", username);
        model.addAttribute("currentAdminUser", currentUser);
        return "admin-activity-log";
    }

    @GetMapping("/admin/staff")
    public String listStaff(org.springframework.security.core.Authentication auth, Model model) {
        AdminUser currentUser = adminUserRepository.findByUsername(auth.getName());
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return "redirect:/admin";
        }
        model.addAttribute("staffList", adminUserRepository.findAll());
        model.addAttribute("newStaff", new AdminUser());
        return "admin-staff";
    }

    @PostMapping("/admin/staff/save")
    public String saveStaff(@ModelAttribute AdminUser staff,
                            @RequestParam(required = false) String newPassword,
                            org.springframework.security.core.Authentication auth) {
        AdminUser currentUser = adminUserRepository.findByUsername(auth.getName());
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return "redirect:/admin";
        }

        boolean isNewStaff = staff.getId() == null;

        if (staff.getId() != null) {
            // Editing existing staff — preserve password unless a new one is provided
            AdminUser existing = adminUserRepository.findById(staff.getId()).orElse(null);
            if (existing != null) {
                staff.setPassword(existing.getPassword());
                staff.setCreatedAt(existing.getCreatedAt());
            }
        } else {
            staff.setCreatedAt(java.time.LocalDateTime.now());
        }

        if (newPassword != null && !newPassword.trim().isEmpty()) {
            staff.setPassword(passwordEncoder.encode(newPassword));
        }

        adminUserRepository.save(staff);
        logActivity(auth, isNewStaff ? "Created Staff Account" : "Updated Staff Account", staff.getUsername() + " (" + staff.getRole() + ")");
        return "redirect:/admin/staff";
    }
    @GetMapping("/admin/staff/edit/{id}")
    public String editStaff(@PathVariable Long id, org.springframework.security.core.Authentication auth, Model model) {
        AdminUser currentUser = adminUserRepository.findByUsername(auth.getName());
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return "redirect:/admin";
        }
        model.addAttribute("staffList", adminUserRepository.findAll());
        model.addAttribute("newStaff", adminUserRepository.findById(id).orElse(new AdminUser()));
        return "admin-staff";
    }

    @GetMapping("/admin/staff/delete/{id}")
    public String deleteStaff(@PathVariable Long id, org.springframework.security.core.Authentication auth) {
        AdminUser currentUser = adminUserRepository.findByUsername(auth.getName());
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return "redirect:/admin";
        }
        if (!id.equals(currentUser.getId())) { // safety: can't delete yourself
            AdminUser toDelete = adminUserRepository.findById(id).orElse(null);
            String username = toDelete != null ? toDelete.getUsername() : ("ID " + id);
            adminUserRepository.deleteById(id);
            logActivity(auth, "Deleted Staff Account", username);
        }
        return "redirect:/admin/staff";
    }
    @GetMapping("/admin/export/returns")
    public void exportReturns(@RequestParam(required = false) String status,
                              @RequestParam(required = false) String reason,
                              @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd") java.time.LocalDate fromDate,
                              @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd") java.time.LocalDate toDate,
                              jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {

        java.util.List<ReturnRequest> returns = returnRequestRepository.findAll();

        if (status != null && !status.isEmpty()) {
            returns = returns.stream().filter(r -> status.equals(r.getStatus())).toList();
        }
        if (reason != null && !reason.isEmpty()) {
            returns = returns.stream().filter(r -> reason.equals(r.getReason())).toList();
        }
        if (fromDate != null) {
            returns = returns.stream().filter(r -> !r.getRequestedAt().toLocalDate().isBefore(fromDate)).toList();
        }
        if (toDate != null) {
            returns = returns.stream().filter(r -> !r.getRequestedAt().toLocalDate().isAfter(toDate)).toList();
        }

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"returns.csv\"");

        java.io.PrintWriter writer = response.getWriter();
        writer.println("Return ID,Order ID,Customer Email,Reason,Comments,Status,Rejection Reason,Admin Notes,Account Holder,Bank Name,Account Number,IFSC,Product Image URL,Label Image URL,Unboxing Video URL,Order Date,Delivered Date,Return Requested Date");

        for (ReturnRequest r : returns) {
            Order order = orderRepository.findById(r.getOrderId()).orElse(null);
            writer.println(csvRow(
                    String.valueOf(r.getId()),
                    String.valueOf(r.getOrderId()),
                    r.getCustomerEmail(),
                    r.getReason(),
                    r.getComments(),
                    r.getStatus(),
                    r.getRejectionReason(),
                    r.getAdminNotes(),
                    r.getAccountHolderName(),
                    r.getBankName(),
                    r.getAccountNumber(),
                    r.getIfscCode(),
                    r.getProductImagePath(),
                    r.getLabelImagePath(),
                    r.getUnboxingVideoPath(),
                    order != null && order.getOrderDate() != null ? order.getOrderDate().toString() : "",
                    order != null && order.getDeliveredDate() != null ? order.getDeliveredDate().toString() : "",
                    r.getRequestedAt() != null ? r.getRequestedAt().toString() : ""
            ));
        }
        writer.flush();
    }
    @GetMapping("/admin/export/customers")
    public void exportCustomers(@RequestParam(required = false) String search,
                                @RequestParam(required = false) String status,
                                jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        java.util.List<Customer> customers = customerRepository.findAll();

        if (search != null && !search.trim().isEmpty()) {
            String q = search.trim().toLowerCase();
            customers = customers.stream()
                    .filter(c -> (c.getFullName() != null && c.getFullName().toLowerCase().contains(q))
                            || (c.getEmail() != null && c.getEmail().toLowerCase().contains(q))
                            || (c.getPhoneNumber() != null && c.getPhoneNumber().contains(q)))
                    .toList();
        }
        if ("blocked".equals(status)) {
            customers = customers.stream().filter(Customer::isBlocked).toList();
        } else if ("active".equals(status)) {
            customers = customers.stream().filter(c -> !c.isBlocked()).toList();
        }

        java.util.List<Order> allOrders = orderRepository.findAll();
        java.util.Map<String, Integer> orderCountByEmail = new java.util.HashMap<>();
        java.util.Map<String, Double> totalSpentByEmail = new java.util.HashMap<>();
        for (Order o : allOrders) {
            if (o.getCustomerEmail() != null) {
                orderCountByEmail.merge(o.getCustomerEmail(), 1, Integer::sum);
                totalSpentByEmail.merge(o.getCustomerEmail(), o.getTotalAmount() != null ? o.getTotalAmount() : 0, Double::sum);
            }
        }

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"customers.csv\"");

        java.io.PrintWriter writer = response.getWriter();
        writer.println("Customer ID,Full Name,Email,Phone,Joined Date,Total Orders,Total Spent,Status,Admin Notes");
        for (Customer c : customers) {
            writer.println(csvRow(
                    String.valueOf(c.getId()),
                    c.getFullName(),
                    c.getEmail(),
                    c.getPhoneNumber(),
                    c.getCreatedAt() != null ? c.getCreatedAt().toString() : "",
                    String.valueOf(orderCountByEmail.getOrDefault(c.getEmail(), 0)),
                    String.valueOf(totalSpentByEmail.getOrDefault(c.getEmail(), 0.0)),
                    c.isBlocked() ? "Blocked" : "Active",
                    c.getAdminNotes()
            ));
        }
        writer.flush();
    }

    private String csvRow(String... fields) {
        StringBuilder row = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i] != null ? fields[i] : "";
            field = field.replace("\"", "\"\"");
            row.append("\"").append(field).append("\"");
            if (i < fields.length - 1) row.append(",");
        }
        return row.toString();
    }
}