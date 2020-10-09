package com.dj.protobufdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.dj.library.LogUtils;
import com.dj.protobuf.entity.Login;
import com.dj.protobufdemo.databinding.ActivityMainBinding;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import okio.ByteString;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding dataBinding =  DataBindingUtil.setContentView(this, R.layout.activity_main);
        dataBinding.connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //不封装的操作方式
//                OkHttpClient okHttpClient = new OkHttpClient();
//                Request request = new Request.Builder().url("ws://192.168.111.102:8091/websocket/axiba").build();
//                DJWebSocketListener listener = new DJWebSocketListener();
//                WebSocket ws = okHttpClient.newWebSocket(request, listener);
////                okHttpClient.dispatcher().executorService().shutdown();

                //采用第三方sdk封装的方式
                WebSocketNetworkHelper.getInstance().connect();
            }
        });

        dataBinding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebSocketNetworkHelper.getInstance().sendMessage("我是一条小鱼儿");
            }
        });

        dataBinding.asyncSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebSocketNetworkHelper.getInstance().asyncSendMessage("我是一条异步发送的消息");
            }
        });

        dataBinding.sendProtoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Login.LoginRequest request = Login.LoginRequest.newBuilder()
                        .setUsername("axiba")
                        .setPassword("123456")
                        .build();

                WebSocketNetworkHelper.getInstance().asyncSendProtoMessage(ByteString.of(request.toByteArray()));
            }
        });

        EventBus.getDefault().register(this);
    }

    @Subscribe
    public void onMessageEvent(WebSocketEvent event){
        LogUtils.e("消息："+event.message+",线程："+Thread.currentThread());
    }

    @Override
    protected void onDestroy() {
        WebSocketNetworkHelper.getInstance().closeConnect();
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
