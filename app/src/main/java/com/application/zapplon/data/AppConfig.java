package com.application.zapplon.data;

import java.io.Serializable;

/**
 * Created by apoorvarora on 01/02/16.
 */
public class AppConfig implements Serializable {

    private String key;
    private String value;

    public AppConfig(){
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
