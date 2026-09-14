package com.alvira.jewellerystore;

import jakarta.persistence.*;

@Entity
public class QuantityOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // admin-facing label, e.g. "Buy 2 Get 10% Off"

    private String scope; // "ALL_PRODUCTS" or "SPECIFIC_PRODUCT"
    private Long productId; // used only when scope = SPECIFIC_PRODUCT

    private Integer triggerQuantity; // e.g. 2, 4

    private String discountType; // "PERCENTAGE_EXTRA" or "PAY_FOR_X"
    private Double discountValue; // e.g. 10 (%) or 3 (pay for 3 of 4)

    private boolean active = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Integer getTriggerQuantity() { return triggerQuantity; }
    public void setTriggerQuantity(Integer triggerQuantity) { this.triggerQuantity = triggerQuantity; }

    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }

    public Double getDiscountValue() { return discountValue; }
    public void setDiscountValue(Double discountValue) { this.discountValue = discountValue; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean appliesToProduct(Long productId) {
        return "ALL_PRODUCTS".equals(scope) || (productId != null && productId.equals(this.productId));
    }

    // Customer-facing badge text, e.g. "Buy 3, get 10% off!" or "Buy 4, pay for only 3!"
    public String getBadgeText() {
        String rewardText = "PERCENTAGE_EXTRA".equals(discountType)
                ? discountValue.intValue() + "% off"
                : "pay for only " + discountValue.intValue();
        return "Buy " + triggerQuantity + ", get " + rewardText + "!";
    }
}