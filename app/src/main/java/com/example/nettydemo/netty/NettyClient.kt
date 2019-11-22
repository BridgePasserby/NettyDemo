package com.hyk.healthhome.netty

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.nettydemo.netty.NETTY_HOST
import com.example.nettydemo.netty.NETTY_POST
import com.example.nettydemo.netty.NettyReceiveListener
import com.example.nettydemo.netty.bean.NettyResponse
import com.example.nettydemo.netty.dispatch.DefaultDispatch
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.DelimiterBasedFrameDecoder
import io.netty.handler.codec.Delimiters
import io.netty.handler.codec.string.StringDecoder
import io.netty.handler.codec.string.StringEncoder
import io.netty.handler.timeout.IdleStateHandler
import io.netty.util.CharsetUtil
import io.netty.util.concurrent.GenericFutureListener
import java.net.InetSocketAddress
import java.util.*
import kotlin.concurrent.thread

class NettyClient private constructor() {
    val TAG = "NettyClient"
    /**
     * 是否需要重连
     */
    private var retry = true
    /**
     * 正在自动重试中....
     */
    private var retrying = false
    /**
     * 重连次数
     */
    var reconnectNum = 4
    var socketChannel: SocketChannel? = null
    var handler: Handler = Handler(Looper.getMainLooper())
    var status = NettyStatus.UNCONNECTED
    var connectListener: ((NettyStatus) -> Unit)? = null // 连接状态回调

    var receiveListeners: MutableList<NettyReceiveListener<*>> = ArrayList()
    var iDispatch = DefaultDispatch()

    fun addReceiveListener(listener: NettyReceiveListener<*>) {
        receiveListeners.add(listener)
    }

    fun removeReceiveListener(listener: NettyReceiveListener<*>){
        receiveListeners.remove(listener)
    }
    companion object {
        var instance = SingleHolder.HOLDER
    }

    private object SingleHolder {
        val HOLDER = NettyClient()
    }

    fun connect(listener: ((NettyStatus) -> Unit)? = null) {
        if (status == NettyStatus.CONNECTED || status == NettyStatus.CONNECTING) {
            Log.i(TAG, "connect() status = $status，请勿频繁调用")
            return
        }
        this.connectListener = listener
        connectServer()
    }

    private fun connectServer(listener: ((NettyStatus) -> Unit)? = null) {
        thread {
            Log.i(TAG, "connectServer Thread Name= ${Thread.currentThread().name}")
            updateStatus(NettyStatus.CONNECTING)
            listener?.invoke(NettyStatus.CONNECTING)
            val group = NioEventLoopGroup()
            Bootstrap()
                .channel(NioSocketChannel::class.java)
                .group(group)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(socketChannel: SocketChannel) {
                        val pipeline = socketChannel.pipeline()
                        pipeline.addLast(
                            DelimiterBasedFrameDecoder(
                                4096,
                                *Delimiters.lineDelimiter()
                            )
                        )
                        pipeline.addLast(StringDecoder(CharsetUtil.UTF_8))
                        pipeline.addLast(StringEncoder(CharsetUtil.UTF_8))
                        pipeline.addLast(ChannelHandle())
                        pipeline.addLast(IdleStateHandler(0, 30, 0))
                    }
                })
                .connect(InetSocketAddress(NETTY_HOST, NETTY_POST))
                .addListener(object : GenericFutureListener<ChannelFuture> {
                    override fun operationComplete(p0: ChannelFuture?) {
                        if (p0!!.isSuccess()) {
                            socketChannel = p0.channel() as SocketChannel
                            updateStatus(NettyStatus.CONNECTED)
                            listener?.invoke(NettyStatus.CONNECTED)
                            Log.e(TAG, "connect success")
                        } else {
                            Log.e(TAG, "connect failed")
                            updateStatus(NettyStatus.UNCONNECTED)
                            listener?.invoke(NettyStatus.UNCONNECTED)
                            close()
                            // 这里一定要关闭，不然一直重试会引发OOM
                            (p0.channel() as SocketChannel).close()
                            group.shutdownGracefully()
                        }
                    }
                })
        }
    }

    @Synchronized
    private fun updateStatus(status: NettyStatus) {
        this.status = status
        if (status == NettyStatus.UNCONNECTED && retrying) {
            runOnUi(Runnable {
                connectListener?.invoke(NettyStatus.RETRYING) // 抛给外层重连状态
            })
            return
        }
        runOnUi(Runnable {
            connectListener?.invoke(status)
        })
    }

    fun retryConnect(delay: Long = 5000) {
        if (!retry) {
            Log.e(TAG, "retryConnect: 不需要重连!!")
            return
        }
        if (reconnectNum <= 0) {
            Log.e(TAG, "retryConnect: 超过重连次数")
            runOnUi(Runnable {
                Log.i(TAG, "retryConnect: 重连失败！建议重启！！！")
                connectListener?.invoke(NettyStatus.RETRYING_FAILED) // 抛给外层重连结果
            })
            retrying = false
            return
        }
        handler.postDelayed({
            reconnectNum--
            retrying = true
            connectServer {
                when (it) {
                    NettyStatus.UNCONNECTED -> {
                        retryConnect(delay)
                    }
                    NettyStatus.CONNECTED -> {
                        Log.i(TAG, "retryConnect: 连接成功!!")
                    }
                    NettyStatus.CONNECTING -> {
                        Log.i(TAG, "retryConnect: 连接中...")
                    }
                    NettyStatus.RETRYING -> {
                        Log.i(TAG, "retryConnect: 重连中......")
                    }
                    NettyStatus.RETRYING_FAILED -> {
                        Log.i(TAG, "retryConnect: 重连失败！请重启！！！")
                    }
                }
            }
        }, delay)
    }

    @Synchronized
    fun send(json: String, sendCallback: ((Int,String) -> Unit)? = null) {
        if (status != NettyStatus.CONNECTED) {
            sendCallback?.invoke(0,"channel 未连接")
            Log.e(TAG, "发送失败，原因：当前未连接")
            return
        }
        if (socketChannel == null) {
            sendCallback?.invoke(0,"socketChannel为空")
            Log.e(TAG, "发送失败，原因：socketChannel为空")
            return
        }
        if (!socketChannel!!.isWritable) {
            sendCallback?.invoke(0,"socketChannel不可写")
            Log.e(TAG, "发送失败，原因：socketChannel不可写")
            return
        }
        if (!socketChannel!!.isActive) {
            sendCallback?.invoke(0,"socketChannel未激活")
            Log.e(TAG, "发送失败，原因：socketChannel未激活")
            return
        }
        socketChannel!!.writeAndFlush(json + "\r\n")
            .addListener { future ->
                if (future.isSuccess()) {
                    runOnUi(Runnable {
                        sendCallback?.invoke(1,"发送成功")
                    })
                } else {
                    runOnUi(Runnable {
                        sendCallback?.invoke(0,"发送失败...")
                    })
                }
            }
    }

    fun handelReceive(p0: ChannelHandlerContext?, p1: String?) {
        val gson = Gson()
        var nettyResponse: NettyResponse<*>? = null
        try {
            nettyResponse = gson.fromJson<NettyResponse<*>>(p1, NettyResponse::class.java)
        }catch (e: JsonSyntaxException) {
            nettyResponse = NettyResponse<String>()
            nettyResponse.messageType = "unknow"
            nettyResponse.isSuccess = false
            nettyResponse.reason = "数据格式错误"
        } finally {
            for (listener in receiveListeners) {
                iDispatch.dealMessage(nettyResponse, listener)
            }
        }
    }

    fun close() {
        if (status != NettyStatus.UNCONNECTED) {
            updateStatus(NettyStatus.UNCONNECTED)
        }
        if (socketChannel != null) {
            socketChannel!!.close()
            socketChannel = null
        }
/*        if (mClientThread != null) {
            try {
                mClientThread!!.interrupt()
            } catch (e: Exception) {
                e.printStackTrace()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            } finally {
                mClientThread = null
            }
        }*/
    }

    private fun runOnUi(r: Runnable) {
        handler.post(r)
    }
}