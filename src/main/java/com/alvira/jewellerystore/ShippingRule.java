package com.alvira.jewellerystore;

import jakarta.persistence.*;

@Entity
public class ShippingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String conditionType = "WEIGHT"; // "WEIGHT" for now, extensible later (PINCODE, ORDER_VALUE, etc.)
    private String operator; // "LESS_THAN_EQUAL" or "GREATER_THAN"
    private Double weightThresholdGrams; // e.g. 500

    private Long partnerId; // which LogisticsPartner handles this condition

    private Integer priority = 1; // lower number = checked first
    private boolean active = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getConditionType() { return conditionType; }
    public void setConditionType(String conditionType) { this.conditionType = conditionType; }

    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }

    public Double getWeightThresholdGrams() { return weightThresholdGrams; }
    public void setWeightThresholdGrams(Double weightThresholdGrams) { this.weightThresholdGrams = weightThresholdGrams; }

    public Long getPartnerId() { return partnerId; }
    public void setPartnerId(Long partnerId) { this.partnerId = partnerId; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    // Checks if a given order weight (in grams) matches this rule's condition
    public boolean matches(double orderWeightGrams) {
        if (weightThresholdGrams == null) return false;
        if ("LESS_THAN_EQUAL".equals(operator)) {
            return orderWeightGrams <= weightThresholdGrams;
        } else if ("GREATER_THAN".equals(operator)) {
            return orderWeightGrams > weightThresholdGrams;
        }
        return false;
    }

    // Human-readable description, e.g. "Weight ≤ 500g"
    public String getDescription() {
        String opText = "LESS_THAN_EQUAL".equals(operator) ? "≤" : ">";
        return "Weight " + opText + " " + (weightThresholdGrams != null ? weightThresholdGrams.intValue() : "?") + "g";
    }
}