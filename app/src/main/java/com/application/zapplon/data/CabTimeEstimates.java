package com.application.zapplon.data;

/**
 * Created by apoorvarora on 30/01/16.
 */
public class CabTimeEstimates {

    private String productId;
    private String displayName;
    private long estimate;

    public CabTimeEstimates(){}

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public long getEstimate() {
        return estimate;
    }

    public void setEstimate(long estimate) {
        this.estimate = estimate;
    }
}
