package com.example.nettydemo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.nettydemo.netty.NETTY_HOST
import com.example.nettydemo.netty.NETTY_POST
import com.example.nettydemo.netty.NettyReceiveListener
import com.example.nettydemo.netty.bean.UserBean
import com.hyk.healthhome.netty.NettyClient
import com.hyk.healthhome.netty.NettyStatus
import kotlinx.android.synthetic.main.activity_second.*

class SecondActivity : AppCompatActivity() {
    val TAG = "SecondActivity"
    var stringBuffer = StringBuffer()
    var receive = StringBuffer()
    // 与后台交互的数据格式
    var cmd: String =
        "{\"CMD\":13,\"Module\":1,\"Data\":{\"UUID\":\"ddaf2c30963e4f32bea657a0d0adae03\",\"Info\":\"%s\"}}"
    val userListner = object : NettyReceiveListener<UserBean> {
        override fun receive(t: UserBean?) {
            receive.append(t!!.userId).append("\n")
            Log.i(TAG, "USER = ${t.toString()}")
            tv_send.text = receive.toString()
        }
    }

    val stringListener = object : NettyReceiveListener<String> {
        override fun receive(t: String?) {
            receive.append(t!!).append("\n")
            Log.i(TAG, "t = ${t.toString()}")
            tv_send.text = receive.toString()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        btn_connect.setOnClickListener {
            NETTY_HOST = et_host.text.toString()
            NETTY_POST = et_port.text.toString().toInt()
            NettyClient.instance.connect {
                when (it) {
                    NettyStatus.UNCONNECTED -> {
                        stringBuffer.append("连接失败").append("\n")
                        tv_receive.text = stringBuffer.toString()
                        NettyClient.instance.close()
                        NettyClient.instance.retryConnect()
                    }
                    NettyStatus.CONNECTED -> {
                        stringBuffer.append("连接成功").append("\n")
                        tv_receive.text = stringBuffer.toString()
                    }
                    NettyStatus.CONNECTING -> {
                        stringBuffer.append("连接中...").append("\n")
                        tv_receive.text = stringBuffer.toString()
                    }
                    NettyStatus.RETRYING -> {
                        stringBuffer.append("重连中...").append("\n")
                        tv_receive.text = stringBuffer.toString()
                    }
                    NettyStatus.RETRYING_FAILED -> {
                        stringBuffer.append("重连失败...").append("\n")
                        tv_receive.text = stringBuffer.toString()
                    }
                }
            }
        }
        btn_send.setOnClickListener {
            sendUser(et_message.text.toString())
        }

        NettyClient.instance.addReceiveListener(userListner)

        NettyClient.instance.addReceiveListener(stringListener)
    }

    fun sendUser(user: String) {
        val format = String.format(cmd, user)
        NettyClient.instance.send(format, { state, reason ->
            Log.i(TAG, "state = $state ; reason = $reason")
        })

    }

    override fun onDestroy() {
        super.onDestroy()
        NettyClient.instance.removeReceiveListener(userListner)
        NettyClient.instance.removeReceiveListener(stringListener)
    }
}
