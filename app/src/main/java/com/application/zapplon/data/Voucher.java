package com.application.zapplon.data;

/**
 * Created by Lenovo on 03/30/2016.
 */
public class Voucher {

    private int voucherId;
    private String image_url;
    private String comapany_name;
    private String terms;

    private boolean isValid;
    private int zappsRequired;
    private double value;

    public Voucher(){

    }

    public int getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(int voucherId) {
        this.voucherId = voucherId;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getComapany_name() {
        return comapany_name;
    }

    public void setComapany_name(String comapany_name) {
        this.comapany_name = comapany_name;
    }

    public String getTerms() {
        return terms;
    }

    public void setTerms(String terms) {
        this.terms = terms;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setIsValid(boolean isValid) {
        this.isValid = isValid;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public int getZappsRequired() {
        return zappsRequired;
    }

    public void setZappsRequired(int zappsRequired) {
        this.zappsRequired = zappsRequired;
    }
}
