package com.example.sency.newrequest2;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends Activity implements View.OnClickListener {

    private EditText edit;
    private Button one;
    private Button two;
    private Button three;
    private Button four;
    private TextView text;

    private String todayWeather;

    private URL url;
    private HttpURLConnection conn;
    private String content;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        one.setOnClickListener(this);
        two.setOnClickListener(this);
        three.setOnClickListener(this);
        four.setOnClickListener(this);

    }

    private void initView() {
        edit = (EditText) findViewById(R.id.edit);
        one = (Button) findViewById(R.id.one);
        two = (Button) findViewById(R.id.two);
        three = (Button) findViewById(R.id.three);
        four = (Button) findViewById(R.id.four);
        text = (TextView) findViewById(R.id.text);

        handler = new MyHandler();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.one:
                new Thread(new OneThread()).start();
                break;
            case R.id.two:
                //通过HttpUrlConnection的POST方式
                new Thread(new TwoThread()).start();
                break;
            case R.id.three:
                break;
            case R.id.four:
                break;
            default:
                break;
        }
    }

    public void getPath() {
        String todayStart = "http://api.k780.com:88/?app=weather.today&weaid=";
        String todayCity = edit.getText().toString();
        String todayEnd = "&&appkey=10003&sign=b59bc3ef6191eb9f747dd4e83c99f2a4&format=json";
        todayWeather = (todayStart + todayCity + todayEnd);
    }

    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                text.setText(content);
            } else {
                text.setText("出错了");
            }
        }
    }


    private class OneThread implements Runnable {
        InputStream is;
        BufferedReader br;

        @Override
        public void run() {
            getPath();
            try {
                //通过路径得到URL对象
                url = new URL(todayWeather);
                //打开服务器
                conn = (HttpURLConnection) url.openConnection();
                //连接服务器
                conn.connect();
                //得到输入流
                is = conn.getInputStream();
                //封装
                br = new BufferedReader(new InputStreamReader(is));
                String line = null;
                //将数据存储在StringBuffere中
                StringBuffer sb = new StringBuffer();
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                sb.append("\n" + "\nWays:HttpUrlConnection-GET");
                content = sb.toString();
                Message message = new Message();
                message.what = 1;
                handler.sendEmptyMessage(message.what);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                    if (br != null) {
                        br.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private class TwoThread implements Runnable {
        private OutputStream os;
        private InputStream is;
        private BufferedReader br;

        @Override
        public void run() {
            getPath();
            try {
                //将String类型的地址的封装为URL
                url = new URL(todayWeather);
                //打开服务器
                conn = (HttpURLConnection) url.openConnection();
                //设置请求方法为POST
                conn.setRequestMethod("POST");
                //POST方法不能缓存数据,则需要手动设置使用缓存的值为false
                conn.setUseCaches(false);
                //连接服务器
                conn.connect();
                /**写入参数*/
                os = conn.getOutputStream();
                //封装输出流
                DataOutputStream dos = new DataOutputStream(os);
                dos.writeBytes("name=" + URLEncoder.encode(edit.getText().toString()));
                dos.close();
                /**读取服务器数据*/
                is = conn.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line = null;
                StringBuffer sb = new StringBuffer();
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                sb.append("\n" + "\nWays:HttpUrlConnection-POST");
                content = sb.toString();
                Message message = new Message();
                message.what = 1;
                handler.sendEmptyMessage(message.what);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                    if (br!=null){
                        br.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
