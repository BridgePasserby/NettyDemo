package com.example.nettydemo.netty.bean;

import java.io.Serializable;
import com.google.gson.annotations.SerializedName;


public class PushBean<T> implements Serializable {
    /**
     * Android一体机
     */
    private static final int MODULE_ANDROID_PAD = 6;

    @SerializedName("Cmd")
    private int Cmd;
    @SerializedName("Module")
    private int Module = MODULE_ANDROID_PAD;

    @SerializedName("Data")
    private T Data;

    public int getCmd() {
        return Cmd;
    }

    public void setCmd(int cmd) {
        Cmd = cmd;
    }

    public int getModule() {
        return Module;
    }

    public void setModule(int module) {
        Module = module;
    }

    public T getData() {
        return Data;
    }

    public void setData(T data) {
        Data = data;
    }
}
