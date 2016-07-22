package com.example.sency.newrequest2;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener {

    private EditText edit;
    private Button one;
    private Button two;
    private Button three;
    private Button four;
    private TextView text;

    private String todayWeather;
    private Handler handler;

    //HttpUrlConnection方式所需成员变量
    private URL url;
    private HttpURLConnection conn;
    private String content;

    //使用HttpClient方式
    HttpClient client;//代表Http客户端

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
                //通过HttpClient的GET方式
                new Thread(new ThreeThread()).start();
                break;
            case R.id.four:
                //使用HttpClient的POST方式
                new Thread(new FourThread()).start();
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
                    if (br != null) {
                        br.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private class ThreeThread implements Runnable {
        InputStream is;
        BufferedReader br;

        @Override
        public void run() {
            //获取路径
            getPath();
            //创建默认的客户端实例
            client = new DefaultHttpClient();
            //创建get请求实例
            HttpGet httpGet = new HttpGet(todayWeather);
            System.out.println("executing request:" + httpGet.getURI());
            try {
                //客户端执行get请求，返回响应实体
                HttpResponse response = client.execute(httpGet);
                //获取响应消息实体
                HttpEntity entity = response.getEntity();
                //将实体封装为流输入
                is = entity.getContent();
                //使用高效字符流读取
                br = new BufferedReader(new InputStreamReader(is));
                String line = null;
                StringBuffer sb = new StringBuffer();
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                sb.append("\n" + "\nWays:HttpClient-GET");
                content = sb.toString();
                Message message = new Message();
                message.what = 1;
                handler.sendEmptyMessage(message.what);
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

    private class FourThread implements Runnable {

        InputStream is;
        BufferedReader br;

        @Override
        public void run() {
            getPath();
            //创建客户端对象
            client = new DefaultHttpClient();
            //封装地址
            HttpPost httpPost = new HttpPost(todayWeather);
            //用list封装要向服务器端发送的参数
            List<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
            pairs.add(new BasicNameValuePair("name", "蛋蛋"));

            try {
                //使用UrlEncodeFormEntity对象来封装List对象
                UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(pairs);
                //设置使用的Entity(实体)
                httpPost.setEntity(urlEncodedFormEntity);
                //客户端开始向指定的网址发送请求
                HttpResponse response = client.execute(httpPost);
                //获得请求的Entity
                HttpEntity entity = response.getEntity();
                is = entity.getContent();
                //读取数据
                br = new BufferedReader(new InputStreamReader(is));
                String line = null;
                StringBuffer sb = new StringBuffer();
                while((line=br.readLine())!=null){
                    sb.append(line);
                }
                sb.append("\n").append("\n").append("Wans:HttpClient-POST");
                content = sb.toString();
                Message message = new Message();
                message.what=1;
                handler.sendEmptyMessage(message.what);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
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
}
