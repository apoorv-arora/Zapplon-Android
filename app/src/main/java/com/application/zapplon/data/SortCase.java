package com.application.zapplon.data;

import java.io.Serializable;

/**
 * Created by Harsh on 6/8/2016.
 */

public class SortCase implements Serializable
{
    private int sortType;
    private String identifier;

    public SortCase(){

    }

    public int getSortType() {
        return sortType;
    }

    public void setSortType(int sortType) {
        this.sortType = sortType;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}