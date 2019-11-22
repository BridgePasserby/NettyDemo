package com.example.nettydemo.netty.dispatch;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.nettydemo.netty.NettyReceiveListener;
import com.example.nettydemo.netty.bean.NettyResponse;
import com.google.gson.Gson;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 默认消息处理
 */
public class DefaultDispatch implements IDispatch {
    private static final String TAG = "DefaultDispatch";
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void dealMessage(NettyResponse nettyResponse, final NettyReceiveListener listener) {
        if (nettyResponse == null || nettyResponse.getData() == null) {
            Log.e(TAG, "dealMessage: nettyResponse is null!");
            return;
        }
        if (!nettyResponse.isSuccess()){
            Log.e(TAG, "dealMessage: nettyResponse failed reason = "+nettyResponse.getReason());
            return;
        }
        final Gson gson = new Gson();
        final String messageJson = gson.toJson(nettyResponse.getData());
        final Type type = getSuperclassTypeParameter(listener.getClass());
        runOnUi(new Runnable() {
            @Override
            public void run() {
                try {
                    listener.receive(gson.fromJson(messageJson, type));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void runOnUi(Runnable runnable) {
        mainHandler.post(runnable);
    }

    private static Type getSuperclassTypeParameter(Class<?> subclass) {
        ParameterizedType parameterizedType = (ParameterizedType) subclass.getGenericInterfaces()[0];
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        return actualTypeArguments[0];
    }
}
