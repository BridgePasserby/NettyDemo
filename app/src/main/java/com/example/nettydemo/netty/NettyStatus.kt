package com.hyk.healthhome.netty

enum class NettyStatus {
    CONNECTED, // netty真实状态：已连接
    CONNECTING, // netty真实状态：连接中
    UNCONNECTED, // netty真实状态：未连接
    RETRYING, // netty 暴露给外部的状态(假状态)：自动重试中
    RETRYING_FAILED // netty 暴露给外部的状态(假状态)：自动重试失败，自动重试也未连接成功
}
