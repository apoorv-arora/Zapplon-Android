package com.application.zapplon.data;

/**
 * Created by apoorvarora on 16/03/16.
 */
public class Tracking {

    private int trackingId;
    private String status;
    private int bookingStatus;
    private String crn;
    private double driverLatitude;
    private double driverLongitude;
    private double moneyBalance;

    // trip info
    private double amount;
    private double payableAmount;
    private double distance_value;
    private String distance_unit;
    private double wait_time_value;
    private String wait_time_unit;
    private double discount;
    private double advance;

    private int userId;
    private String token;
    private long tokenDeletionTime;
    private long timestamp;

    public Tracking() {
    }

    public int getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(int trackingId) {
        this.trackingId = trackingId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(int bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public String getCrn() {
        return crn;
    }

    public void setCrn(String crn) {
        this.crn = crn;
    }

    public double getDriverLatitude() {
        return driverLatitude;
    }

    public void setDriverLatitude(double driverLatitude) {
        this.driverLatitude = driverLatitude;
    }

    public double getDriverLongitude() {
        return driverLongitude;
    }

    public void setDriverLongitude(double driverLongitude) {
        this.driverLongitude = driverLongitude;
    }

    public double getMoneyBalance() {
        return moneyBalance;
    }

    public void setMoneyBalance(double moneyBalance) {
        this.moneyBalance = moneyBalance;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getPayableAmount() {
        return payableAmount;
    }

    public void setPayableAmount(double payableAmount) {
        this.payableAmount = payableAmount;
    }

    public double getDistance_value() {
        return distance_value;
    }

    public void setDistance_value(double distance_value) {
        this.distance_value = distance_value;
    }

    public String getDistance_unit() {
        return distance_unit;
    }

    public void setDistance_unit(String distance_unit) {
        this.distance_unit = distance_unit;
    }

    public double getWait_time_value() {
        return wait_time_value;
    }

    public void setWait_time_value(double wait_time_value) {
        this.wait_time_value = wait_time_value;
    }

    public String getWait_time_unit() {
        return wait_time_unit;
    }

    public void setWait_time_unit(String wait_time_unit) {
        this.wait_time_unit = wait_time_unit;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getAdvance() {
        return advance;
    }

    public void setAdvance(double advance) {
        this.advance = advance;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getTokenDeletionTime() {
        return tokenDeletionTime;
    }

    public void setTokenDeletionTime(long tokenDeletionTime) {
        this.tokenDeletionTime = tokenDeletionTime;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
