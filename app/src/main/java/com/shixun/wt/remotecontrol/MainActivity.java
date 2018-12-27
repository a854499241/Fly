package com.shixun.wt.remotecontrol;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    public byte[] data = new byte[34];
    public OutputStream outputStream;
    public BluetoothSocket socket;
    public static int accelerator = 200;//油门
    public static int course = 1500;//航向
    public static int roll = 1320;//横滚
    public static int pitch = 1420;//俯仰
    public void initdata() {
        data[0] = (byte) 0xAA;
        data[1] = (byte) 0xC0;
        data[2] = (byte) 0x1C;
        data[3] = (byte) (accelerator>>8);        //油门值拆成高八位
        data[4] = (byte) (accelerator&0xff);      //油门值拆成低八位

        data[5] = (byte) (course>>8);        //航向值
        data[6] = (byte) (course&0xff);
        data[7] = (byte) (roll>>8);        //横滚值
        data[8] = (byte) (roll&0xff);
        data[9] = (byte) (pitch>>8);        //俯仰值
        data[10] = (byte) (pitch&0xff);
        data[31] = (byte) 0x1C;
        data[32] = (byte) 0x0D;
        data[33] = (byte) 0x0A;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initdata();
        seekbar();
    }

    public class myThread implements Runnable {
        @Override
        public void run() {
            try {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = adapter.getRemoteDevice("00:0E:0E:0E:30:F4");
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                socket = device.createRfcommSocketToServiceRecord(uuid);
                socket.connect();
                outputStream = socket.getOutputStream();
                Message msg = Message.obtain();
                msg.what = 1;  //1为连接成功
                handler.sendMessage(msg);
            } catch (Exception e) {
                Message msg = Message.obtain();
                msg.what = -1;  //-1为连接错误
                handler.sendMessage(msg);
            }
        }
    }
    public boolean flag = true;
    // 开启持续发送线程SendThread
    public void fly(View view) {
        TextView acc = findViewById(R.id.textView2);
        acc.setText("油门值:"+accelerator);
        flag = true;
        Thread thread = new Thread(new MainActivity.SendThread());
        thread.start();
    }

    public class SendThread implements Runnable {
        @Override
        public void run() {
            try {
                while (flag){
                    outputStream.write(data);
                    Thread.sleep(5);    // 让程序暂停5毫秒
                }

            } catch (Exception e) {
                Message msg = Message.obtain();
                msg.what = -2;  //-2为启动失败
                handler.sendMessage(msg);
            }
        }
    }
    // 启动socket连接
    public void boost(View view) {
        Thread thread = new Thread(new MainActivity.myThread());
        thread.start();
    }

    public void myswitch(View view) {
        flag = !flag;
    }

    public void pressPitch(View view) {
        pitch += 1;
        initdata();
        TextView acc = findViewById(R.id.textView4);
        acc.setText("俯仰值:"+pitch);
    }

    public void loosePitch(View view) {
        pitch -= 1;
        initdata();
        TextView acc = findViewById(R.id.textView4);
        acc.setText("俯仰值:"+pitch);
    }

    public void looseRsoll(View view) {
        roll -= 1;
        initdata();
        TextView acc = findViewById(R.id.textView5);
        acc.setText("横滚值:"+roll);
    }

    public void pressRsoll(View view) {
        roll += 1;
        initdata();
        TextView acc = findViewById(R.id.textView5);
        acc.setText("横滚值:"+roll);
    }

    public void looseAccelerator(View view) {
        accelerator -= 50;
        initdata();
        TextView acc = findViewById(R.id.textView2);
        acc.setText("油门值:"+accelerator);
    }

    public void pressAccelerator(View view) {
        accelerator += 50;
        initdata();
        TextView acc = findViewById(R.id.textView2);
        acc.setText("油门值:"+accelerator);
    }

    public void btnOnclick(View view) {
        Intent intent = new Intent(MainActivity.this, Main2Activity.class);
        startActivity(intent);
    }
    public  void seekbar(){
        SeekBar sb = findViewById(R.id.seekBar);
        sb.setMax(800);

        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                accelerator = progress;
                data[3] = (byte)(progress>>8);
                data[4] = (byte)(progress&0xff);
                TextView tv = findViewById(R.id.textView2);
                tv.setText("当前油门值："+accelerator);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //判断消息码
            if (msg.what==-1) {
                Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
            } else if (msg.what==-2) {
                Toast.makeText(MainActivity.this, "请先启动", Toast.LENGTH_SHORT).show();
            }else if (msg.what==1) {
                Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
            }
        }
    };

    }
