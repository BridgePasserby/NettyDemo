//package com.hyk.healthhome.netty
//
//import android.app.Service
//import android.content.Intent
//import android.os.IBinder
//import android.util.Log
//import io.netty.bootstrap.Bootstrap
//import io.netty.channel.ChannelFutureListener
//import io.netty.channel.ChannelInitializer
//import io.netty.channel.ChannelOption
//import io.netty.channel.nio.NioEventLoopGroup
//import io.netty.channel.socket.SocketChannel
//import io.netty.channel.socket.nio.NioSocketChannel
//import io.netty.handler.codec.DelimiterBasedFrameDecoder
//import io.netty.handler.codec.Delimiters
//import io.netty.handler.codec.string.StringDecoder
//import io.netty.handler.codec.string.StringEncoder
//import io.netty.handler.timeout.IdleStateHandler
//import io.netty.util.CharsetUtil
//import java.net.InetSocketAddress
//
//class NettyService : Service() {
//    val TAG = "NettyService"
//
//    override fun onBind(intent: Intent?): IBinder? {
//        return null
//    }
//
//    override fun onCreate() {
//        super.onCreate()
//        NettyCache.nettyService = this
//    }
//
//    fun connect(){
//        if (status === NettyStatus.CONNECTING || status === NettyStatus.CONNECTED) {
//            Log.d(TAG, "connect() channel state = $status")
//            return
//        }
//        setRetry(true)
//        updateStatus(NettyStatus.CONNECTING)
//        val group = NioEventLoopGroup()
//        Bootstrap()
//                .channel(NioSocketChannel::class.java)
//                .group(group)
//                .option(ChannelOption.SO_KEEPALIVE, true)
//                .option(ChannelOption.TCP_NODELAY, true)
//                .handler(object : ChannelInitializer<SocketChannel>() {
//                    override fun initChannel(socketChannel: SocketChannel) {
//                        val pipeline = socketChannel.pipeline()
//                        pipeline.addLast(DelimiterBasedFrameDecoder(4096, *Delimiters.lineDelimiter()))
//                        pipeline.addLast(StringDecoder(CharsetUtil.UTF_8))
//                        pipeline.addLast(StringEncoder(CharsetUtil.UTF_8))
//                        pipeline.addLast(ChannelHandle())
//                        pipeline.addLast(IdleStateHandler(0, 30, 0))
//                    }
//                })
//                .connect(InetSocketAddress(HOST, PORT))
//                .addListener({ future ->
//                    if (future.isSuccess()) {
//                        socketChannel = future.channel() as SocketChannel
//                        updateStatus(NettyStatus.CONNECTED)
//                        if (callback != null) {
//                            callback.onSuccess()
//                        }
//                    } else {
//                        Log.e(TAG, "connect failed")
//                        updateStatus(NettyStatus.UNCONNECTED)
//                        close()
//                        // 这里一定要关闭，不然一直重试会引发OOM
//                        future.channel().close()
//                        group.shutdownGracefully()
//                        if (callback != null) {
//                            callback.onFailed(CONNECTED_FAILED)
//                        }
//                    }
//                } as ChannelFutureListener)
//
//    }
//}