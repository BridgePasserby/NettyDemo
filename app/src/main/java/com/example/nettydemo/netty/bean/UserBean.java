package com.example.nettydemo.netty.bean;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UserBean implements Serializable {
    @SerializedName("userId")
    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "UserBean{" +
                "userId='" + userId + '\'' +
                '}';
    }
}
