package com.example.sency.netrequest;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 *网络通信基本知识
 * 1,2,3...都是例子，他们之间各自独立,没有关系
 */
public class MainActivity extends Activity {

    private WebView webView;
    private IntentFilter intentFilter;
    private NetBroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //2.WebView的loadUrl()加载网络地址
        webView = (WebView)findViewById(R.id.web);
        webView.loadUrl("http://www.baidu.com");

        intentFilter = new IntentFilter();
        //添加一条广播值
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        broadcastReceiver = new NetBroadcastReceiver();
        //注册
        registerReceiver(broadcastReceiver,intentFilter);

        //3.在线程中使用URL访问网页源码
        new Thread(new Runnable() {
            @Override
            public void run() {
                //使用URL访问网页
                try {
                    //从连接得到输入流
                    InputStream is = new URL("http://www.baidu.com").openStream();
                    //封装输入流
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String line = null;
                    //使用StringBuffere存储所有数据
                    StringBuffer sb = new StringBuffer();
                    while((line=br.readLine())!=null){
                        sb.append(line);
                    }
                    br.close();
                    System.out.println(sb);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    //1.使用广播判断网络是否可用
    class NetBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = cm.getActiveNetworkInfo();
            if(info!=null){
                Toast.makeText(context,"网络连接正常",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(context,"网络连接异常",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }
}
