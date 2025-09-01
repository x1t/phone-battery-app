package com.batteryapp;

import android.app.Activity;
import android.os.Bundle;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;

public class MainActivity extends Activity {
    
    private TextView batteryLevel;
    private TextView statusText;
    private Button reportButton;
    private int currentBatteryLevel = 0;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        batteryLevel = findViewById(R.id.batteryLevel);
        statusText = findViewById(R.id.statusText);
        reportButton = findViewById(R.id.reportButton);
        
        // 获取当前电量
        getBatteryLevel();
        
        // 设置按钮点击事件
        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportBattery();
            }
        });
        
        // 监听电量变化
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }
    
    private void getBatteryLevel() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);
        
        if (batteryStatus != null) {
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            currentBatteryLevel = (int) ((level / (float) scale) * 100);
            
            batteryLevel.setText("当前电量: " + currentBatteryLevel + "%");
        }
    }
    
    private BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            currentBatteryLevel = (int) ((level / (float) scale) * 100);
            
            batteryLevel.setText("当前电量: " + currentBatteryLevel + "%");
        }
    };
    
    private void reportBattery() {
        statusText.setText("正在发送...");
        reportButton.setEnabled(false);
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 构建POST请求
                    URL url = new URL("http://1.14.165.245:7020/fs1?date=1");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    connection.setDoOutput(true);
                    
                    // 构建请求体
                    String postData = "text=" + URLEncoder.encode("xx手机电量" + currentBatteryLevel + "%!", "UTF-8");
                    
                    // 发送请求
                    try (OutputStream os = connection.getOutputStream()) {
                        byte[] input = postData.getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                    }
                    
                    // 读取响应
                    int responseCode = connection.getResponseCode();
                    
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (responseCode == HttpURLConnection.HTTP_OK) {
                                statusText.setText("发送成功! 电量: " + currentBatteryLevel + "%");
                            } else {
                                statusText.setText("发送失败，响应码: " + responseCode);
                            }
                            reportButton.setEnabled(true);
                        }
                    });
                    
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            statusText.setText("发送失败: " + e.getMessage());
                            reportButton.setEnabled(true);
                        }
                    });
                }
            }
        }).start();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(batteryReceiver);
    }
}