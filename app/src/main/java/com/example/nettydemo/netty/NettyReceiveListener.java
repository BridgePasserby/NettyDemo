package com.example.nettydemo.netty;

public interface NettyReceiveListener<T> {
    // FIXME 每次send后是否需要服务器返回他接收成功？
//    void failed();

    void receive(T t);
}
