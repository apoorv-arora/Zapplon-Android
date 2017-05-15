package com.application.zapplon.data;

/**
 * Created by apoorvarora on 18/02/16.
 */
public class ConnectedAccount {

    private String accessToken;
    private String refreshToken;
    private long deletionTime;
    private int cabSessionId;
    private int cabCompany;
    private String cabCompanyName;

    public ConnectedAccount(){}

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public int getCabSessionId() {
        return cabSessionId;
    }

    public void setCabSessionId(int cabSessionId) {
        this.cabSessionId = cabSessionId;
    }

    public int getCabCompany() {
        return cabCompany;
    }

    public void setCabCompany(int cabCompany) {
        this.cabCompany = cabCompany;
    }

    public String getCabCompanyName() {
        return cabCompanyName;
    }

    public void setCabCompanyName(String cabCompanyName) {
        this.cabCompanyName = cabCompanyName;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public long getDeletionTime() {
        return deletionTime;
    }

    public void setDeletionTime(long deletionTime) {
        this.deletionTime = deletionTime;
    }
}
