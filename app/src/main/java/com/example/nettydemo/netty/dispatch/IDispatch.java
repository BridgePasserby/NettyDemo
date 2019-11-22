package com.example.nettydemo.netty.dispatch;


import com.example.nettydemo.netty.NettyReceiveListener;
import com.example.nettydemo.netty.bean.NettyResponse;

/**
 * 处理并分发消息
 */
public interface IDispatch {
    /**
     * 处理netty返回的消息
     * @param nettyResponse
     */
    void dealMessage(NettyResponse nettyResponse, NettyReceiveListener listener);

}
