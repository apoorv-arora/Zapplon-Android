package com.application.zapplon.data;

import java.io.Serializable;

public class Speak implements Serializable{
    private boolean speaker = true;
    private String textSet;

    public Speak(){
    }

    public void setSpeaker(Boolean speaker)
    {
        this.speaker = speaker;
    }

    public Boolean getSpeaker() {
        return speaker;
    }

    public String getReturnText()
    {
        return textSet;
    }

    public void setReturnText(String text)
    {
        this.textSet = text;
    }
}