package com.example.ardronecontrol;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.*;
import java.net.URL;
import java.nio.CharBuffer;

import android.os.Handler;


/**
 * Created with IntelliJ IDEA.
 * User: Andrey
 * Date: 17.05.13
 * Time: 20:33
 * To change this template use File | Settings | File Templates.
 */
public class SendGetRequest implements Runnable {

    private String ipAddress;
    private SharedPreferences sp;
    private String request;
    private Handler mHandler;

    //private Exception exception;
    public SendGetRequest(String address, SharedPreferences sp) {
        ipAddress = address;
        this.sp = sp;
    }

    //private Exception exception;
    public SendGetRequest(String address, SharedPreferences sp, String request) {
        ipAddress = address;
        this.sp = sp;
        this.request = request;
    }

    public String getIpAddress() {
        String tp = sp.getString("address",null);
        return tp==null ? ipAddress : tp;
    }

    public void setIpAddress(String ipAddress) {
       this.ipAddress = ipAddress;
    }

    public void run() {
       if (request!=null) {
           doInBackground(request);
       }
    }

    protected Void doInBackground(String... urls) {
        Log.d("START COMMAND_SENDING", urls[0]);
        for (int i = 0; i < urls.length && urls[i] != null; ++i) {
            try {
                if (urls[i].contains("image")) {
                    InputStream is = null;
                    try {
                        is = (InputStream) new URL("http://"+getIpAddress()+urls[i]).getContent();
                    } catch (Exception e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    Drawable d = Drawable.createFromStream(is, "src name");

                    //LinearLayout mainLayout = (LinearLayout) findViewById(R.id.mainLayout);
                    Message msg = new Message();
                    msg.obj = d;
                    getmHandler().sendMessage(msg);
                    //getMainView().setBackground(d);
                } else if (urls[i].contains("drone/battery")) {
                    InputStream is = null;
                    String answer=null;
                    try {
                        is = new URL("http://"+getIpAddress()+urls[i]).openStream();
                        InputStreamReader isr = new InputStreamReader(is);
                        BufferedReader br = new BufferedReader(isr);
                        answer = br.readLine();
                    } catch (Exception e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    //LinearLayout mainLayout = (LinearLayout) findViewById(R.id.mainLayout);
                    Message msg = new Message();
                    msg.obj = answer==null?"-1":answer;
                    getmHandler().sendMessage(msg);
                } else if (urls[i].contains("drone/qr")) {
                    InputStream is = null;
                    String answer=null;
                    try {
                        is = new URL("http://"+getIpAddress()+urls[i]).openStream();
                        InputStreamReader isr = new InputStreamReader(is);
                        BufferedReader br = new BufferedReader(isr);
                        answer = br.readLine();
                    } catch (Exception e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    //LinearLayout mainLayout = (LinearLayout) findViewById(R.id.mainLayout);
                    Message msg = new Message();
                    msg.obj = answer==null?"null":answer;
                    getmHandler().sendMessage(msg);
                } else
                {
                String url = "http://" + getIpAddress() + urls[i];
                HttpClient client = new DefaultHttpClient();
                Log.d("URL", url);
                client.execute(new HttpGet(url));
                Log.d("COMMAND_SENT", url);
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public Handler getmHandler() {
        return mHandler;
    }

    public void setmHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }
}