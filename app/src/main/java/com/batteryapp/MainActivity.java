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
import android.widget.EditText;
import android.view.View;
import android.os.Handler;
import android.content.SharedPreferences;
import android.widget.Toast;
import java.io.FileWriter;
import java.io.File;
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
    private Button saveIdButton;
    private EditText phoneIdInput;
    private int currentBatteryLevel = 0;
    private String phoneId = "未命名手机";
    private SharedPreferences prefs;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        batteryLevel = findViewById(R.id.batteryLevel);
        statusText = findViewById(R.id.statusText);
        reportButton = findViewById(R.id.reportButton);
        saveIdButton = findViewById(R.id.saveIdButton);
        phoneIdInput = findViewById(R.id.phoneIdInput);
        
        // 初始化SharedPreferences
        prefs = getSharedPreferences("BatteryApp", MODE_PRIVATE);
        
        // 加载已保存的手机标识
        phoneId = prefs.getString("phone_id", "未命名手机");
        phoneIdInput.setText(phoneId);
        
        // 获取当前电量
        getBatteryLevel();
        
        // 应用启动后自动发送电量报告
        statusText.setText("正在自动发送电量报告...");
        reportBattery();
        
        // 设置保存按钮点击事件
        saveIdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePhoneId();
            }
        });
        
        // 设置按钮点击事件（备用手动发送）
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
                    URL url = new URL("https://ip.wcy9.com/tongzhi/fs1?date=1");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    connection.setDoOutput(true);
                    
                    // 构建请求体（包含手机标识）
                    String postData = "text=" + URLEncoder.encode(phoneId + "电量" + currentBatteryLevel + "%!", "UTF-8");
                    
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
                                // 自动化模式：发送成功后2秒自动关闭应用
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        finish();
                                    }
                                }, 2000);
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
    
    private void savePhoneId() {
        String inputId = phoneIdInput.getText().toString().trim();
        if (inputId.isEmpty()) {
            Toast.makeText(this, "请输入手机标识", Toast.LENGTH_SHORT).show();
            return;
        }
        
        phoneId = inputId;
        
        // 保存到SharedPreferences
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("phone_id", phoneId);
        editor.apply();
        
        // 保存到txt文件
        saveToFile();
        
        Toast.makeText(this, "手机标识已保存: " + phoneId, Toast.LENGTH_SHORT).show();
    }
    
    private void saveToFile() {
        try {
            // 保存到外部存储的txt文件
            File dir = getExternalFilesDir(null);
            File file = new File(dir, "phone_id.txt");
            
            FileWriter writer = new FileWriter(file);
            writer.write("手机标识: " + phoneId + "\n");
            writer.write("保存时间: " + java.text.DateFormat.getDateTimeInstance().format(new java.util.Date()) + "\n");
            writer.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(batteryReceiver);
    }
}