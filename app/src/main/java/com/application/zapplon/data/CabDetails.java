package com.application.zapplon.data;

import java.io.Serializable;

/**
 * Created by apoorvarora on 28/01/16.
 */
public class CabDetails implements Serializable {

    private int type; // should be one of TYPE_OLA, TYPE_UBER, TYPE_JUGNOO, TYPE_EASY
    private int subType; // should be one of CAB_SEDAN, CAB_COMPACT, CAB_LUXURY,
    // BIKE, AUTO

    private double waitingCostPerMinute; // price per minute
    private double ridingCostPerMinute ;
    private double costPerDistance; // price per km
    private double base; // base fare
    private Surcharge surcharge; // additional cost parameters
    private double cancellationFee;

    private long estimatedTimeOfArrival; // eta in seconds
    private String priceEstimate; // minimum price to reach
    private String timeEstimate; // minimum time to reach
    private int capacity; // capacity of the cab
    private double distance; // distance of the cab from the user
    private String productId; // unique identifier of the cab
    private String logoUrl;
    private String displayName;
    private int zapp_count;
    private int isRecommended;

    public CabDetails() {
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getSubType() {
        return subType;
    }

    public void setSubType(int type) {
        this.subType = type;
    }

    public double getCostPerDistance() {
        return costPerDistance;
    }

    public void setCostPerDistance(double costPerDistance) {
        this.costPerDistance = costPerDistance;
    }

    public long getEstimatedTimeOfArrival() {
        return estimatedTimeOfArrival;
    }

    public void setEstimatedTimeOfArrival(long estimatedTimeOfArrival) {
        this.estimatedTimeOfArrival = estimatedTimeOfArrival;
    }

    public String getPriceEstimate() {
        return priceEstimate;
    }

    public void setPriceEstimate(String priceEstimate) {
        this.priceEstimate = priceEstimate;
    }

    public String getTimeEstimate() {
        return timeEstimate;
    }

    public void setTimeEstimate(String timeEstimate) {
        this.timeEstimate = timeEstimate;
    }

    public double getBase() {
        return base;
    }

    public void setBase(double base) {
        this.base = base;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Surcharge getSurcharge() {
        return surcharge;
    }

    public void setSurcharge(Surcharge surcharge) {
        this.surcharge = surcharge;
    }

    public double getCancellationFee() {
        return cancellationFee;
    }

    public void setCancellationFee(double cancellationFee) {
        this.cancellationFee = cancellationFee;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public double getWaitingCostPerMinute() {
        return waitingCostPerMinute;
    }

    public void setWaitingCostPerMinute(double waitingCostPerMinute) {
        this.waitingCostPerMinute = waitingCostPerMinute;
    }

    public double getRidingCostPerMinute() {
        return ridingCostPerMinute;
    }

    public void setRidingCostPerMinute(double ridingCostPerMinute) {
        this.ridingCostPerMinute = ridingCostPerMinute;
    }

    public int getZapp_count() {
        return zapp_count;
    }

    public void setZapp_count(int zapp_count) {
        this.zapp_count = zapp_count;
    }

    public int getIsRecommended() {
        return isRecommended;
    }

    public void setIsRecommended(int isRecommended) {
        this.isRecommended = isRecommended;
    }
}
