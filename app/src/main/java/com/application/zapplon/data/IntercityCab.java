package com.application.zapplon.data;

import java.io.Serializable;

public class IntercityCab implements Serializable {

    private int type; // should be one of TYPE_AHA, TYPE_RODER

    private int subType; // should be one of CAB_SEDAN, CAB_COMPACT, CAB_LUXURY

    private String logoUrl;
    private String cabImageUrl;
    private String displayName;

    private double costPerDistance;
    private double extraCostPerDistance;

    private double basePrice;
    private double advance;
    private double fare;
    private int capacity;
    private int cabType; // should be one of TYPE_AHA_ECONOMY,TYPE_AHA_PREMIUM

    private int structure; // should be one of ONLINE_CAB, OFFLINE_CAB
    private int modes; // should be one of ONE_WAY, ROUND_TRIP, MULTI_CITY,
    // SHARING

    private int availability; // checks the number of available cabs

    private String bookingId;
    private String pickupId;

    private String terms;  //terms and conditions for palyers

    public IntercityCab() {

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

    public void setSubType(int subType) {
        this.subType = subType;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getCabImageUrl() {
        return cabImageUrl;
    }

    public void setCabImageUrl(String cabImageUrl) {
        this.cabImageUrl = cabImageUrl;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public double getCostPerDistance() {
        return costPerDistance;
    }

    public void setCostPerDistance(double costPerDistance) {
        this.costPerDistance = costPerDistance;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }

    public double getAdvance() {
        return advance;
    }

    public void setAdvance(double advance) {
        this.advance = advance;
    }

    public double getFare() {
        return fare;
    }

    public void setFare(double fare) {
        this.fare = fare;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getStructure() {
        return structure;
    }

    public void setStructure(int structure) {
        this.structure = structure;
    }

    public int getModes() {
        return modes;
    }

    public void setModes(int modes) {
        this.modes = modes;
    }

    public int getAvailability() {
        return availability;
    }

    public void setAvailability(int availability) {
        this.availability = availability;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getPickupId() {
        return pickupId;
    }

    public void setPickupId(String pickupId) {
        this.pickupId = pickupId;
    }

    public int getCabType() {
        return cabType;
    }

    public void setCabType(int cabType) {
        this.cabType = cabType;
    }

    public String getTerms() {
        return terms;
    }

    public void setTerms(String terms) {
        this.terms = terms;
    }

    public double getExtraCostPerDistance() {
        return extraCostPerDistance;
    }

    public void setExtraCostPerDistance(double extraCostPerDistance) {
        this.extraCostPerDistance = extraCostPerDistance;
    }

}
