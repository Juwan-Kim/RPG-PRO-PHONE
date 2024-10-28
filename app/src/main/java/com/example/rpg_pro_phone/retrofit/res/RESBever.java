package com.example.rpg_pro_phone.retrofit.res;

import com.google.gson.annotations.SerializedName;

public class RESBever {
    @SerializedName("firstbever")
    private String firstBever;
    @SerializedName("firstrate")
    private String firstRate;
    @SerializedName("secondbever")
    private String secondBever;
    @SerializedName("secondrate")
    private String secondRate;
    @SerializedName("thirdbever")
    private String thirdBever;
    @SerializedName("thirdrate")
    private String thirdRate;

    public String getFirstBever() {
        return firstBever;
    }

    public void setFirstBever(String firstBever) {
        this.firstBever = firstBever;
    }

    public String getFirstRate() {
        return firstRate;
    }

    public void setFirstRate(String firstRate) {
        this.firstRate = firstRate;
    }

    public String getSecondBever() {
        return secondBever;
    }

    public void setSecondBever(String secondBever) {
        this.secondBever = secondBever;
    }

    public String getSecondRate() {
        return secondRate;
    }

    public void setSecondRate(String secondRate) {
        this.secondRate = secondRate;
    }

    public String getThirdBever() {
        return thirdBever;
    }

    public void setThirdBever(String thirdBever) {
        this.thirdBever = thirdBever;
    }

    public String getThirdRate() {
        return thirdRate;
    }

    public void setThirdRate(String thirdRate) {
        this.thirdRate = thirdRate;
    }
}
