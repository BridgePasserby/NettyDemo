package com.example.nettydemo.netty.bean;

/**
 * 服务器响应消息
 */
public class NettyResponse<T> {
    /**
     * messageType : interaction
     * data : 1
     */

    private String messageType;

    private boolean success = true;

    private String reason = "成功";

    private T data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
