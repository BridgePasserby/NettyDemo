package com.hyk.healthhome.netty

import android.util.Log
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.util.ReferenceCountUtil

class ChannelHandle : SimpleChannelInboundHandler<String>() {
    val TAG = "ChannelHandle"

    @Throws(Exception::class)
    override fun channelActive(ctx: ChannelHandlerContext) {
        super.channelActive(ctx)
        Log.i(TAG, "channelActive: 客户端与服务端通道-开启，本机IP:" + ctx.channel().localAddress())
    }

    @Throws(Exception::class)
    override fun channelInactive(ctx: ChannelHandlerContext) {
        super.channelInactive(ctx)
        Log.i(TAG, "channelInactive: 客户端与服务端通道-断开")
        NettyClient.instance.close()
        NettyClient.instance.reconnectNum = 4
        NettyClient.instance.retryConnect(3000)
    }

    @Throws(Exception::class)
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        super.exceptionCaught(ctx, cause)
        ctx.close()
        println("异常退出:" + cause.message)
    }

    @Throws(Exception::class)
    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
        Log.d(TAG, "userEventTriggered() called with: ctx = [$ctx], evt = [$evt]")
        super.userEventTriggered(ctx, evt)
        //            if (evt instanceof IdleStateEvent) {
        //                IdleStateEvent e = (IdleStateEvent) evt;
        //                if (e.state() == IdleState.WRITER_IDLE) {
        //                    // 空闲了，发个心跳吧
        //                    NettyPushMessage nettyMessage = new NettyPushMessage();
        //                    // TODO: 2019/5/16 cmd改为心跳(待对接) by Z.kai
        //                    nettyMessage.setCmd(NettyPushMessage.MESSAGE_TYPE_USER_INFO);
        //                    NettyPushMessage.DataBean dataBean = new NettyPushMessage.DataBean();
        //                    dataBean.setUserId(NettyCache.getUserId());
        //                    nettyMessage.setData(dataBean);
        //                    ctx.writeAndFlush(nettyMessage.toJson());
        //                }
        //            }
    }

    override fun channelRead0(p0: ChannelHandlerContext?, p1: String?) {
        Log.i(TAG, "channelRead0() p0 = $p0 ; p1 = $p1")
        NettyClient.instance.handelReceive(p0, p1)
        ReferenceCountUtil.release(p1)
    }

}