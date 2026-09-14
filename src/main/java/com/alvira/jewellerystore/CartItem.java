package com.alvira.jewellerystore;

import java.util.List;

public class CartItem {

    private Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getTotalPrice() {
        return product.getPrice() * quantity;
    }

    // Finds the best-matching active offer for this item's product, regardless of whether quantity qualifies yet
    public QuantityOffer getBestPossibleOffer(List<QuantityOffer> offers) {
        QuantityOffer best = null;
        for (QuantityOffer offer : offers) {
            boolean matchesProduct = "ALL_PRODUCTS".equals(offer.getScope())
                    || (product.getId().equals(offer.getProductId()));
            if (!matchesProduct || offer.getTriggerQuantity() == null) continue;
            if (best == null || offer.getTriggerQuantity() > best.getTriggerQuantity()) {
                best = offer;
            }
        }
        return best;
    }

    // Finds the best-matching active offer that the current quantity already qualifies for
    public QuantityOffer getAppliedOffer(List<QuantityOffer> offers) {
        QuantityOffer best = null;
        for (QuantityOffer offer : offers) {
            boolean matchesProduct = "ALL_PRODUCTS".equals(offer.getScope())
                    || (product.getId().equals(offer.getProductId()));
            if (!matchesProduct) continue;
            if (offer.getTriggerQuantity() == null || quantity < offer.getTriggerQuantity()) continue;
            if (best == null || offer.getTriggerQuantity() > best.getTriggerQuantity()) {
                best = offer;
            }
        }
        return best;
    }

    // Returns a customer-facing nudge message if adding more would unlock a better offer
    public String getUpsellMessage(List<QuantityOffer> offers) {
        QuantityOffer possible = getBestPossibleOffer(offers);
        if (possible == null) return null;

        QuantityOffer applied = getAppliedOffer(offers);
        if (applied != null && applied.getTriggerQuantity().equals(possible.getTriggerQuantity())) {
            return null; // already got the best available offer
        }

        int needed = possible.getTriggerQuantity() - quantity;
        if (needed <= 0) return null;

        String rewardText = "PERCENTAGE_EXTRA".equals(possible.getDiscountType())
                ? possible.getDiscountValue().intValue() + "% off"
                : "pay for only " + possible.getDiscountValue().intValue();

        return "Add " + needed + " more to get " + rewardText + "!";
    }

    public double getDiscountedTotalPrice(List<QuantityOffer> offers) {
        QuantityOffer offer = getAppliedOffer(offers);
        if (offer == null) {
            return getTotalPrice();
        }
        double unitPrice = product.getPrice();
        if ("PERCENTAGE_EXTRA".equals(offer.getDiscountType())) {
            return getTotalPrice() * (1 - offer.getDiscountValue() / 100);
        } else if ("PAY_FOR_X".equals(offer.getDiscountType())) {
            int trigger = offer.getTriggerQuantity();
            int paidUnits = offer.getDiscountValue().intValue();
            int extraUnits = quantity - trigger;
            return (unitPrice * paidUnits) + (unitPrice * Math.max(extraUnits, 0));
        }
        return getTotalPrice();
    }
}