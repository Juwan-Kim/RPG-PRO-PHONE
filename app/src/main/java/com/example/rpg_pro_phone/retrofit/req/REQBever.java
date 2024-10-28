package com.example.rpg_pro_phone.retrofit.req;

import com.google.gson.annotations.SerializedName;

public class REQBever {
    @SerializedName("userkey")
    private String userKey;

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }
}
