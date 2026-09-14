package com.alvira.jewellerystore;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class ReturnController {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CloudinaryService cloudinaryService;
    @Autowired
    private ReturnRequestRepository returnRequestRepository;

    // Shows all of the customer's delivered orders, marking which are still return-eligible
    @GetMapping("/my-returns")
    public String myReturnableOrders(Model model, HttpSession session) {
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer == null) {
            return "redirect:/login";
        }

        List<Order> allOrders = orderRepository.findAll().stream()
                .filter(o -> customer.getEmail().equals(o.getCustomerEmail()))
                .filter(o -> "DELIVERED".equals(o.getStatus()))
                .sorted((a, b) -> b.getOrderDate().compareTo(a.getOrderDate()))
                .toList();

        model.addAttribute("orders", allOrders);
        model.addAttribute("now", LocalDateTime.now());

        java.util.Map<Long, ReturnRequest> returnsByOrderId = new java.util.HashMap<>();
        for (ReturnRequest r : returnRequestRepository.findByCustomerEmail(customer.getEmail())) {
            returnsByOrderId.put(r.getOrderId(), r);
        }
        model.addAttribute("returnsByOrderId", returnsByOrderId);

        return "returns-my-orders";
    }

    @GetMapping("/returns/initiate/{orderId}")
    public String showReturnForm(@PathVariable Long orderId, Model model, HttpSession session) {
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer == null) {
            return "redirect:/login";
        }

        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null || !customer.getEmail().equals(order.getCustomerEmail())) {
            return "redirect:/my-returns";
        }

        if (!isEligible(order)) {
            return "redirect:/my-returns";
        }

        if (returnRequestRepository.findByOrderId(orderId) != null) {
            return "redirect:/my-returns";
        }

        model.addAttribute("order", order);
        return "returns-initiate";
    }
    @GetMapping("/returns/reupload/{id}")
    public String showReuploadForm(@PathVariable Long id, Model model, HttpSession session) {
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer == null) {
            return "redirect:/login";
        }
        ReturnRequest request = returnRequestRepository.findById(id).orElse(null);
        if (request == null || !customer.getEmail().equals(request.getCustomerEmail()) || !"REJECTED".equals(request.getStatus())) {
            return "redirect:/account";
        }
        model.addAttribute("returnRequest", request);
        return "returns-reupload";
    }

    @PostMapping("/returns/reupload/{id}")
    public String submitReupload(@PathVariable Long id,
                                 @RequestParam(required = false) MultipartFile productImage,
                                 @RequestParam(required = false) MultipartFile labelImage,
                                 @RequestParam(required = false) MultipartFile unboxingVideo,
                                 HttpSession session) throws IOException {
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer == null) {
            return "redirect:/login";
        }
        ReturnRequest request = returnRequestRepository.findById(id).orElse(null);
        if (request == null || !customer.getEmail().equals(request.getCustomerEmail()) || !"REJECTED".equals(request.getStatus())) {
            return "redirect:/account";
        }

        if (productImage != null && !productImage.isEmpty()) {
            request.setProductImagePath(saveFile(productImage));
        }
        if (labelImage != null && !labelImage.isEmpty()) {
            request.setLabelImagePath(saveFile(labelImage));
        }
        if (unboxingVideo != null && !unboxingVideo.isEmpty()) {
            request.setUnboxingVideoPath(saveFile(unboxingVideo));
        }
        request.setStatus("REQUESTED");
        request.setRejectionReason(null);
        returnRequestRepository.save(request);

        return "redirect:/account";
    }
    @PostMapping("/returns/submit")
    public String submitReturn(@RequestParam Long orderId,
                               @RequestParam String reason,
                               @RequestParam(required = false) String comments,
                               @RequestParam String accountNumber,
                               @RequestParam String ifscCode,
                               @RequestParam String bankName,
                               @RequestParam String accountHolderName,
                               @RequestParam MultipartFile productImage,
                               @RequestParam MultipartFile labelImage,
                               @RequestParam MultipartFile unboxingVideo,
                               HttpSession session) throws IOException {

        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer == null) {
            return "redirect:/login";
        }

        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null || !customer.getEmail().equals(order.getCustomerEmail()) || !isEligible(order)) {
            return "redirect:/my-returns";
        }
        if (returnRequestRepository.findByOrderId(orderId) != null) {
            return "redirect:/my-returns";
        }

        ReturnRequest request = new ReturnRequest();
        request.setOrderId(orderId);
        request.setCustomerEmail(customer.getEmail());
        request.setReason(reason);
        request.setComments(comments);
        request.setAccountNumber(accountNumber);
        request.setIfscCode(ifscCode);
        request.setBankName(bankName);
        request.setAccountHolderName(accountHolderName);

        request.setProductImagePath(saveFile(productImage));
        request.setLabelImagePath(saveFile(labelImage));
        request.setUnboxingVideoPath(saveFile(unboxingVideo));

        returnRequestRepository.save(request);

        return "redirect:/my-returns";
    }

    private String saveFile(MultipartFile file) throws IOException {
        return cloudinaryService.uploadFile(file);
    }

    // Returns true if the order was delivered within the last 5 days
    private boolean isEligible(Order order) {
        if (order.getDeliveredDate() == null) {
            return false;
        }
        return order.getDeliveredDate().isAfter(LocalDateTime.now().minusDays(5));
    }
}