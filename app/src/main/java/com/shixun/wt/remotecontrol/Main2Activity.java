package com.shixun.wt.remotecontrol;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class Main2Activity extends AppCompatActivity {
    public byte[] data = new byte[34];

    public void initdata() {
        data[0] = (byte) 0xAA;
        data[1] = (byte) 0xC0;
        data[2] = 0x1C;
        data[3] = (byte) (200>>8);        //油门值拆成高八位
        data[4] = (byte) (200&0xff);      //油门值拆成低八位
        data[31] = 0x1C;
        data[32] = 0x0D;
        data[33] = 0x0A;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        initdata();
    }

    public class myThread implements Runnable {
        @Override
        public void run() {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = adapter.getRemoteDevice("00:0E:0E:0E:30:F5");
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            try {
                BluetoothSocket socket = device.createRfcommSocketToServiceRecord(uuid);
                socket.connect();
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void boost(View view) {
        Thread thread = new Thread(new myThread());
        thread.start();
    }
}
