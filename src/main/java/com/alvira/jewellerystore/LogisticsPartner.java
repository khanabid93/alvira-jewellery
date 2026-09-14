package com.alvira.jewellerystore;

import jakarta.persistence.*;

@Entity
public class LogisticsPartner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String partnerName; // e.g. "Delhivery", "Ekart", "Shiprocket", "Nimbuspost", "Bluedart"
    private String apiIdOrEmail;
    private String apiPasswordOrToken;
    private boolean enabled = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPartnerName() { return partnerName; }
    public void setPartnerName(String partnerName) { this.partnerName = partnerName; }

    public String getApiIdOrEmail() { return apiIdOrEmail; }
    public void setApiIdOrEmail(String apiIdOrEmail) { this.apiIdOrEmail = apiIdOrEmail; }

    public String getApiPasswordOrToken() { return apiPasswordOrToken; }
    public void setApiPasswordOrToken(String apiPasswordOrToken) { this.apiPasswordOrToken = apiPasswordOrToken; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}