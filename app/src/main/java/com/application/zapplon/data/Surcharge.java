package com.application.zapplon.data;

/**
 * Created by Pratik on 18-May-16.
 */
public class Surcharge {

    private String name;
    private String type;
    private String description;
    private double value;

    public Surcharge(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

}
