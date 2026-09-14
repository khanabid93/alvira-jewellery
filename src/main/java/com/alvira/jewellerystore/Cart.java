package com.alvira.jewellerystore;

import java.util.ArrayList;
import java.util.List;

public class Cart {

    private List<CartItem> items = new ArrayList<>();
    private String appliedCouponCode;
    private double discountAmount = 0;
    private double quantityOfferDiscount = 0;

    public List<CartItem> getItems() {
        return items;
    }
    public void removeProduct(Long productId) {
        items.removeIf(item -> item.getProduct().getId().equals(productId));
    }

    public void clearItems() {
        items.clear();
    }
    public void addProduct(Product product) {
        for (CartItem item : items) {
            if (item.getProduct().getId().equals(product.getId())) {
                item.setQuantity(item.getQuantity() + 1);
                return;
            }
        }
        items.add(new CartItem(product, 1));
    }

    public void setQuantity(Long productId, int quantity) {
        if (quantity <= 0) {
            removeProduct(productId);
            return;
        }
        for (CartItem item : items) {
            if (item.getProduct().getId().equals(productId)) {
                item.setQuantity(quantity);
                return;
            }
        }
    }

    public double getSubtotal() {
        double total = 0;
        for (CartItem item : items) {
            total += item.getTotalPrice();
        }
        return Math.round(total * 100.0) / 100.0;
    }

    public void recalculateOfferDiscount(List<QuantityOffer> offers) {
        double fullTotal = 0;
        double discountedTotal = 0;
        for (CartItem item : items) {
            fullTotal += item.getTotalPrice();
            discountedTotal += item.getDiscountedTotalPrice(offers);
        }
        this.quantityOfferDiscount = Math.round((fullTotal - discountedTotal) * 100.0) / 100.0;
    }

    public double getQuantityOfferDiscount() {
        return quantityOfferDiscount;
    }

    // Only one discount ever applies — whichever benefits the customer more
    public double getEffectiveDiscount() {
        return Math.max(discountAmount, quantityOfferDiscount);
    }

    public boolean isQuantityOfferActive() {
        return quantityOfferDiscount > 0 && quantityOfferDiscount >= discountAmount;
    }

    public boolean isCouponActiveDisplay() {
        return discountAmount > 0 && discountAmount > quantityOfferDiscount;
    }

    public double getTotal() {
        double total = getSubtotal() - getEffectiveDiscount();
        total = total < 0 ? 0 : total;
        return Math.round(total * 100.0) / 100.0;
    }

    public int getItemCount() {
        int count = 0;
        for (CartItem item : items) {
            count += item.getQuantity();
        }
        return count;
    }

    public String getAppliedCouponCode() {
        return appliedCouponCode;
    }

    public void setAppliedCouponCode(String appliedCouponCode) {
        this.appliedCouponCode = appliedCouponCode;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = Math.round(discountAmount * 100.0) / 100.0;
    }

    public void removeCoupon() {
        this.appliedCouponCode = null;
        this.discountAmount = 0;
    }
}