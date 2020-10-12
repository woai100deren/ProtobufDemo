package com.dj.protobufdemo;

import android.util.Log;

import androidx.annotation.NonNull;

import com.dhh.websocket.Config;
import com.dhh.websocket.RxWebSocket;
import com.dhh.websocket.WebSocketSubscriber;
import com.dj.library.LogUtils;
import com.dj.protobuf.entity.Login;
import com.google.protobuf.InvalidProtocolBufferException;

import org.greenrobot.eventbus.EventBus;

import java.util.Base64;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.WebSocket;
import okio.ByteString;

/**
 * 网络请求封装类
 * Created by wangjing4 on 2017/8/23.
 */

public class WebSocketNetworkHelper {
    private static final String TAG = WebSocketNetworkHelper.class.getName();
    private static final int DEFAULT_TIMEOUT = 10;//网络超时时间（单位秒）
    public static final String BASE_URL = "ws://192.168.111.102:8091//websocket/axiba";//网络请求URL地址
    private volatile static WebSocketNetworkHelper webSocketNetworkHelper;
    private WebSocket mWebSocket;
    private WebSocketSubscriber webSocketSubscriber;
    private WebSocketNetworkHelper(){
        initWebSocket();
    }

    /**
     * 单例模式，获取网络工具类
     * @return WebSocketNetworkHelper
     */
    public static WebSocketNetworkHelper getInstance(){
        if(webSocketNetworkHelper == null){
            synchronized (WebSocketNetworkHelper.class){
                if(webSocketNetworkHelper == null){
                    webSocketNetworkHelper = new WebSocketNetworkHelper();
                }
            }
        }
        return webSocketNetworkHelper;
    }

    /**
     * 网络连接基本配置
     */
    private void initWebSocket() {
        Config config = new Config.Builder()
                .setShowLog(true, TAG)
                .setClient(new OkHttpClient.Builder().pingInterval(DEFAULT_TIMEOUT, TimeUnit.SECONDS).build())
                .setReconnectInterval(5, TimeUnit.SECONDS)  //set reconnect interval
                .build();
        RxWebSocket.setConfig(config);
    }

    public void connect(){
        if(webSocketSubscriber!=null){
            LogUtils.e(TAG, "webSocket已经发起了连接，不能再连接。");
            return;
        }
        webSocketSubscriber = new WebSocketSubscriber() {
            @Override
            public void onOpen(@NonNull WebSocket webSocket) {
                LogUtils.e(TAG, "webSocket连接成功。");
                mWebSocket = webSocket;
            }

            @Override
            public void onMessage(@NonNull String text) {
                Log.e(TAG, "收到服务器返回数据String:\n" + text);

                //临时模拟，后台通过base64序列化protobuf数据，客户端接收到数据之后，进行反序列化，并且转化成protobuf对象数据
                try {
                    Login.LoginResponse response = Login.LoginResponse.parseFrom(Base64.getDecoder().decode(text));
                    Log.e(TAG, "接收到数据后，解析：code = "+response.getCode() + ",msg = "+response.getMsg());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //如果此处返回在主线程，如果需要处理的数据较大，个人认为再开线程池进行数据转换，然后在转换完之后，EventBus将数据传递出去。
                EventBus.getDefault().post(new WebSocketEvent(text));
            }

            @Override
            public void onMessage(@NonNull ByteString byteString) {
                LogUtils.e(TAG, "收到服务器返回数据ByteString:" + byteString.toString());
//                ResponseManager.handleResponse(byteString,WebSocketActivity.this);

                //如果此处返回在主线程，如果需要处理的数据较大，个人认为再开线程池进行数据转换，然后在转换完之后，EventBus将数据传递出去。
                EventBus.getDefault().post(new WebSocketEvent(byteString.toString()));
            }

            @Override
            protected void onReconnect() {
                LogUtils.e(TAG, "重连:");
            }

            @Override
            protected void onClose() {
                LogUtils.e(TAG, "webSocket断开");
                mWebSocket = null;
                webSocketSubscriber = null;
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }
        };
        RxWebSocket.get(BASE_URL).subscribe(webSocketSubscriber);
    }

    public void closeConnect(){
        if(webSocketSubscriber!=null){
            webSocketSubscriber.dispose();
        }
    }

    public void sendMessage(String message){
        if(mWebSocket!=null){
            mWebSocket.send(message);
        }
    }

    /**
     * 异步发送,若WebSocket已经打开,直接发送,若没有打开,打开一个WebSocket发送完数据,直接关闭.
     */
    public void asyncSendMessage(String message){
        RxWebSocket.asyncSend(BASE_URL,message);
    }

    /**
     * 异步发送,若WebSocket已经打开,直接发送,若没有打开,打开一个WebSocket发送完数据,直接关闭.
     */
    public void asyncSendProtoMessage(ByteString message){
        RxWebSocket.asyncSend(BASE_URL,message);
    }
}
